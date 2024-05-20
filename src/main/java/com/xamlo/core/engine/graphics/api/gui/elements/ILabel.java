package com.xamlo.core.engine.graphics.api.gui.elements;

import com.xamlo.core.engine.graphics.api.gui.IActivatable;
import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.IFont;
import com.xamlo.core.engine.graphics.api.gui.IFontSupport;

public interface ILabel extends IWidget, 
								IFontSupport,
								IActivatable {

    void setText(String text);

    String getText();

    void setTextColor(IColor color);

    IColor getTextColor();

    void setFont(IFont font);

    IFont getFont();

}