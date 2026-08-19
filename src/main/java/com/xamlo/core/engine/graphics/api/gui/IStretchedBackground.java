package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.components.gui.EnumImageStretch;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout.NinePatchInsets;

/**
 * Элемент с фоновым изображением, для которого режим растяжения настраивается
 * сверх исторического FILL (FIT / CENTER / NINEPATCH, E3). Все методы — default'ы,
 * поэтому существующие реализации {@link IBackgroundSupport} компилируются и ведут
 * себя без изменений (режим остаётся FILL, борды нулевые).
 */
public interface IStretchedBackground extends IBackgroundSupport {

    /** Режим растяжения фонового изображения; null трактуется как FILL. */
    default void setBackgroundStretchMode(EnumImageStretch mode) {
    }

    default EnumImageStretch getBackgroundStretchMode() {
        return EnumImageStretch.FILL;
    }

    /** Пиксели бордов nine-patch по сторонам фонового изображения (см. NinePatchInsets). */
    default void setNinePatchInsets(int left, int top, int right, int bottom) {
    }

    default NinePatchInsets getNinePatchInsets() {
        return NinePatchInsets.NONE;
    }

}
