package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.api.gui.IDragListener;

/**
 * UI-элемент, который можно перетаскивать мышью. Поддерживается диспетчером
 * событий ввода: при удержании кнопки и движении курсора с порога срабатывания
 * вызывается установленный {@link #getDragListener() слушатель}.
 */
public interface IDraggable {

	/** Устанавливает обработчик жеста (может быть null для отказа от drag-and-drop). */
	void setDragListener(IDragListener listener);

	IDragListener getDragListener();
}
