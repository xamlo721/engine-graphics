package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.components.gui.EnumOrientation;

/**
 * Маркерный контракт для элементов, рисующих «заполнение» внутри собственных границ
 * (progress bar, слайдер и т.п.). Рендерер после фона элемента отрисовывает второй
 * уменьшенный квад вдоль getFillDirection() на величину getFillFraction().
 */
public interface IFillIndicator {

    /** Доля заполненной области, 0..1 (вне диапазона значение обрезается). */
    float getFillFraction();

    /** Цвет заполнения; null — заполнение не рисуется. */
    IColor getFillColor();

    default EnumOrientation getFillDirection() {
        return EnumOrientation.HORIZONTAL;
    }

}
