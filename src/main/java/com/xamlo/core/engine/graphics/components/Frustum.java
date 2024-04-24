package com.xamlo.core.engine.graphics.components;


import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;


public class Frustum {
	
    private Vector4f[] frustumPlanes = new Vector4f[6];
    private Vector3f[] frustumCorners = new Vector3f[8];
    private static Frustum frustum = new Frustum();

    public static Frustum getFrustum() {
        if(frustum == null) {
            frustum = new Frustum();
        }
        return frustum;
    }

    public boolean pointInFrustum(float x, float y, float z) {
        for (int i = 0; i < 6; i++) {
            if (frustumPlanes[i].x * x + frustumPlanes[i].y * y + frustumPlanes[i].z * z + frustumPlanes[i].w <= 0.0F) {
                return false;
            }
        }
        return true;
    }

    public boolean sphereInFrustum(float x, float y, float z, float radius) {
        for (int i = 0; i < 6; i++) {
            if (frustumPlanes[i].x * x + frustumPlanes[i].y * y + frustumPlanes[i].z * z + frustumPlanes[i].w <= -radius) {
                return false;
            }
        }
        return true;
    }

    public boolean cubeFullyInFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
        for (int i = 0; i < 6; i++) {
            if (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F)
                return false;
        }
        return true;
    }

    public boolean cubeInFrustum(int x, int y, int z, int size) {
        for(int i = 0; i < 6; i++ ) {
            if(frustumPlanes[i].x * (x-size) + frustumPlanes[i].y * (y-size) + frustumPlanes[i].z * (z-size) + frustumPlanes[i].w > 0)
                continue;
            if(frustumPlanes[i].x * (x+size) + frustumPlanes[i].y * (y-size) + frustumPlanes[i].z * (z-size) + frustumPlanes[i].w > 0)
                continue;
            if(frustumPlanes[i].x * (x-size) + frustumPlanes[i].y * (y+size) + frustumPlanes[i].z * (z-size) + frustumPlanes[i].w > 0)
                continue;
            if(frustumPlanes[i].x * (x+size) + frustumPlanes[i].y * (y+size) + frustumPlanes[i].z * (z-size) + frustumPlanes[i].w > 0)
                continue;
            if(frustumPlanes[i].x * (x-size) + frustumPlanes[i].y * (y-size) + frustumPlanes[i].z * (z+size) + frustumPlanes[i].w > 0)
                continue;
            if(frustumPlanes[i].x * (x+size) + frustumPlanes[i].y * (y-size) + frustumPlanes[i].z * (z+size) + frustumPlanes[i].w > 0)
                continue;
            if(frustumPlanes[i].x * (x-size) + frustumPlanes[i].y * (y+size) + frustumPlanes[i].z * (z+size) + frustumPlanes[i].w > 0)
                continue;
            if(frustumPlanes[i].x * (x+size) + frustumPlanes[i].y * (y+size) + frustumPlanes[i].z * (z+size) + frustumPlanes[i].w > 0)
                continue;

            return false;
        }
        return true;
    }

    public boolean nodeFullyInFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
        for (int i = 0; i < 6; i++) {
            if (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F)
                return false;
            if (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F)
                return false;
        }
        return true;
    }

    public boolean cubeIntoFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
        for (int i = 0; i < 6; i++) {
            if (    (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F) &&
                    (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F) &&
                    (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F) &&
                    (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z1 + frustumPlanes[i].w <= 0.0F) &&
                    (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F) &&
                    (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y1 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F) &&
                    (frustumPlanes[i].x * x1 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F) &&
                    (frustumPlanes[i].x * x2 + frustumPlanes[i].y * y2 + frustumPlanes[i].z * z2 + frustumPlanes[i].w <= 0.0F)) {
                return false;
            }
        }
        return true;
    }

    public boolean cubeIntoFrustum(Vector3i min, int size){
        return cubeInFrustum(min.x, min.y, min.z, size);
    }



    public void calculateFrustum(Matrix4f mvp) {
        // ax * bx * cx +  d = 0; store a,b,c,d

        //left plane
        Vector4f leftPlane = new Vector4f(
                mvp.m30() + mvp.m00(),
                mvp.m31() + mvp.m01(),
                mvp.m32() + mvp.m02(),
                mvp.m33() + mvp.m03());

        this.frustumPlanes[0] = Util.normalizePlane(leftPlane);

        //right plane
        Vector4f rightPlane = new Vector4f(
                mvp.m30() - mvp.m00(),
                mvp.m31() - mvp.m01(),
                mvp.m32() - mvp.m02(),
                mvp.m33() - mvp.m03());

        this.frustumPlanes[1] = Util.normalizePlane(rightPlane);

        //bot plane
        Vector4f botPlane = new Vector4f(
                mvp.m30() + mvp.m10(),
                mvp.m31() + mvp.m11(),
                mvp.m32() + mvp.m12(),
                mvp.m33() + mvp.m13());

        this.frustumPlanes[2] = Util.normalizePlane(botPlane);

        //top plane
        Vector4f topPlane = new Vector4f(
                mvp.m30() - mvp.m10(),
                mvp.m31() - mvp.m11(),
                mvp.m32() - mvp.m12(),
                mvp.m33() - mvp.m13());

        this.frustumPlanes[3] = Util.normalizePlane(topPlane);

        //near plane
        Vector4f nearPlane = new Vector4f(
                mvp.m30() + mvp.m20(),
                mvp.m31() + mvp.m21(),
                mvp.m32() + mvp.m22(),
                mvp.m33() + mvp.m23());

        this.frustumPlanes[4] = Util.normalizePlane(nearPlane);

        //far plane
        Vector4f farPlane = new Vector4f(
                mvp.m30() - mvp.m20(),
                mvp.m31() - mvp.m21(),
                mvp.m32() - mvp.m22(),
                mvp.m33() - mvp.m23());

        this.frustumPlanes[5] = Util.normalizePlane(farPlane);

        extractFrustumCorners();
    }
