package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.IClickListener;
import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.elements.IPushButton;
import com.xamlo.core.engine.graphics.components.AbstractTexture;

public class PushButton extends Label implements IPushButton {

	protected IColor buttonColor;
	protected IColor buttonHoverColor;
	protected IColor buttonPressedColor;
	protected IColor buttonDisabledColor;
	protected int buttonBorderSize;
	protected IColor buttonBorderColor;
	protected int buttonCornerRadius;
    protected AbstractTexture buttonIcon;
    protected EnumIconPosition buttonIconPosition;
    protected int buttonIconSpacing;
    protected EnumAlignment buttonAlignment;
    protected int buttonPadding;
    protected int buttonMargin;
    protected boolean buttonEnabled;
    protected boolean buttonVisible;
    protected boolean buttonPressed;
    protected boolean buttonHovered;
    protected boolean buttonFocused;
    protected IClickListener buttonClickListener;
    
    public PushButton(String text) {
    	super(text);
    	this.buttonVisible = true;
    	this.buttonEnabled = true;
    }
    
    public PushButton() {
    	super();
    }
    
    
    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setTextColor(IColor color) {
        this.textColor = color;
    }

    @Override
    public IColor getTextColor() {
        return textColor;
    }

    @Override
    public void setButtonColor(IColor color) {
        this.buttonColor = color;
    }

    @Override
    public IColor getButtonColor() {
        return buttonColor;
    }

    @Override
    public void setHoverColor(IColor color) {
        this.buttonHoverColor = color;
    }

    @Override
    public IColor getHoverColor() {
        return buttonHoverColor;
    }

    @Override
    public void setButtonPressedColor(IColor color) {
        this.buttonPressedColor = color;
    }

    @Override
    public IColor getButtonPressedColor() {
        return buttonPressedColor;
    }

    @Override
    public void setDisabledColor(IColor color) {
        this.buttonDisabledColor = color;
    }

    @Override
    public IColor getDisabledColor() {
        return buttonDisabledColor;
    }

    @Override
    public void setIcon(AbstractTexture icon) {
        this.buttonIcon = icon;
    }

    @Override
    public AbstractTexture getIcon() {
        return buttonIcon;
    }

    @Override
    public void setIconPosition(EnumIconPosition position) {
        this.buttonIconPosition = position;
    }

    @Override
    public EnumIconPosition getIconPosition() {
        return buttonIconPosition;
    }

    @Override
    public void setIconSpacing(int spacing) {
        this.buttonIconSpacing = spacing;
    }

    @Override
    public int getIconSpacing() {
        return buttonIconSpacing;
    }

    @Override
    public void setAlignment(EnumAlignment alignment) {
        this.buttonAlignment = alignment;
    }

    @Override
    public EnumAlignment getAlignment() {
        return buttonAlignment;
    }

    @Override
    public void setPadding(int padding) {
        this.buttonPadding = padding;
    }

    @Override
    public int getPadding() {
        return buttonPadding;
    }

    @Override
    public void setMargin(int margin) {
        this.buttonMargin = margin;
    }

    @Override
    public int getMargin() {
        return buttonMargin;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.buttonEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return buttonEnabled;
    }

    @Override
    public void setVisible(boolean visible) {
        this.buttonVisible = visible;
    }

    @Override
    public boolean isVisible() {
        return buttonVisible;
    }

    @Override
    public void setPressed(boolean pressed) {
        this.buttonPressed = pressed;
    }

    @Override
    public boolean isPressed() {
        return buttonPressed;
    }

    @Override
    public void setHovered(boolean hovered) {
        this.buttonHovered = hovered;
    }

    @Override
    public boolean isHovered() {
        return buttonHovered;
    }

    @Override
    public void setFocused(boolean focused) {
        this.buttonFocused = focused;
    }

    @Override
    public boolean isFocused() {
        return buttonFocused;
    }

    @Override
    public void setClickListener(IClickListener listener) {
        this.buttonClickListener = listener;
    }

    @Override
    public IClickListener getClickListener() {
        return buttonClickListener;
    }


    @Override
    public String toString() {
    	return "[" + this.widgetName + "]";
    }
}