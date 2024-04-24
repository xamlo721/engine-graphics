package com.xamlo.core.engine.graphics.components;

import ru.satomi.dc.primitive.Quaternion;

import java.util.ArrayList;

import org.joml.Vector4f;

public class Util {

	public static Quaternion normalizePlane(Quaternion plane) {
		float mag;
		mag = (float) Math.sqrt(plane.getX() * plane.getX() + plane.getY() * plane.getY() + plane.getZ() * plane.getZ());
		plane.setX(plane.getX()/mag);
		plane.setY(plane.getY()/mag);
		plane.setZ(plane.getZ()/mag);
		plane.setW(plane.getW()/mag);
	
		return plane;
	}

	public static Vector4f normalizePlane(Vector4f plane) {
		float mag;
		mag = (float) Math.sqrt(plane.x * plane.x + plane.y * plane.y + plane.z * plane.z);
		plane.x = (plane.x/mag);
		plane.y = (plane.y/mag);
		plane.z = (plane.z/mag);
		plane.w = (plane.w/mag);

		return plane;
	}
	
}
