package com.xamlo.core.engine.graphics.components.attribs;

import static org.lwjgl.opengl.GL11.GL_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_2_BYTES;
import static org.lwjgl.opengl.GL11.GL_3_BYTES;
import static org.lwjgl.opengl.GL11.GL_4_BYTES;
import static org.lwjgl.opengl.GL11.GL_DOUBLE;


public enum EnumAttributeType {

	Byte (GL_BYTE),
	
	UnsignedByte (GL_UNSIGNED_BYTE),
	
	Short (GL_SHORT),
	
	UnsignedShort (GL_UNSIGNED_SHORT),

	Int(GL_INT),

	UnsignedInt (GL_UNSIGNED_INT),

	Float (GL_FLOAT),
	
	Bytes2 (GL_2_BYTES),
	
	Bytes3 (GL_3_BYTES),
	
	Bytes4 (GL_4_BYTES),
	
	Double (GL_DOUBLE);

	private final int openGLValue;

	private EnumAttributeType(int levelCode) {
		this.openGLValue = levelCode;
	}
	
	public int getOpenGLValue() {
		return this.openGLValue;
	}
	
	public int getTypeSize() {
		int size = 0;
		switch (this) {
			case Byte: 			size =  1; break;
			case UnsignedByte:  size =  1; break; 
			case Short:  		size =  2; break;
			case UnsignedShort: size =  2; break;
			case Int:  			size =  4; break;
			case UnsignedInt:   size =  1; break;
			case Float: 		size =  4; break;
			case Bytes2:  		size =  2; break;
			case Bytes3: 		size =  3; break;
			case Bytes4: 		size =  4; break;
			case Double: 		size =  8; break;
			default: size = 0; break;
		}
		return size;

	}
	
}
