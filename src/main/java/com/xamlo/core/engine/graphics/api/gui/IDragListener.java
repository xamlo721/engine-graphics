package com.xamlo.core.engine.graphics.api.gui;

/**
 * Слушатель жеста перетаскивания элемента {@link IDraggable}. Вызывается
 * диспетчером событий ввода на каждом этапе жеста. Координаты — в системе
 * координат сцены/GUI. Методы таргетов вызваны только если под курсором при
 * соответствующем событии находится элемент-таргет {@link IDropTarget}; иначе
 * onDragEnd приходит без таргета и drop не происходит.
 */
public interface IDragListener {

	/** Жест стал перетаскиванием: кнопка удерживается и курсор преодолел порог. */
	void onDragStart(IDraggable element, float xCoord, float yCoord);

	/** Перетаскиваемый элемент движется вместе с курсором. dx/dy — сдвиг за кадр. */
	void onDrag(IDraggable element, float xCoord, float yCoord, float deltaX, float deltaY);

	/** Курсор во время перетаскивания попал на новый таргет (подсветить/принять). */
	void onDragEnterTarget(IDraggable element, IDropTarget target);

	/** Курсор покинул текущий таргет во время перетаскивания. */
	void onDragLeaveTarget(IDraggable element, IDropTarget target);

	/** Кнопка отпущена над таргетом — дроп состоялся. */
	void onDrop(IDraggable element, IDropTarget target, float xCoord, float yCoord);

	/** Кнопка отпущена вне любого таргета — жест сброшен без эффекта. */
	void onDragEnd(IDraggable element, float xCoord, float yCoord);
}
