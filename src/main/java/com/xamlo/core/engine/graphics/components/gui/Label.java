package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.elements.ILabel;
import com.xamlo.core.engine.graphics.api.gui.font.IFont;


public class Label extends Widget implements ILabel {

    protected String text;
    protected IColor textColor;
    protected IFont font;
    protected boolean isEnabled;
    protected IColor disableColor;

    public Label() {
        super();
        this.text = "";
        this.textColor = new Color(0, 0, 0);
    }

    public Label(String text) {
        super();
        this.text = text;
        this.textColor = new Color(0, 0, 0);
    }
    
    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public void setTextColor(IColor color) {
        this.textColor = color;
    }

    @Override
    public IColor getTextColor() {
        return this.textColor;
    }

    @Override
    public void setFont(IFont font) {
        this.font = font;
    }

    @Override
    public IFont getFont() {
        return this.font;
    }

	@Override
	public IColor getDisabledColor() {
		return this.disableColor;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.isEnable = enabled;
	}

	@Override
	public boolean isEnabled() {
		return this.isEnable;
	}

	@Override
	public void setDisabledColor(IColor color) {
		this.disableColor = color;
	}

}