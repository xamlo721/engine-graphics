package com.xamlo.core.engine.graphics.api.primitives;

import java.nio.FloatBuffer;

import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

public interface IVertex {

	public FloatBuffer getVertexData();
	
	public IVertex append(Vector2f vec);
	
	public IVertex append(Vector3f vec);
	
	public IVertex append(Vector4f vec);
	
	public IVertex append(FloatBuffer data);
	
	public IVertex append(float[] data);
	
	public void release();
	
	public Vector3d getNormalVector();

}
