package com.xamlo.core.engine.graphics.opengl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;

public enum EnumOpenglBlendMode {
	
	/// 0
	ZERO(GL11.GL_ZERO),
	
	/// 1
	ONE(GL11.GL_ONE),
	
	///C_source
	SRC_COLOR(GL11.GL_SRC_COLOR),
	
	/// 1 - C_source
	ONE_MINUS_SRC_COLOR(GL11.GL_ONE_MINUS_SRC_COLOR),
	
	/// C_destionation
	DST_COLOR(GL11.GL_DST_COLOR),
	
	/// 1 - C_destination
	ONE_MINUS_DST_COLOR(GL11.GL_ONE_MINUS_DST_COLOR),
	
	///Равен alpha-компоненте вектора C_source
	SRC_ALPHA(GL11.GL_SRC_ALPHA),
	
	///1 - alpha вектора C_source
	ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA),
	
	///Равен alpha-компоненте вектора C_destination
	DST_ALPHA(GL11.GL_DST_ALPHA),
	
	///1 - alpha вектора C_destination
	ONE_MINUS_DST_ALPHA(GL11.GL_ONE_MINUS_DST_ALPHA),
	
	///равен вектору цвета C_constant
	CONSTANT_COLOR(GL21.GL_CONSTANT_COLOR),
	
	/// 1 - C_constant
	ONE_MINUS_CONSTANT_COLOR(GL21.GL_ONE_MINUS_CONSTANT_COLOR),
	
	///Равен alpha-компоненте вектора C_constant
	CONSTANT_ALPHA(GL21.GL_CONSTANT_ALPHA),

	///1 - alpha вектора C_constant
	ONE_MINUS_CONSTANT_ALPHA(GL21.GL_ONE_MINUS_CONSTANT_ALPHA);
		
	
	private int openglValue;

	EnumOpenglBlendMode(int openglValue) {
		this.openglValue = openglValue;
	}
	
	int getOpenGLValue() {
		return this.openglValue;
	}

}
