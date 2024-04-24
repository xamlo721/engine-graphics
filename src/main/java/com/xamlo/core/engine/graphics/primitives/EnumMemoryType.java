package com.xamlo.core.engine.graphics.primitives;

import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;

/**
 * Тип области памяти, в которой мы хотим разместить буфер.
 * Разные области памяти имеют разные размеры и разную скорость работы с GPU
 * Существуют следующие типы:
 * 		GL_STATIC_DRAW: данные либо никогда не будут изменяться, либо будут изменяться очень редко;
 * 		GL_DYNAMIC_DRAW: данные будут меняться довольно часто;
 * 		GL_STREAM_DRAW: данные будут меняться при каждой отрисовке.	 
 * 
 */
public enum EnumMemoryType {
	
	STATIC  (GL_STATIC_DRAW),
	DYNAMIC (GL_DYNAMIC_DRAW),
	STREAM  (GL_STREAM_DRAW);
	
	private final int openGLValue;

	private EnumMemoryType(int levelCode) {
		this.openGLValue = levelCode;
	}
	
	public int getOpenGLValue() {
		return this.openGLValue;
	}
	
}	