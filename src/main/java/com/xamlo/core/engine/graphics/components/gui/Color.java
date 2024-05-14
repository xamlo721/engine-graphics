package com.xamlo.core.engine.graphics.components.gui;

import org.joml.Vector4f;

import com.xamlo.core.engine.graphics.api.gui.IColor;

public class Color implements IColor {
	
    private int red;
    private int green;
    private int blue;
    private int alpha;

    public Color(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public Color(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }
    
    @Override
    public int getRed() {
        return red;
    }

    @Override
    public int getGreen() {
        return green;
    }

    @Override
    public int getBlue() {
        return blue;
    }

    @Override
    public int getAlpha() {
        return alpha;
    }

	@Override
	public Vector4f getColorVector() {
		return new Vector4f(this.red, this.green, this.blue, this.alpha);
	}
    
}