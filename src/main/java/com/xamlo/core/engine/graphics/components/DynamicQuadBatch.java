package com.xamlo.core.engine.graphics.components;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

/**
 * Публичный компонент стриминговых батчей квадов для immediate-стиля отрисовки поверх движка:
 * layout {pos vec3 (loc 0), uv vec2 (loc 1), color rgba vec4 (loc 2), lightmap vec2 (loc 3)},
 * полный DYNAMIC_DRAW re-upload GPU-буферов на каждом draw. Паттерн загрузки повторяет эталон
 * TexturedQuadBatch (включая урок rewind перед glBufferData и порядок state-setup в draw'е).
 * Связывание текстур вызывающий делает сам (glActiveTexture + bind) — компонент не владеет unit'ами.
 * Uniform 'mvp' программы обязан быть зарегистрирован через ShaderProgram.createUniform до первого draw.
 */
public final class DynamicQuadBatch {
    public static final int FLOATS_PER_VERTEX = 11; // pos3 + uv2 + rgba4 + lightmap2
    private static final int STRIDE_BYTES = FLOATS_PER_VERTEX * 4;

    private int vaoId;
    private int vertexBufferId;
    private int indexBufferId;
    private FloatBuffer vertexData;
    private IntBuffer indexData;
    /** Сколько вершин добавлено с момента последнего draw/discard. */
    private int addedVerts;
    private boolean inited;

