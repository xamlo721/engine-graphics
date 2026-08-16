package com.xamlo.core.engine.graphics.api.gui;

/**
 * UI-элемент, принимающий перетаскиваемые элементы. Диспетчер событий ввода
 * во время активного жеста следит за тем, лежит ли курсор над таргетом, и
 * вызывает у элемента {@link #setDropActive(boolean)} для подсветки состояния
 * «готов принять», а при отпускании кнопки — передаёт элемент слушателю
 * через onDrop (см. IDragListener).
 */
public interface IDropTarget {

	/** true, пока активный жест может быть сброшен на этот таргет (подсветка). */
	void setDropActive(boolean active);

	boolean isDropActive();
}
