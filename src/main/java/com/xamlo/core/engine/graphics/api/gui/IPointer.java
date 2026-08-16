package com.xamlo.core.engine.graphics.api.gui;

/**
 * Элемент, принимающий обработчик указателя (клики с координатами).
 */
public interface IPointer {

    /**
     * Устанавливает обработчик указателя.
     * @param listener реализация обработчика (может быть null)
     */
    void setPointerListener(IPointerListener listener);

    /**
     * Возвращает обработчик указателя.
     * @return текущий обработчик или null, если не установлен
     */
    IPointerListener getPointerListener();

}
