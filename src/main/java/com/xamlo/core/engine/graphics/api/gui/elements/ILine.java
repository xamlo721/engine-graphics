package com.xamlo.core.engine.graphics.api.gui.elements;

import org.joml.Vector2f;

import com.xamlo.core.engine.graphics.api.gui.IColor;

public interface ILine {
	
    void setStartPoint(Vector2f startPoint);
    
    Vector2f getStartPoint();

    void setEndPoint(Vector2f endPoint);
    
    Vector2f getEndPoint();

    void setLineColor(IColor lineColor);
    
    IColor getLineColor();

}