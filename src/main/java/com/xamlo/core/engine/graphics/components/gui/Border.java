package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.IColor;

public class Border {
	
    private int thickness;
    private IColor color;

    public Border(int thickness, IColor color) {
        this.thickness = thickness;
        this.color = color;
    }

    public int getThickness() {
        return thickness;
    }

    public IColor getColor() {
        return color;
    }
    
}