//
//    public boolean AABBInsideFrustum(Aabb aabb) {
//        for (int i = 0; i < 6; i++) {
//            Vector4f p = frustumPlanes[i];
//            int outside = 0;
//            outside += p.dot(new Vector4f(aabb.min.x, aabb.min.y, aabb.min.z, 1.f)) < 0.f ? 1 : 0;
//            outside += p.dot(new Vector4f(aabb.max.x, aabb.min.y, aabb.min.z, 1.f)) < 0.f ? 1 : 0;
//            outside += p.dot(new Vector4f(aabb.min.x, aabb.max.y, aabb.min.z, 1.f)) < 0.f ? 1 : 0;
//            outside += p.dot(new Vector4f(aabb.max.x, aabb.max.y, aabb.min.z, 1.f)) < 0.f ? 1 : 0;
//            outside += p.dot(new Vector4f(aabb.min.x, aabb.min.y, aabb.max.z, 1.f)) < 0.f ? 1 : 0;
//            outside += p.dot(new Vector4f(aabb.max.x, aabb.min.y, aabb.max.z, 1.f)) < 0.f ? 1 : 0;
//            outside += p.dot(new Vector4f(aabb.min.x, aabb.max.y, aabb.max.z, 1.f)) < 0.f ? 1 : 0;
//            outside += p.dot(new Vector4f(aabb.max.x, aabb.max.y, aabb.max.z, 1.f)) < 0.f ? 1 : 0;
//
//            if (outside == 8) {// all points outside the frustum
//                return false;
//            }
//        }
//        return true;
//    }

    private static Vector3f IntersectionPoint(Vector4f a, Vector4f b, Vector4f c) {
        // Formula used
        //                d1 ( N2 * N3 ) + d2 ( N3 * N1 ) + d3 ( N1 * N2 )
        //P =   -------------------------------------------------------------------------
        //                             N1 . ( N2 * N3 )
        //
        // Note: N refers to the normal, d refers to the displacement. '.' means dot product. '*' means cross product

        Vector3f v1, v2, v3;
        Vector3f a3, b3, c3;
        a3 = new Vector3f(a.x, a.y, a.z);
        b3 = new Vector3f(b.x, b.y, b.z);
        c3 = new Vector3f(c.x, c.y, c.z);
        Vector3f cross = b3.cross(c3);

        float f = a3.dot(cross);
        f *= -1.0f;

        cross = b3.cross(c3);
        v1 = cross.mul(a.w);
        //v1 = (a.D * (Vector3.Cross(b.Normal, c.Normal)));

        cross = c3.cross(a3);
        v2 = cross.mul(b.w);
        //v2 = (b.D * (Vector3.Cross(c.Normal, a.Normal)));

        cross = a3.cross(b3);
        v3 = cross.mul(c.w);
        //v3 = (c.D * (Vector3.Cross(a.Normal, b.Normal)));

        Vector3f result = new Vector3f();
        result.x = (v1.x + v2.x + v3.x) / f;
        result.y = (v1.y + v2.y + v3.y) / f;
        result.z = (v1.z + v2.z + v3.z) / f;
        return result;
    }

    private Vector3f intersectionPoint(Vector4f a, Vector4f b, Vector4f c) {
        // Formula used
        //                d1 ( N2 * N3 ) + d2 ( N3 * N1 ) + d3 ( N1 * N2 )
        //P =   ---------------------------------------------------------------------
        //                             N1 . ( N2 * N3 )
        //
        // Note: N refers to the normal, d refers to the displacement. '.' means dot product. '*' means cross product

        Vector3f v1, v2, v3;
        Vector3f a3, b3, c3;
        a3 = new Vector3f(a.x, a.y, a.z);
        b3 = new Vector3f(b.x, b.y, b.z);
        c3 = new Vector3f(c.x, c.y, c.z);
        float f = -a3.dot(b3.cross(c3));

        v1 = b3.cross(c3).mul(a.w);
        v2 = c3.cross(a3).mul(b.w);
        v3 = a3.cross(b3).mul(c.w);

        Vector3f vec = new Vector3f(v1.x + v2.x + v3.x, v1.y + v2.y + v3.y, v1.z + v2.z + v3.z);
        return vec.div(f);
    }

    private void extractFrustumCorners() {
        frustumCorners[0] = intersectionPoint(frustumPlanes[0], frustumPlanes[2], frustumPlanes[4]);
        frustumCorners[1] = intersectionPoint(frustumPlanes[0], frustumPlanes[3], frustumPlanes[4]);
        frustumCorners[2] = intersectionPoint(frustumPlanes[0], frustumPlanes[3], frustumPlanes[5]);
        frustumCorners[3] = intersectionPoint(frustumPlanes[0], frustumPlanes[2], frustumPlanes[5]);
        frustumCorners[4] = intersectionPoint(frustumPlanes[1], frustumPlanes[2], frustumPlanes[4]);
        frustumCorners[5] = intersectionPoint(frustumPlanes[1], frustumPlanes[3], frustumPlanes[4]);
        frustumCorners[6] = intersectionPoint(frustumPlanes[1], frustumPlanes[3], frustumPlanes[5]);
        frustumCorners[7] = intersectionPoint(frustumPlanes[1], frustumPlanes[2], frustumPlanes[5]);
    }

    public Vector3f[] getFrustumCorners() {
        return frustumCorners;
    }
}