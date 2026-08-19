package com.xamlo.core.engine.graphics.api.gui;

/**
 * Типизированные данные жеста drag-and-drop (E4). Держит произвольный объект,
 * который источник жеста задаёт через {@link IDraggable#setDragPayload(DragPayload)},
 * а таргет получает в {@link IDropTarget#acceptsDrag(DragPayload)} для проверки
 * «можно ли принять» до подсветки и дропа.
 *
 * <p>Движок намеренно не интерпретирует содержимое: тип значения определяет
 * пользовательский контракт приложения (например, идентификатор перемещаемого
 * объекта или его описание), движок лишь передаёт ссылку от источника к цели
 * без копирования.</p>
 */
public final class DragPayload {

	private Object value;

	public DragPayload() {
	}

	public DragPayload(Object value) {
		this.value = value;
	}

	/** Фабрика: payload с уже готовым значением. */
	public static DragPayload of(Object value) {
		return new DragPayload(value);
	}

	/** Значение payload'а (null, если ещё не задано). */
	public Object getValue() {
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
