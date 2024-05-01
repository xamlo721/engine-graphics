package com.xamlo.core.engine.graphics.opengl.cull;

import org.lwjgl.opengl.GL11;

public enum EnumOpenGLCullOrder {
	
	
	//default-value!
	// Против часовой стрелки
	CCW(GL11.GL_CCW),
	
	///По часовой стрелке
	CW(GL11.GL_CW);

	private int openglValue;

	EnumOpenGLCullOrder(int openglValue) {
		this.openglValue = openglValue;
	}
	
	int getOpenGLValue() {
		return this.openglValue;
	}

}
