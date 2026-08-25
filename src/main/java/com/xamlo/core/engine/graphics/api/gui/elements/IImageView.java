package com.xamlo.core.engine.graphics.api.gui.elements;

import com.xamlo.core.engine.graphics.api.gui.ITextureHandle;
import com.xamlo.core.engine.graphics.components.gui.EnumImageStretch;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout.StretchAnchor;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout.TextureRegion;

/**
 * Интерфейс для элементов, отображающих текстуру внутри собственных границ.
 */
public interface IImageView extends IWidget {

    void setImage(ITextureHandle image);

    ITextureHandle getImage();

    void setStretchMode(EnumImageStretch mode);

    EnumImageStretch getStretchMode();

    /**
     * Устанавливает изображение и его режим растяжения; null-режим текущий не меняет.
     * Дефолтная реализация делегирует setImage/setStretchMode, поэтому даже реализации
     * вне {@code Widget} получают осмысленное поведение без правок.
     */
    default void setBackgroundImage(ITextureHandle image, EnumImageStretch mode) {
        setImage(image);
        if (mode != null) {
            setStretchMode(mode);
        }
    }

    /** Пиксели бордов nine-patch по сторонам изображения (левый, верхний, правый, нижний). */
    default void setNinePatchInsets(int left, int top, int right, int bottom) {
    }

    /**
     * Подрегион исходной текстуры в пикселях от её левого верхнего угла ({@link TextureRegion}).
     * Нулевые или отрицательные значения означают «вся текстура» по соответствующей оси; дефолт — {@code FULL}.
     * Правило композиции: кроп применяется ПЕРВЫМ — до маппинга режима растяжения и до борд nine-patch
     * (борды при режиме NINEPATCH интерпретируются относительно вырезанного фрагмента); для FILL/FIT/CENTER
     * подрегион ведёт себя как самостоятельное изображение своих размеров. Режим FILL с неполным регионом
     * рисуется через CPU-side генерацию квадов с собственными UV (как остальные режимы), без изменения
     * исторического unit-mesh пути и шейдера элемента.
     */
    default void setRegion(int uPx, int vPx, int wPx, int hPx) {
    }

    default TextureRegion getRegion() {
        return TextureRegion.FULL;
    }

    /**
     * Якорь обрезания/выравнивания изображения в координатах [0..1]² (см. {@link StretchAnchor}):
     * действует на COVER/CENTER/FIT, дефолт — центр; хранение — на уровне виджета фона.
     */
    default void setCropAnchor(float x, float y) {
    }

    default StretchAnchor getCropAnchor() {
        return StretchAnchor.CENTER;
    }

}
