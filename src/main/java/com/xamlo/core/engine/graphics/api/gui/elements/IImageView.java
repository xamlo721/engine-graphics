package com.xamlo.core.engine.graphics.api.gui.elements;

import com.xamlo.core.engine.graphics.components.AbstractTexture;
import com.xamlo.core.engine.graphics.components.gui.EnumImageStretch;

/**
 * Интерфейс для элементов, отображающих текстуру внутри собственных границ.
 */
public interface IImageView extends IWidget {

    void setImage(AbstractTexture image);

    AbstractTexture getImage();

    void setStretchMode(EnumImageStretch mode);

    EnumImageStretch getStretchMode();

    /**
     * Устанавливает изображение и его режим растяжения; null-режим текущий не меняет.
     * Дефолтная реализация делегирует setImage/setStretchMode, поэтому даже реализации
     * вне {@code Widget} получают осмысленное поведение без правок.
     */
    default void setBackgroundImage(AbstractTexture image, EnumImageStretch mode) {
        setImage(image);
        if (mode != null) {
            setStretchMode(mode);
        }
    }

    /** Пиксели бордов nine-patch по сторонам изображения (левый, верхний, правый, нижний). */
    default void setNinePatchInsets(int left, int top, int right, int bottom) {
    }

}
