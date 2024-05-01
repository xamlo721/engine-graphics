package com.xamlo.core.engine.graphics.opengl.cull;

import org.lwjgl.opengl.GL11;

public enum EnumOpenGLCullMode {
	
	
	//default-value!
	// Отбрасывает только нелицевые грани
	BACK(GL11.GL_BACK),
	
	///Отбрасывает только лицевые грани
	FRONT(GL11.GL_FRONT),
	
	///Отбрасывает и те и другие грани
	FRONT_AND_BACK(GL11.GL_FRONT_AND_BACK);

	private int openglValue;

	EnumOpenGLCullMode(int openglValue) {
		this.openglValue = openglValue;
	}
	
	int getOpenGLValue() {
		return this.openglValue;
	}

}
