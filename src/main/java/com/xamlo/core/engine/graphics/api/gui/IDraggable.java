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

	/**
	 * Типизированные данные жеста (E4): источник задаёт их до или во время старта
	 * перетаскивания; таргет получает ссылку через
	 * {@link IDropTarget#acceptsDrag(DragPayload)} и слушатель читает тем же getter'ом
	 * в onDrop/onDragEnd. Движок содержимое не интерпретирует. Payload живёт на
	 * элементе до явной замены/очистки: автоматического сброса при завершении жеста
	 * нет, потому что колбэки читают его лениво после END-события. Состояние хранится
	 * вне элемента ({@link DragStateRegistry}), поэтому все существующие реализации
	 * компилируются без изменений.
	 *
	 * @param payload значение payload'а; null очищает ранее заданное.
	 */
	default void setDragPayload(DragPayload payload) {
		DragStateRegistry.storePayload(this, payload);
	}

	/** Текущий payload элемента (null, если не задан). */
	default DragPayload getDragPayload() {
		return DragStateRegistry.payloadOf(this);
	}

	/**
	 * Включён ли у элемента «призрак» — визуальная копия за курсором во время
	 * перетаскивания (E4). По умолчанию выключен: поведение существующих виджетов
	 * без явного согласия не меняется, а жесты управления (слайдер, скроллбар)
	 * остаются без дублирующей копии. Источники drag-and-drop включают флаг вместе с
	 * задачей {@link #getDragPayload() payload'а}.
	 */
	default boolean isGhostPreviewEnabled() {
		return DragStateRegistry.ghostPreviewEnabled(this);
	}

	default void setGhostPreviewEnabled(boolean enabled) {
		DragStateRegistry.storeGhostPreview(this, enabled);
	}

	/**
	 * Создаёт «визуальный снимок» элемента для ghost-preview жеста (E4): копию,
	 * неотличимую по виду от оригинала в момент старта, но неучастную в hit-test'е
	 * и несущую ни слушателей, ни фокус/drag-состояние. Диспетчер сам позиционирует
	 * возвращённый элемент за курсором до DragEnd. По умолчанию — null (копии нет),
	 * каталогные виджеты переопределяют метод через базовый Widget.
	 */
	default IUIElement createDragGhost() {
		return null;
	}

}
