package com.xamlo.core.engine.graphics.components;

import org.joml.Vector3f;

public class Ray {
	
    public final Vector3f origin = new Vector3f();
    public final Vector3f direction = new Vector3f();

    public Ray(){}

    public Ray (Vector3f origin, Vector3f direction) {
        this.origin.set(origin);
        this.direction.set(direction);
        this.direction.normalize();
    }


}
