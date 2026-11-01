package com.xamlo.core.engine.graphics.components;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_FLOAT;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import com.xamlo.core.engine.graphics.api.components.IRenderEngine;
import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.core.engine.graphics.threads.RenderTaskQueue;
import com.xamlo.engine.api.resources.IShaderResource;
import com.xamlo.engine.resources.ResourceLoader;

/**
 * Рендер-объект чанка мира: собственный VAO + вершинный/индексный буферы с DYNAMIC usage,
 * собственная пара шейдерных программ (debug-color и текстурированная) из
 * {@code shaders/world/chunk{Vertex,Fragment}.glsl} и опциональное затухание во fog по дистанции.
 *
 * <p>Вершинное расположение жёсткое, 9 float на вершину:
 * pos(3) @ location 0, color(4) @ location 1, uv(2) @ location 2.</p>
 *
 * <p>Элемент наследует {@link AbstractSceneElement}, чтобы вписываться в типизацию
 * {@code IScene.getRenderableObject()} ({@code List<AbstractSceneElement>}); унаследованные
 * примитивные шейдеры НЕ используются — init()/release() полностью переопределены под
 * собственные программы, а геометрия живёт не в GraphicalMesh, а в собственных буферах
 * (см. getMesh()).</p>
 *
 * <p><b>Потоки.</b> GL-состояние (программы, буферы) создаётся только на рендер-потоке —
 * либо явно через init(), либо лениво при первом draw(). Данные можно ставить с любого
 * логического потока: attachTo(renderEngine) один раз после старта движка, затем
 * updateLocal(vertices, indices) + submitDirtyUpload() — фактический upload() выполнится
 * в processRenderTasks() ближайшего кадра. Пример для хоста:</p>
 *
 * <pre>{@code
 * // один раз, после Engine.init():
 * chunk.attachTo(gameApp.getEngine().getRenderingEngine());
 * // из любого потока при пересборке чанка:
 * chunk.updateLocal(newVerts, newIndices);   // снимок данных копируется сразу
 * chunk.submitDirtyUpload();                 // загрузится в VRAM следующим кадром
 * }</pre>
 */
public class ChunkMeshElement extends AbstractSceneElement {

    /** Число float на вершину: pos(3)+color(4)+uv(2). */
    public static final int FLOATS_PER_VERTEX = 9;

    private static final String VERTEX_SHADER_ID = "shader.world.chunk.vertex";
    private static final String FRAGMENT_SHADER_ID = "shader.world.chunk.fragment";

    private static final int STRIDE_BYTES = FLOATS_PER_VERTEX * Float.BYTES;

    private final IShaderResource<String> vertexSource;
    private final IShaderResource<String> fragmentSource;

    /** Компилируются лениво на рендер-потоке (см. ensureGl()). */
    private ShaderProgram colorProgram;
    private ShaderProgram textureProgram;
    private boolean glInitialized;
    private boolean released;

    private int vaoId;
    private int vboId;
    private int iboId;
    private int vertexFloats = -1;
    private int indexCount = -1;

    volatile Matrix4f modelMatrix = new Matrix4f();

    private Texture texture;
    /** See {@link #setTranslucent(boolean)}: drawn in the after-opaque blended pass with depth writes off. */
    private boolean translucent = false;

