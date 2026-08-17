package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.api.gui.font.IFont;

/**
 * Одна строка многострочного текста с собственным стилем.
 * Нулевые поля означают «наследовать от блока» (цвет/шрифт элемента).
 */
public final class TextLineSpec {

	private final String text;
	private final IColor color;
	private final IFont lineFont;

	public TextLineSpec(String text) {
		this(text, null, null);
	}

	public TextLineSpec(String text, IColor color) {
		this(text, color, null);
	}

	public TextLineSpec(String text, IColor color, IFont lineFont) {
		this.text = text == null ? "" : text;
		this.color = color;
		this.lineFont = lineFont;
	}

	public String getText() {
		return text;
	}

	public IColor getColor() {
		return color;
	}

	public IFont getLineFont() {
		return lineFont;
	}

}
