package com.xamlo.core.engine.graphics.renderers;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.joml.Matrix4f;

import com.xamlo.core.engine.graphics.components.ShaderProgram;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout.StretchPiece;

/**
 * Динамический батч текстурированных квадов для режимов растяжения фона, отличных от FILL (E3).
 *
 * <p>Геометрия совпадает с исторической сеткой {@code model.gui.default.element}
 * (позиция float3 + UV float2, stride 20 байт, uint-индексы), поэтому батч рисуется тем же
 * GUI-шейдером без единой новой uniform'ы: позиции вершин — абсолютные логические координаты
 * окна (z = -1 как у остальных проходов элемента), UV — подпрямоугольники исходного
 * изображения. Все квады одного элемента летят в один draw call.</p>
 */
final class TexturedQuadBatch {

    private static final int FLOATS_PER_VERTEX = 5; // x y z u v
    /** Глубина прохода элемента (совпадает со значением Tz в UIElementRenderer). */
    private static final float ELEMENT_Z = -1.0f;

    private int vertexBufferId;
    private int indexBufferId;
    private int vaoId;
    private FloatBuffer vertexData;
    private IntBuffer indexData;
    private final Matrix4f mvpMatrix = new Matrix4f();

    void init() {
        this.vaoId = glGenVertexArrays();
        this.vertexBufferId = glGenBuffers();
        this.indexBufferId = glGenBuffers();

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0);   // position: vec3 @ offset 0
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12);  // texCoord: vec2 @ offset 12
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferId);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    void release() {
        if (vaoId != 0) {
            glBindVertexArray(vaoId);
            glDeleteVertexArrays(vaoId);
            vaoId = 0;
        }
        if (vertexBufferId != 0) {
            glDeleteBuffers(vertexBufferId);
            vertexBufferId = 0;
        }
        if (indexBufferId != 0) {
            glDeleteBuffers(indexBufferId);
            indexBufferId = 0;
        }
        freeNative();
    }

    /**
     * Рисует фрагменты растяжения одним draw call'ом. Шейдер должен быть уже привязан,
     * текстура — на слоте 0, юниформы цвета/клипа — выставлены проходом элемента.
     */
    void draw(ShaderProgram shader, float left, float right, float bottom, float top,
              float nearP, float farP, float absX, float absY, int srcWidth, int srcHeight,
              List<StretchPiece> pieces) {
        if (pieces.isEmpty()) {
            return;
        }
        ensureCapacity(pieces.size());

        final float invW = 1f / srcWidth;
        final float invH = 1f / srcHeight;
        vertexData.clear();
        for (StretchPiece p : pieces) {
            // Порядок углов и UV совпадают с createCharMesh шрифтового атласа:
            // локальный +y уходит вверх экрана, v=0 — верхняя строка изображения.
            float u0 = p.sx() * invW;
            float v0 = p.sy() * invH;
            float u1 = (p.sx() + p.sw()) * invW;
            float v1 = (p.sy() + p.sh()) * invH;
            float x0 = absX + p.dx();
            float yTop = absY + p.dy();
            float x1 = x0 + p.dw();
            float yBottom = yTop + p.dh();

            putVertex(vertexData, x0, yTop, ELEMENT_Z, u0, v0);       // TL
            putVertex(vertexData, x0, yBottom, ELEMENT_Z, u0, v1);    // BL
            putVertex(vertexData, x1, yBottom, ELEMENT_Z, u1, v1);    // BR
            putVertex(vertexData, x1, yTop, ELEMENT_Z, u1, v0);       // TR
        }

        indexData.clear();
        for (int i = 0; i < pieces.size(); i++) {
            int base = i * 4;
            // Тот же порядок граней, что у element.obj и глифов атласа.
            indexData.put(base).put(base + 1).put(base + 3)
                    .put(base + 3).put(base + 1).put(base + 2);
        }

        shader.setUniform("mvp", mvpMatrix.zero().setOrtho(left, right, bottom, top, nearP, farP));

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STREAM_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STREAM_DRAW);
        glDrawElements(GL_TRIANGLES, pieces.size() * 6, GL_UNSIGNED_INT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private static void putVertex(FloatBuffer out, float x, float y, float z, float u, float v) {
        out.put(x).put(y).put(z).put(u).put(v);
    }

    /** Пересоздаёт нативные буферы под большее число квадов (рост только вперёд). */
    private void ensureCapacity(int quads) {
        int neededVertices = Math.max(quads, 1) * 4;
        if (vertexData == null || vertexData.capacity() < neededVertices * FLOATS_PER_VERTEX) {
            freeNative();
            vertexData = memAllocFloat(neededVertices * FLOATS_PER_VERTEX);
        }
        if (indexData == null || indexData.capacity() < Math.max(quads, 1) * 6) {
            indexData = memAllocInt(Math.max(quads, 1) * 6);
        }
    }

    private void freeNative() {
        if (vertexData != null) {
            memFree(vertexData);
            vertexData = null;
        }
        if (indexData != null) {
            memFree(indexData);
            indexData = null;
        }
    }

}
