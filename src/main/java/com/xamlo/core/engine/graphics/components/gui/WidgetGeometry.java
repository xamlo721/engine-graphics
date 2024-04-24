package com.xamlo.core.engine.graphics.components.gui;

public class WidgetGeometry extends WidgetSize {

	protected int xCoord;
	protected int yCoord;
	
	public WidgetGeometry(int xCoord, int yCoord, int width, int height) {
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
	

	
}
