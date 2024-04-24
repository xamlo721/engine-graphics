package com.xamlo.core.engine.graphics.components.gui;

import org.joml.Vector2f;

import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.ILine;

public class Line extends Widget implements ILine {

    private Vector2f startPoint;
    private Vector2f endPoint;
    private IColor lineColor;

    public Line(Vector2f startPoint, Vector2f endPoint, IColor lineColor) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.lineColor = lineColor;
    }

    @Override
    public void setStartPoint(Vector2f startPoint) {
        this.startPoint = startPoint;
    }

    @Override
    public Vector2f getStartPoint() {
        return startPoint;
    }

    @Override
    public void setEndPoint(Vector2f endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public Vector2f getEndPoint() {
        return endPoint;
    }

    @Override
    public void setLineColor(IColor lineColor) {
        this.lineColor = lineColor;
    }

    @Override
    public IColor getLineColor() {
        return lineColor;
    }

}