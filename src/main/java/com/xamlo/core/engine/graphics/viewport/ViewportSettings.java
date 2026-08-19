package com.xamlo.core.engine.graphics.viewport;

import com.xamlo.core.engine.graphics.components.gui.ClipContexts;

/**
 * Глобальное состояние логического пространства UI (E9). По умолчанию null — legacy-режим:
 * рендереры и клип-математика работают с историческими константами {@link ClipContexts},
 * поведение побайтово как до введения вьюпорта. Режим включается явным вызовом
 * {@code setLogicalViewport(...)} у контроллера сцены (опционально приложением), после чего
 * ортоматрицы/клип берут размер заданной сетки вместо зашитых констант.
 */
public final class ViewportSettings {

	/** Активная сетка; null = legacy-режим без опционального ресайза GUI. */
	private static volatile LogicalViewport active = null;

	private ViewportSettings() {
	}

	public static void setActive(LogicalViewport viewport) {
		active = viewport;
	}

	public static LogicalViewport getActive() {
		return active;
	}

	public static boolean isActive() {
		return active != null;
	}

	/** Ширина пространства отрисовки: заданная сетка или legacy-константа, если не задана. */
	public static int effectiveLogicalWidth() {
		LogicalViewport v = active;
		return v == null ? ClipContexts.WINDOW_WIDTH : v.getLogicalWidth();
	}

	/** Высота пространства отрисовки: заданная сетка или legacy-константа, если не задана. */
	public static int effectiveLogicalHeight() {
		LogicalViewport v = active;
		return v == null ? ClipContexts.WINDOW_HEIGHT : v.getLogicalHeight();
	}

}
