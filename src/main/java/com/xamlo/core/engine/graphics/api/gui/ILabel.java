package com.xamlo.core.engine.graphics.api.gui;

public interface ILabel extends IWidget {

    void setText(String text);

    String getText();

    void setTextColor(IColor color);

    IColor getTextColor();

    void setFont(IFont font);

    IFont getFont();

}