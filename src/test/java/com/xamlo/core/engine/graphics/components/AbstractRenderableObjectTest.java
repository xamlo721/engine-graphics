package com.xamlo.core.engine.graphics.components;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class AbstractRenderableObjectTest {

    private static class TestObject extends AbstractRenderableObject {
        @Override
        public GraphicalMesh getMesh() {
            return null;
        }

        @Override
        public void loadMesh() {
        }

        @Override
        public void init() {
        }

        @Override
        public void release() {
        }
    }

    private static float[] dump(Matrix4f matrix) {
        float[] out = new float[16];
        matrix.get(out);
        return out;
    }

    @Test
    void objectMatrixIsIdempotent() {
        TestObject object = new TestObject();
        object.setPosition(1, 2, 3);
        object.setRotation(90, 0, 0);

        float[] first = dump(object.getObjectMatrix());
        float[] second = dump(object.getObjectMatrix());

        assertArrayEquals(first, second, 0.000001f, "getObjectMatrix must not accumulate transforms across calls");
    }

    @Test
    void scaleDoesNotMutateGeometryDeformation() {
        TestObject object = new TestObject();
        object.setExpandGeometry(new Vector3f(1, 2, 3));
        object.setScale(2.0f);
        float xBefore = object.geometryDeformation.x;
        float yBefore = object.geometryDeformation.y;
        float zBefore = object.geometryDeformation.z;

        for (int i = 0; i < 5; i++) {
            object.getObjectMatrix();
            object.getScaleMatrix();
        }

        assertEquals(xBefore, object.geometryDeformation.x, 0.0f,
                "repeated matrix builds must not multiply geometryDeformation by the scale factor each time");
        assertEquals(yBefore, object.geometryDeformation.y, 0.0f);
        assertEquals(zBefore, object.geometryDeformation.z, 0.0f);
    }

    @Test
    void worldMatrixIsIdempotentAndAppliesRotation() {
        TestObject object = new TestObject();
        Vector3f offset = new Vector3f(1, 2, 3);
        Vector3f rotation = new Vector3f(90, 45, 0);

        Matrix4f first = object.getWorldMatrix(offset, rotation, 1.0f);
        float[] snapshot = dump(first);
        Matrix4f second = object.getWorldMatrix(offset, rotation, 1.0f);

        assertArrayEquals(snapshot, dump(second), 0.000001f, "getWorldMatrix must start from identity on every call");
        assertTrue(Math.abs(second.m12()) > 0.5f || Math.abs(second.m21()) > 0.5f,
                "rotation parameters must be applied to the resulting matrix");
    }

}