    public void init() {
        if (this.inited) {
            return;
        }
        this.vaoId = GL30.glGenVertexArrays();
        this.vertexBufferId = GL15.glGenBuffers();
        this.indexBufferId = GL15.glGenBuffers();
        GL30.glBindVertexArray(this.vaoId);
        GL15.glBindBuffer(34962, this.vertexBufferId); // GL_ARRAY_BUFFER
        GL20.glVertexAttribPointer(0, 3, 5126, false, STRIDE_BYTES, 0L);      // position
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 2, 5126, false, STRIDE_BYTES, 12L);     // uv
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(2, 4, 5126, false, STRIDE_BYTES, 20L);     // rgba color
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(3, 2, 5126, false, STRIDE_BYTES, 36L);     // lightmap uv
        GL20.glEnableVertexAttribArray(3);
        GL15.glBindBuffer(34963, this.indexBufferId);                         // GL_ELEMENT_ARRAY_BUFFER
        GL30.glBindVertexArray(0);
        this.inited = true;
    }

    public void release() {
        if (this.vaoId != 0) {
            GL30.glDeleteVertexArrays(this.vaoId);
            this.vaoId = 0;
        }
        if (this.vertexBufferId != 0) {
            GL15.glDeleteBuffers(this.vertexBufferId);
            this.vertexBufferId = 0;
        }
        if (this.indexBufferId != 0) {
            GL15.glDeleteBuffers(this.indexBufferId);
            this.indexBufferId = 0;
        }
        this.freeNative();
        this.addedVerts = 0;
        this.inited = false;
    }

    /** Забронировать место под не менее чем maxQuads квадов (рост нативных буферов по эталону). */
    public void reserve(int maxQuads) {
        int neededVertices = Math.max(maxQuads, 1) * 4;
        if (this.vertexData == null || this.vertexData.capacity() < neededVertices * FLOATS_PER_VERTEX) {
            this.freeNative();
            this.vertexData = MemoryUtil.memAllocFloat(neededVertices * FLOATS_PER_VERTEX);
        }
        if (this.indexData == null || this.indexData.capacity() < Math.max(maxQuads, 1) * 6) {
            this.indexData = MemoryUtil.memAllocInt(Math.max(maxQuads, 1) * 6);
        }
    }

    /**
     * Одна вершина батча: позиция + uv + rgba-цвет + lightmap-uv. Квад — четыре последовательных
     * appendVertex в порядке углов [левый верхний, левый нижний, правый нижний, правый верхний]
     * в координатах прохода; порядок обязателен для корректной закрутки индексов при отсечении
     * задних граней глобальным бэкфес-каллом окна (CCW+BACK).
     */
    public void appendVertex(float x, float y, float z, float u, float v,
                             float r, float g, float b, float a, float lu, float lv) {
        initIfNeeded();
        int o = addedVerts * FLOATS_PER_VERTEX;
        this.vertexData.put(o, x);
        this.vertexData.put(o + 1, y);
        this.vertexData.put(o + 2, z);
        this.vertexData.put(o + 3, u);
        this.vertexData.put(o + 4, v);
        this.vertexData.put(o + 5, r);
        this.vertexData.put(o + 6, g);
        this.vertexData.put(o + 7, b);
        this.vertexData.put(o + 8, a);
        this.vertexData.put(o + 9, lu);
        this.vertexData.put(o + 10, lv);
        ++addedVerts;
    }

    private void initIfNeeded() {
        if (!this.inited) {
            this.init();
        }
        reserve(this.addedVerts / 4 + 1);
    }

    /** Re-upload накопленных вершин на GPU без отрисовки (зафиксировать батч). */
    public void commit() {
        uploadBuffers();
    }

    /** Отрисовать накопленный батч через переданную программу. Возвращает число вершин в кадре. */
    public int draw(ShaderProgram program, Matrix4f mvp) {
        if (!this.inited || this.vertexData == null || this.indexData == null || this.addedVerts == 0) {
            return 0;
        }
        int nQuads = this.addedVerts / 4;
        this.uploadBuffers();
        GL30.glBindVertexArray(this.vaoId);
        program.bind();
        program.setUniform("mvp", mvp);
        // Индексы квада [A лев-верх, B лев-низ, C прав-низ, D прав-верх] по формуле эталона:
        // A,D,B и B,D,C — противочасовая закрутка под глобальный бэкфес-калл окна (CCW+BACK),
        // проверенная реальным GUI-рендером TexturedQuadBatch на этом же контексте.
        for (int i = 0; i < nQuads; ++i) {
            int base = i * 4;
            int o = i * 6;
            indexData.put(o, base);
            indexData.put(o + 1, base + 3);
            indexData.put(o + 2, base + 1);
            indexData.put(o + 3, base + 1);
            indexData.put(o + 4, base + 3);
            indexData.put(o + 5, base + 2);
        }
        // Явные треугольники вместо GL_QUADS: в core-profile quad'ы как примитив не гарантированы.
        GL11.glDrawElements(5123, nQuads * 6, 5125, 0L); // TRIANGLES, UNSIGNED_INT
        GL30.glBindVertexArray(0);
        program.unbind();
        this.addedVerts = 0;
        return nQuads * 4;
    }

    private void uploadBuffers() {
        if (!this.inited || this.vertexData == null || this.indexData == null) {
            return;
        }
        // NIO-overload glBufferData читает участок [position..limit]: без rewind после заполнения
        // уходил бы нулевой DYNAMIC_DRAW re-upload (урок эталона TexturedQuadBatch).
        this.vertexData.rewind();
        this.indexData.rewind();
        GL30.glBindVertexArray(this.vaoId);
        GL15.glBindBuffer(34962, this.vertexBufferId);
        GL15.glBufferData(34962, this.vertexData, 35040); // GL_DYNAMIC_DRAW
        GL15.glBindBuffer(34963, this.indexBufferId);
        GL15.glBufferData(34963, this.indexData, 35040);
        GL30.glBindVertexArray(0);
    }

    public int getAddedVertexCount() {
        return this.addedVerts;
    }

    /** Сброс накопленных вершин без отрисовки. */
    public void discard() {
        this.addedVerts = 0;
    }

    private void freeNative() {
        if (this.vertexData != null) {
            MemoryUtil.memFree((Buffer) this.vertexData);
            this.vertexData = null;
        }
        if (this.indexData != null) {
            MemoryUtil.memFree((Buffer) this.indexData);
            this.indexData = null;
        }
    }
}
