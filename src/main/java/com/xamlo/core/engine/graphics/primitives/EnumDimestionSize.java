package com.xamlo.core.engine.graphics.primitives;

public enum EnumDimestionSize {
	_1D (1),
	_2D (2),
	_3D (3),
	_4D (4);
	
	private final int openGLValue;

	private EnumDimestionSize(int levelCode) {
		this.openGLValue = levelCode;
	}
	
	public int value() {
		return this.openGLValue;
	}
	
}	