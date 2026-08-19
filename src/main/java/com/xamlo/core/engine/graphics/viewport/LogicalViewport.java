package com.xamlo.core.engine.graphics.viewport;

/**
 * Логическое пространство GUI: фиксированная «дизайн»-сетка, в которой раскладываются
 * элементы и которую растягивает GL-вьюпорт под фактический размер окна. Нейтральная
 * модель без привязки к конкретным проектам: приложение само решает, какую сетку
 * задать при ресайзе (равномерное масштабирование, letterbox с целочисленным шагом или
 * чистый stretch) на основе этих примитивов.
 */
public final class LogicalViewport {

	private final int logicalWidth;
	private final int logicalHeight;

	public LogicalViewport(final int logicalWidth, final int logicalHeight) {
		if (logicalWidth <= 0 || logicalHeight <= 0) {
			throw new IllegalArgumentException(
					"Логический размер должен быть положительным: " + logicalWidth + "x" + logicalHeight);
		}
		this.logicalWidth = logicalWidth;
		this.logicalHeight = logicalHeight;
	}

	public int getLogicalWidth() {
		return logicalWidth;
	}

	public int getLogicalHeight() {
		return logicalHeight;
	}

	/** Пикселей физического окна на единицу логической сети по X (0 — вырожденный физ. размер). */
	public float scaleX(float physicalWidth) {
		return physicalWidth > 0f ? physicalWidth / logicalWidth : 0f;
	}

	/** Пикселей физического окна на единицу логической сети по Y (0 — вырожденный физ. размер). */
	public float scaleY(float physicalHeight) {
		return physicalHeight > 0f ? physicalHeight / logicalHeight : 0f;
	}

	/**
	 * Физическая координата курсора → координата логической сети по X при текущем
	 * физическом размере окна. Вырожденные (<=0) физические размеры мапятся в 0,
	 * выход за границы не обрезается — клиппинг решает потребитель.
	 */
	public float toDesignX(float physicalX, float physicalWidth) {
		if (physicalWidth <= 0f) {
			return 0f;
		}
		return physicalX * logicalWidth / physicalWidth;
	}

	/** См. {@link #toDesignX}: то же самое для оси Y. */
	public float toDesignY(float physicalY, float physicalHeight) {
		if (physicalHeight <= 0f) {
			return 0f;
		}
		return physicalY * logicalHeight / physicalHeight;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LogicalViewport other)) {
			return false;
		}
		return logicalWidth == other.logicalWidth && logicalHeight == other.logicalHeight;
	}

	@Override
	public int hashCode() {
		return 31 * logicalWidth + logicalHeight;
	}

	@Override
	public String toString() {
		return "LogicalViewport[" + logicalWidth + "x" + logicalHeight + "]";
	}

}
