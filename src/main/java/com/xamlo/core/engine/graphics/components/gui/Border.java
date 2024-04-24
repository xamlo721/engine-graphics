package com.xamlo.core.engine.graphics.components.gui;

public class Border {
	
    private int thickness;
    private Color color;

    public Border(int thickness, Color color) {
        this.thickness = thickness;
        this.color = color;
    }

    public int getThickness() {
        return thickness;
    }

    public Color getColor() {
        return color;
    }
    
}