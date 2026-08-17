package com.xamlo.core.engine.graphics.components.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.xamlo.core.engine.graphics.api.gui.IResizable;
import com.xamlo.core.engine.graphics.api.gui.TextLineSpec;
import com.xamlo.core.engine.graphics.font.ApplicationFont;
import com.xamlo.core.engine.graphics.font.UnicodeGlyphFont;
import com.xamlo.core.engine.graphics.fontsystem.FontSystem;

/**
 * Многострочный текстовый блок: каждая строка может иметь собственный цвет и шрифт,
 * длинные строки при включённом переносе ломаются по словам под ширину элемента.
 * Блок рисуется сверху вниз от верхнего края с учётом padding'а.
 */
public class TextBlock extends Label {

	private final List<TextLineSpec> lines = new ArrayList<>();
	// Перенос длинных строк под ширину блока включён по умолчанию: контент не вылезает за рамку.
	private boolean wordWrapEnabled = true;
	private boolean selfClipping = true;


	public TextBlock() {
		super("");
	}

	public TextBlock(String firstLine) {
		super(firstLine);
		this.lines.add(new TextLineSpec(firstLine));
	}

	@Override
	public void setText(String text) {
		this.lines.clear();
		if (text != null && !text.isEmpty()) {
			this.lines.add(new TextLineSpec(text));
		}
	}

	@Override
	public String getText() {
		StringBuilder sb = new StringBuilder();
		for (TextLineSpec line : this.lines) {
			if (sb.length() > 0) {
				sb.append('\n');
			}
			sb.append(line.getText());
		}
		return sb.toString();
	}

	/** Строки рисуются многострочным путём рендерера, а не однострочной маской. */
	@Override
	public List<TextLineSpec> getStyledLines() {
		return this.lines;
	}

	public TextBlock addLine(String text) {
		this.lines.add(new TextLineSpec(text));
		return this;
	}

	public TextBlock addLine(TextLineSpec spec) {
		this.lines.add(spec);
		return this;
	}

	public void clearLines() {
		this.lines.clear();
	}

	public List<TextLineSpec> getLines() {
		return Collections.unmodifiableList(this.lines);
	}

	public boolean isWordWrapEnabled() {
		return this.wordWrapEnabled;
	}

	public void setWordWrap(boolean enabled) {
		this.wordWrapEnabled = enabled;
	}

	/** Контент обрезается рамкой элемента через клип-рендерер. */
	public boolean isSelfClipping() {
		return this.selfClipping;
	}

	public void setSelfClipping(boolean enabled) {
		this.selfClipping = enabled;
	}

	@Override
	public boolean isContentClipped() {
		return this.selfClipping;
	}

	private ApplicationFont resolveKey(TextLineSpec spec) {
		com.xamlo.core.engine.graphics.api.gui.font.IFont font = (spec != null && spec.getLineFont() != null)
				? spec.getLineFont() : this.getFont();
		if (font == null || font.getFontFamily() == null) {
			return null;
		}
		return new ApplicationFont(font.getFontFamily(), font.getFontSize(), font.isBold(), font.isItalic());
	}

	private UnicodeGlyphFont resolveFont(TextLineSpec spec) {
		ApplicationFont key = resolveKey(spec);
		return (key == null) ? null : FontSystem.getInstance().ensureFont(key);
	}

	/** Шаг строки в пикселях для её шрифта (реальные метрики, headless-совместимо). */
	protected float lineAdvanceOf(TextLineSpec spec) {
		ApplicationFont key = resolveKey(spec);
		UnicodeGlyphFont font = resolveFont(spec);
		float h = (font == null) ? 0f : font.getMaxHeight();
		int size = (key == null) ? 12 : key.getFontSize();
		return TextMetrics.lineAdvance(h, size);
	}

	/** Метрика ширины текста (переопределяется в тестах подделкой). */
	protected TextLayout.Measure createMeasure() {
		return new TextLayout.Measure() {
			@Override
			public float widthOf(String text) {
				return measureWidth(null, text);
			}

			@Override
			public float widthOf(TextLineSpec context, String text) {
				return measureWidth(context, text);
			}
		};
	}

	private float measureWidth(TextLineSpec spec, String text) {
		if (text.isEmpty()) {
			return 0f;
		}
		UnicodeGlyphFont font = resolveFont(spec);
		return (font == null) ? text.length() * 7f : font.getStringWidth(text);
	}

	/** Размещение строк по текущей геометрии/выравниванию — общий с рендерером расчёт. */
	public List<TextLayout.PlacedLine> computePlacedLines() {
		UIElementGeometry geometry = ((IResizable) this).getGeometry();
		float pad = Math.max(((IResizable) this).getPadding(), 4f);
		return TextLayout.layout(getAbsX(), getAbsY(), geometry.getWidth(), geometry.getHeight(),
				pad, ((IResizable) this).getAlignment(), this.wordWrapEnabled, this.lines, createMeasure(), this::lineAdvanceOf);
	}

}
