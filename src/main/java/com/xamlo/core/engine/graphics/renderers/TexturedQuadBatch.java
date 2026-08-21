package com.xamlo.core.engine.graphics.renderers;

import com.xamlo.core.engine.graphics.components.ShaderProgram;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

final class TexturedQuadBatch {
    private static final int FLOATS_PER_VERTEX = 5;
    private static final float ELEMENT_Z = -1.0f;
    private int vertexBufferId;
    private int indexBufferId;
    private int vaoId;
    private FloatBuffer vertexData;
    private IntBuffer indexData;
    private final Matrix4f mvpMatrix = new Matrix4f();

    TexturedQuadBatch() {
    }

    void init() {
        this.vaoId = GL30.glGenVertexArrays();
        this.vertexBufferId = GL15.glGenBuffers();
        this.indexBufferId = GL15.glGenBuffers();
        GL30.glBindVertexArray((int)this.vaoId);
        GL15.glBindBuffer((int)34962, (int)this.vertexBufferId);
        GL20.glVertexAttribPointer((int)0, (int)3, (int)5126, (boolean)false, (int)20, (long)0L);
        GL20.glEnableVertexAttribArray((int)0);
        GL20.glVertexAttribPointer((int)1, (int)2, (int)5126, (boolean)false, (int)20, (long)12L);
        GL20.glEnableVertexAttribArray((int)1);
        GL15.glBindBuffer((int)34963, (int)this.indexBufferId);
        GL30.glBindVertexArray((int)0);
    }

    void release() {
        if (this.vaoId != 0) {
            GL30.glBindVertexArray((int)this.vaoId);
            GL30.glDeleteVertexArrays((int)this.vaoId);
            this.vaoId = 0;
        }
        if (this.vertexBufferId != 0) {
            GL15.glDeleteBuffers((int)this.vertexBufferId);
            this.vertexBufferId = 0;
        }
        if (this.indexBufferId != 0) {
            GL15.glDeleteBuffers((int)this.indexBufferId);
            this.indexBufferId = 0;
        }
        this.freeNative();
    }

    /** Вершины — в конвенции element.obj: нормализованный квадрат [-0.5..+0.5] вокруг центра элемента; */
    void draw(ShaderProgram shader, Matrix4f mvp, float absX, float absY, float elemW, float elemH,
              int srcWidth, int srcHeight, List<ImageStretchLayout.StretchPiece> pieces) {
        if (pieces.isEmpty()) {
            return;
        }
        this.ensureCapacity(pieces.size());
        float invW = 1.0f / (float)srcWidth;
        float invH = 1.0f / (float)srcHeight;
        // Масштаб до нормализованного квадрата unit-mesh'а (матрица прохода сама умножит на пиксели элемента).
        final float nxScale = Math.max(1e-6f, 1f / Math.max(1f, elemW));
        final float nyScale = Math.max(1e-6f, 1f / Math.max(1f, elemH));
        this.vertexData.clear();
        for (ImageStretchLayout.StretchPiece p : pieces) {
            float u0 = p.sx() * invW;
            float v0 = p.sy() * invH;
            float u1 = (p.sx() + p.sw()) * invW;
            float v1 = (p.sy() + p.sh()) * invH;
            float x0 = absX + p.dx();
            float yTop = absY + p.dy();
            float x1 = x0 + p.dw();
            float yBottom = yTop + p.dh();
            // Локальные координаты относительно центра фрагмента -> нормализованный квадрат вокруг центра элемента.
            float cxp = absX + p.dw() * 0.5f;
            float cyp = absY + p.dh() * 0.5f;
            TexturedQuadBatch.putVertex(this.vertexData, (x0 - cxp) * nxScale, -(yTop - cyp) * nyScale, -1.0f, u0, v0);
            TexturedQuadBatch.putVertex(this.vertexData, (x0 - cxp) * nxScale, -(yBottom - cyp) * nyScale, -1.0f, u0, v1);
            TexturedQuadBatch.putVertex(this.vertexData, (x1 - cxp) * nxScale, -(yBottom - cyp) * nyScale, -1.0f, u1, v1);
            TexturedQuadBatch.putVertex(this.vertexData, (x1 - cxp) * nxScale, -(yTop - cyp) * nyScale, -1.0f, u1, v0);

        }
        this.indexData.clear();
        for (int i = 0; i < pieces.size(); ++i) {
            int base = i * 4;
            this.indexData.put(base).put(base + 1).put(base + 3).put(base + 3).put(base + 1).put(base + 2);
        }

        GL30.glBindVertexArray((int)this.vaoId);
        GL15.glBindBuffer((int)34962, (int)this.vertexBufferId);
        GL15.glBufferData((int)34962, (FloatBuffer)this.vertexData, (int)35040);
        GL15.glBindBuffer((int)34963, (int)this.indexBufferId);
        GL15.glBufferData((int)34963, (IntBuffer)this.indexData, (int)35040);
        GL11.glDrawElements((int)4, (int)(pieces.size() * 6), (int)5125, (long)0L);
        GL15.glBindBuffer((int)34963, (int)0);
        GL15.glBindBuffer((int)34962, (int)0);
        GL30.glBindVertexArray((int)0);
    }

    private static void putVertex(FloatBuffer out, float x, float y, float z, float u, float v) {
        out.put(x).put(y).put(z).put(u).put(v);
    }

    private void ensureCapacity(int quads) {
        int neededVertices = Math.max(quads, 1) * 4;
        if (this.vertexData == null || this.vertexData.capacity() < neededVertices * 5) {
            this.freeNative();
            this.vertexData = MemoryUtil.memAllocFloat((int)(neededVertices * 5));
        }
        if (this.indexData == null || this.indexData.capacity() < Math.max(quads, 1) * 6) {
            this.indexData = MemoryUtil.memAllocInt((int)(Math.max(quads, 1) * 6));
        }
    }

    private void freeNative() {
        if (this.vertexData != null) {
            MemoryUtil.memFree((Buffer)this.vertexData);
            this.vertexData = null;
        }
        if (this.indexData != null) {
            MemoryUtil.memFree((Buffer)this.indexData);
            this.indexData = null;
        }
    }
}
