package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.IVerticalGradient;
import com.xamlo.core.engine.graphics.api.gui.IColor;

/**
 * Вертикальный двухцветный градиент внутри собственных границ: линейная интерполяция от цвета
 * верхней кромки ({@link #getTopColor()}) до цвета нижней ({@link #getBottomColor()}), альфа-смешиваемая
 * тем же путём, что и цвет фона элемента. Используется самостоятельно в любой сцене без предположений
 * о родителе (только базовые возможности {@link Widget}). По умолчанию полностью прозрачна —
 * становится видимой после явной установки цветов.
 */
public class GradientRect extends Widget implements IVerticalGradient {

	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	private IColor topColor;
	private IColor bottomColor;

	public GradientRect() {
		super();
		this.backgroundColor = TRANSPARENT; // прозрачный фон, чтобы подложка не заслоняла градиент
		this.topColor = TRANSPARENT;
		this.bottomColor = TRANSPARENT;
	}

	public void setTopColor(IColor color) {
		this.topColor = (color == null) ? TRANSPARENT : color;
	}

	@Override
	public IColor getTopColor() {
		return topColor;
	}

	public void setBottomColor(IColor color) {
		this.bottomColor = (color == null) ? TRANSPARENT : color;
	}

	@Override
	public IColor getBottomColor() {
		return bottomColor;
	}

}
