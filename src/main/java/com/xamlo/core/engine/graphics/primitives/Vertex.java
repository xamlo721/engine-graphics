package com.xamlo.core.engine.graphics.primitives;

import java.nio.FloatBuffer;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import com.xamlo.core.engine.graphics.api.primitives.IVertex;

/**
 * Класс Vertex хранит данные о вершине,
 * которую он описывает в виде буфера в куче.
 * 
 * В классе умышленно нет проверок на размерность буфера vertexData
 * Если упадёт, то упадёт - вина программиста
 * @author Satomi
 */
public class Vertex implements IVertex {
	
	private final static int defaultSize = 3;
	
	private FloatBuffer vertexData;
	
	public Vertex() {
	    this(defaultSize);
	}
	
	public Vertex(int size) {
	    vertexData = MemoryUtil.memAllocFloat(size);
	}
    
	@Override
    public FloatBuffer getVertexData() {
		return vertexData.duplicate().flip();
    	
    }
	
	@Override
	public IVertex append(Vector2f vec) {
		vertexData.put(vec.x);
		vertexData.put(vec.y);
		return this;
	}

	@Override
	public IVertex append(Vector3f vec) {
		vertexData.put(vec.x);
		vertexData.put(vec.y);
		vertexData.put(vec.z);	
		return this;	
	}

	@Override
	public IVertex append(Vector4f vec) {
		vertexData.put(vec.x);
		vertexData.put(vec.y);
		vertexData.put(vec.z);
		vertexData.put(vec.w);
		return this;
	}
	
	@Override
	public IVertex append(FloatBuffer data) {
		vertexData.put(data);
		return this;
	}

	@Override
	public IVertex append(float[] data) {
		vertexData.put(data);	
		return this;	
	}
	
	@Override
	public void release() {
	    MemoryUtil.memFree(vertexData);
	}

	@Override
	public Vector3d getNormalVector() {
		float x = vertexData.get(0);
		float y = vertexData.get(1);
		float z = vertexData.get(2);
		return new Vector3d(x, y, z);
	}
	
}