    // Fog uniforms, читается рендер-потоком при draw()
    private volatile float fogFarDistance;
    private volatile Vector3f fogCenter = new Vector3f();
    private final Vector4f fogColorVec = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);

    // AABB чанка для frustum-culling в DefaultSceneRenderer: CORNER-BASED — координата
    // нижнего блока + размер по каждой оси в блоках (типовой MC-чанк: size=16).
    private volatile int chunkMinX, chunkMinY, chunkMinZ;
    private volatile int chunkSx = 16, chunkSy = 16, chunkSz = 16;
    private volatile boolean hasChunkBounds;

    // Снимок данных для асинхронной загрузки (updateLocal копирует массивы сразу)
    private volatile float[] pendingVertices;
    private volatile int[] pendingIndices;
    private RenderTaskQueue taskQueue;

    public ChunkMeshElement() {
        super();
        this.vertexSource = ResourceLoader.INSTANCE().loadShader(VERTEX_SHADER_ID);
        this.fragmentSource = ResourceLoader.INSTANCE().loadShader(FRAGMENT_SHADER_ID);
    }

    /**
     * Компилирует программы и создаёт VAO на рендер-потоке. Идемпотентно; ленивый вызов
     * из bind()/draw() делает явный init() необязательным, но хосту рекомендуется вызывать
     * его явно один раз после старта движка (как у остальных элементов сцены).
     */
    @Override
    public void init() {
        ensureGl();
    }

    @Override
    public void release() {
        if (released || !glInitialized) {
            return;
        }
        released = true;
        glInitialized = false;
        if (vboId != 0) {
            glDeleteBuffers(vboId);
            vboId = 0;
        }
        if (iboId != 0) {
            glDeleteBuffers(iboId);
            iboId = 0;
        }
        if (vaoId != 0) {
            glBindVertexArray(0);
            glDeleteVertexArrays(vaoId);
            vaoId = 0;
        }
        vertexFloats = -1;
        indexCount = -1;
        if (colorProgram != null) {
            colorProgram.cleanup();
            colorProgram = null;
        }
        if (textureProgram != null) {
            textureProgram.cleanup();
            textureProgram = null;
        }
    }

    /** Привязывает движок ради очереди задач рендер-потока. Вызывать один раз после Engine.init(). */
    public void attachTo(IRenderEngine renderEngine) {
        this.taskQueue = renderEngine.getRenderTaskQueue();
    }

    // ------------------------------------------------------------------ геометрия

    @Override
    public GraphicalMesh getMesh() {
        // Геометрия чанка живёт в собственных VBO/IBO, а не в GraphicalMesh:
        // этот элемент намеренно не маршрутизируется через SceneElementRenderer.
        return null;
    }

    @Override
    public void loadMesh() {
        // Не используется: данные приходят через upload()/updateLocal()+submitDirtyUpload().
    }

    /**
     * Загружает вершины и индексы в VRAM. Только рендер-поток.
     * vertices — плоский массив из кратно {@value #FLOATS_PER_VERTEX} float
     * (pos3,color4,uv2 на вершину); indices может быть null — тогда отрисовка идёт
     * обычным glDrawArrays. Повторный вызов с тем же размером обновляет существующие
     * DYNAMIC-буферы через glBufferSubData без регенерации объектов; другой размер
     * приводит к перегенерации.
     */
    public void upload(float[] vertices, int[] indices) {
        ensureGl();
        if (vertices == null || vertices.length % FLOATS_PER_VERTEX != 0) {
            throw new IllegalArgumentException(
                    "ChunkMeshElement.upload: vertex count must be a multiple of " + FLOATS_PER_VERTEX);
        }
        boolean vboChanged = false;
        if (vboId == 0) {
            vboId = glGenBuffers();
            vboChanged = true;
        } else if (vertexFloats >= 0 && vertexFloats != vertices.length) {
            glDeleteBuffers(vboId);
            vboId = glGenBuffers();
            vboChanged = true;
        }
        FloatBuffer vb = MemoryUtil.memAllocFloat(vertices.length);
        vb.put(vertices).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        if (vboChanged) {
            glBufferData(GL_ARRAY_BUFFER, vb, GL_DYNAMIC_DRAW);
            setupVertexLayout(true);
        } else {
            glBufferSubData(GL_ARRAY_BUFFER, 0L, vb);
        }
        MemoryUtil.memFree(vb);

        if (indices != null && indices.length > 0) {
            boolean iboChanged = false;
            if (iboId == 0) {
                iboId = glGenBuffers();
                iboChanged = true;
            } else if (indexCount >= 0 && indexCount != indices.length) {
                glDeleteBuffers(iboId);
                iboId = glGenBuffers();
                iboChanged = true;
            }
            IntBuffer ib = MemoryUtil.memAllocInt(indices.length);
            ib.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);
            if (iboChanged) {
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, GL_DYNAMIC_DRAW);
                setupVertexLayout(false);
            } else {
                glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0L, ib);
            }
            MemoryUtil.memFree(ib);
            indexCount = indices.length;
        } else {
            if (iboId != 0) {
                glDeleteBuffers(iboId);
                iboId = 0;
                setupVertexLayout(false);
            }
            indexCount = -1;
        }

        vertexFloats = vertices.length;
    }

    /** Настройки атрибутов VAO для текущего vbo/ibo. Вызывать после регенерации буферов. */
    private void setupVertexLayout(boolean touchVboPointers) {
        glBindVertexArray(vaoId);
        if (touchVboPointers && vboId != 0) {
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, STRIDE_BYTES, 0L);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 4, GL_FLOAT, false, STRIDE_BYTES, 12L);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, STRIDE_BYTES, 28L);
        }
        if (iboId != 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);
        } else {
            // Сброс EAB-привязки в VAO, если индексный буфер снят
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        glBindVertexArray(0);
    }

    /**
     * Кладёт снимок данных в очередь загрузки с любого потока; фактический upload()
     * выполнится на рендер-потоке ближайшим кадром. Требует предварительного attachTo().
     */
    public void submitDirtyUpload() {
        float[] verts = pendingVertices;
        int[] idx = pendingIndices;
        RenderTaskQueue queue = taskQueue;
        if (queue == null) {
            throw new IllegalStateException(
                    "ChunkMeshElement.submitDirtyUpload: call attachTo(renderEngine) first");
        }
        if (verts == null) {
            return;
        }
        final float[] v = verts;
        final int[] i = idx;
        queue.submit(() -> upload(v, i));
    }

    /** Ставит данные для следующей submitDirtyUpload(); массивы копируются сразу. Любой поток. */
    public void updateLocal(float[] vertices, int[] indices) {
        this.pendingVertices = vertices != null ? vertices.clone() : null;
        this.pendingIndices = indices != null ? indices.clone() : null;
    }

    // ------------------------------------------------------------------ состояние

    public void setModelMatrix(Matrix4f matrix) {
        this.modelMatrix = matrix != null ? matrix : new Matrix4f();
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    /** Текстура элемента (атлас/карта блоков); null — режим debug-цвета через вершинные цвета. */
    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    /**
     * Полупрозрачный материал: элемент рисуется отдельным проходом ПОСЛЕ сплошной геометрии,
     * с включённым блендингом и ВЫКЛЮЧЕННОЙ записью глубины (порядок внутри прохода не
     * сортируется — для водоёмов этого достаточно, глубина пишется только сплошным проходом).
     * Флаг не привязан к конкретной игре: это свойство материала, а не движка.
     */
    public void setTranslucent(boolean translucent) {
        this.translucent = translucent;
    }

    public boolean isTranslucent() {
        return translucent;
    }

    /** Цвет тумана RGB (альфа игнорируется шейдером). */
    public void setFogColor(float r, float g, float b) {
        fogColorVec.set(r, g, b, 1.0f);
    }

    /** Дальняя дистанция затухания в мировых блоках от центра тумана; &lt;= 0 выключает fog. */
    public void setFogFarDistance(float distanceBlocks) {
        this.fogFarDistance = distanceBlocks;
    }

    /** Центр измерения дистанции для fog (обычно позиция камеры), мировые координаты. */
    public void setFogCenter(Vector3f worldPosition) {
        this.fogCenter = worldPosition != null ? worldPosition : new Vector3f();
    }

    /**
     * Регистрирует AABB чанка для frustum-culling: CORNER-BASED семантика — x/y/z это
     * координата нижнего левого заднего блока ящика (не центр!), size — протяжённость по
     * каждой оси в блоках (типовой MC-чанк: size = 16). До первого вызова элемент рисуется
     * без каллинга. Любой поток.
     */
    public void registerChunkBox(int minX, int minY, int minZ, int size) {
        this.registerChunkBox(minX, minY, minZ, size, size, size);
    }

    /**
     * Axis-exact variant of {@link #registerChunkBox(int, int, int, int)} for non-uniform slabs
     * (e.g. a fixed X/Z footprint with an independent vertical extent). Same corner-based semantics.
     */
    public void registerChunkBox(int minX, int minY, int minZ, int sx, int sy, int sz) {
        if (sx <= 0 || sy <= 0 || sz <= 0) {
            return;
        }
        this.chunkMinX = minX;
        this.chunkMinY = minY;
        this.chunkMinZ = minZ;
        this.chunkSx = sx;
        this.chunkSy = sy;
        this.chunkSz = sz;
        this.hasChunkBounds = true;
    }

    public boolean hasChunkBounds() {
        return hasChunkBounds;
    }

    public int getChunkMinX() {
        return chunkMinX;
    }

    public int getChunkMinY() {
        return chunkMinY;
    }

    public int getChunkMinZ() {
        return chunkMinZ;
    }

    public int getChunkSX() {
        return chunkSx;
    }

    public int getChunkSY() {
        return chunkSy;
    }

    public int getChunkSZ() {
        return chunkSz;
    }

    /** Legacy uniform-size view of the culling box (conservative superset over all axes). */
    public int getChunkSize() {
        return Math.max(chunkSx, Math.max(chunkSy, chunkSz));
    }

    // ------------------------------------------------------------------ отрисовка

    /** Полная настройка GL под этот элемент: программа + юниформы + VAO (+текстура). Рендер-поток. */
    public void bind() {
        ensureGl();
        ShaderProgram program = texture != null ? textureProgram : colorProgram;
        program.bind();
        program.setUniform("modelMatrix", modelMatrix);
        program.setUniform("uFogColor", fogColorVec);
        program.setUniform("uFogFar", fogFarDistance);
        program.setUniform("uFogCenter", fogCenter);
        if (texture != null) {
            glActiveTexture(GL_TEXTURE0);
            texture.bind();
            program.setUniform("texture_sampler", 0);
        }
        glBindVertexArray(vaoId);
    }

    /** Симметричное снятие состояния после {@link #bind()}. Рендер-поток. */
    public void unbind() {
        if (!glInitialized) {
            return;
        }
        glBindVertexArray(0);
        ShaderProgram program = texture != null ? textureProgram : colorProgram;
        program.unbind();
    }

    /**
     * Рисует чанк в текущей сцене: projectionMatrix берётся из scene.getProjectionMatrix()
     * (RenderEngine.transformScene каждый кадр кладёт туда projection*view). Тихо пропускает
     * кадр, если данные ещё не загружены или у сцены нет матрицы. Только рендер-поток —
     * именно его вызывает DefaultSceneRenderer для элементов этого типа.
     */
    public void draw(IScene scene) {
        ensureGl();
        Matrix4f mvp = scene == null ? null : scene.getProjectionMatrix();
        if (mvp == null || vertexFloats < 0) {
            return;
        }
        bind();
        getShader().setUniform("projectionMatrix", mvp);
        if (indexCount > 0) {
            glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0L);
        } else {
            glDrawArrays(GL_TRIANGLES, 0, vertexFloats / FLOATS_PER_VERTEX);
        }
        unbind();
    }

    // ------------------------------------------------------------------ внутреннее

    private ShaderProgram currentProgram() {
        return texture != null ? textureProgram : colorProgram;
    }

    /** Переопределение: отдаём активную программу элемента вместо примитивной базовой. */
    @Override
    public ShaderProgram getShader() {
        return currentProgram();
    }

    /** Ленивая компиляция программ + создание VAO на рендер-потоке. Идемпотентно. */
    private synchronized void ensureGl() {
        if (glInitialized || released) {
            return;
        }
        String vsText = vertexSource.getShaderProgram();
        String fsColor = fragmentSource.getShaderProgram();
        String fsTextured = enableTextureDefine(fsColor);

        colorProgram = new ShaderProgram();
        colorProgram.addVertexShader(vsText);
        colorProgram.addFragmentShader(fsColor);
        colorProgram.compileShader();
        registerUniform(colorProgram, "projectionMatrix", "modelMatrix",
                "uFogColor", "uFogFar", "uFogCenter");

        textureProgram = new ShaderProgram();
        textureProgram.addVertexShader(vsText);
        textureProgram.addFragmentShader(fsTextured);
        textureProgram.compileShader();
        registerUniform(textureProgram, "projectionMatrix", "modelMatrix",
                "texture_sampler", "uFogColor", "uFogFar", "uFogCenter");

        vaoId = glGenVertexArrays();
        glInitialized = true;
    }

    /** Вставляет #define USE_TEXTURE сразу после строки #version (она обязана быть первой в GLSL). */
    private static String enableTextureDefine(String glsl) {
        int newline = glsl.indexOf('\n');
        if (newline < 0) {
            return "#define USE_TEXTURE\n" + glsl;
        }
        return glsl.substring(0, newline + 1) + "#define USE_TEXTURE\n" + glsl.substring(newline + 1);
    }

    private void registerUniform(ShaderProgram program, String... names) {
        for (String name : names) {
            try {
                program.createUniform(name);
            } catch (Exception e) {
                throw new IllegalStateException("ChunkMeshElement: uniform not found in chunk shader: " + name, e);
            }
        }
    }

}
