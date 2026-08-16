package com.xamlo.core.engine.graphics.api.gui;

/**
 * Обработчик указателя: получает клики с абсолютными экранными координатами
 * (в отличие от {@link IClickListener}, который координат не несёт).
 * Используется, например, для постановки caret в текстовом поле по месту клика.
 */
public interface IPointerListener {

    /**
     * @param element элемент, по которому кликнули
     * @param x абсолютная экранная координата X клика
     * @param y абсолютная экранная координата Y клика
     */
    void onPointer(IUIElement element, float x, float y);

}
