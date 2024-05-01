package com.xamlo.core.engine.graphics.opengl.blend;

import org.lwjgl.opengl.GL15;

public enum EnumOpenGLBlendOperationMode {
	
	/// DefaultValue
	/// glBlendEquation
	FUNC_ADD(GL15.GL_FUNC_ADD),
	
	/// Вычитает компоненту приемника из компоненты источника
	FUNC_SUBTRACT(GL15.GL_FUNC_SUBTRACT),
	
	///Вычитает компоненту источника из компоненты приемника
	FUNC_REVERSE_SUBTRACT(GL15.GL_FUNC_REVERSE_SUBTRACT);

	
	private int openglValue;

	EnumOpenGLBlendOperationMode(int openglValue) {
		this.openglValue = openglValue;
	}
	
	int getOpenGLValue() {
		return this.openglValue;
	}

}
