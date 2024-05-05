package com.xamlo.core.engine.graphics.components.gui;

public class UIElementGeometry extends ElementSize {

	protected int xCoord;
	protected int yCoord;
	
	public UIElementGeometry(int xCoord, int yCoord, int width, int height) {
		super(width, height);
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}
	
	public int getXCoord() {
		return this.xCoord;
	}
	
	public int getYCoord() {
		return this.yCoord;
	}
	
	@Override
	public String toString() {
		return "[x: " + xCoord + "][y: " + yCoord + "]";
	}

	
}
