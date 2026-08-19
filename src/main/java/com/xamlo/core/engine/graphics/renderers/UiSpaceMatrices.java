package com.xamlo.core.engine.graphics.renderers;

import org.joml.Matrix4f;

import com.xamlo.core.engine.graphics.viewport.ViewportSettings;

/**
 * Чистая математика ортоматриц UI-проходов без GL: единичный меш масштабируется из центра
 * квада в логическом пространстве отрисовки ({@link ViewportSettings}, по умолчанию —
 * исторические 1920×1080). Порядок операций побайтово совпадает с исторической сборкой mvp.
 */
final class UiSpaceMatrices {

	/** Глубина прохода элементов GUI (совпадает со значением Tz у всех рендереров). */
	static final float ELEMENT_Z = -1.0f;

	private static final float NEAR = 0.2f;
	private static final float FAR = 1000.0f;

	private UiSpaceMatrices() {
	}

	/**
	 * Матрица элемента/глифа: ortho(0, W, H, 0) → сдвиг в центр → масштаб на размеры.
	 * Границы W/H задаёт вызывающий код из {@link ViewportSettings}.
	 */
	static void quadMatrix(Matrix4f out, float left, float right, float bottom, float top,
			float centerX, float centerY, float widthPx, float heightPx) {
		out.zero()
				.setOrtho(left, right, bottom, top, NEAR, FAR)
				.translate(centerX, centerY, ELEMENT_Z)
				.scale(widthPx, -heightPx, 1.0f);
	}

}
