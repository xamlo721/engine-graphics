package com.xamlo.core.engine.graphics.primitives;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;

/**
 * Виды буферов VBO, которые принимает OpenGL.
 * Используется для передачи в функции:
 *  - BindBuffer, 
 *  - BufferData, 
 *  - BufferSubData, 
 *  - MapBuffer, 
 *  - UnmapBuffer, 
 *  - GetBufferSubData,
 *  - GetBufferParameteriv,
 *  - GetBufferPointerv
 * @author Satomi
 *
 */
public enum EnumBufferType {

	VertexBuffer  (GL_ARRAY_BUFFER),
	ElementBuffer (GL_ELEMENT_ARRAY_BUFFER);
	
	private final int openGLValue;

	private EnumBufferType(int levelCode) {
		this.openGLValue = levelCode;
	}
	
	public int getOpenGLValue() {
		return this.openGLValue;
	}
	
}	