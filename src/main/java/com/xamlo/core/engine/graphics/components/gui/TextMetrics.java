package com.xamlo.core.engine.graphics.components.gui;

/** Общие вертикальные метрики строки текста для всех многострочных потребителей. */
public final class TextMetrics {

	private TextMetrics() {
	}

	/** Шаг строки в пикселях по максимальной высоте глифов шрифта и размеру кегля. */
	public static float lineAdvance(float maxGlyphHeight, int fontSize) {
		int size = (fontSize <= 0) ? 12 : fontSize;
		if (maxGlyphHeight <= 0f) {
			return 16f + size * 0.35f;
		}
		return maxGlyphHeight + Math.max(2f, size * 0.25f);
	}

}
