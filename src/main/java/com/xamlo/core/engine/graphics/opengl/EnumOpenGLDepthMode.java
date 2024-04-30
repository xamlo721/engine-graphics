package com.xamlo.core.engine.graphics.opengl;

import org.lwjgl.opengl.GL11;

public enum EnumOpenGLDepthMode {
	
	ALWAYS(GL11.GL_ALWAYS),
	
	NEVER(GL11.GL_NEVER),
	
	LESS(GL11.GL_LESS),
	
	EQUAL(GL11.GL_EQUAL),
	
	LEQUAL(GL11.GL_LEQUAL),
	
	GREATER(GL11.GL_GREATER),
	
	NOTEQUAL(GL11.GL_NOTEQUAL),
	
	GEQUAL(GL11.GL_GEQUAL);

	private int openglValue;

	EnumOpenGLDepthMode(int openglValue) {
		this.openglValue = openglValue;
	}
	
	int getOpenGLValue() {
		return this.openglValue;
	}

}
