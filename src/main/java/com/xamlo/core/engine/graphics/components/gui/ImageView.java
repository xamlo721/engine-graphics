package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.elements.IImageView;
import com.xamlo.core.engine.graphics.components.AbstractTexture;

/**
 * Отображение текстуры в пределах геометрии элемента. Все режимы растяжения из
 * {@link EnumImageStretch} учитываются при рендеринге (FILL — исторический путь
 * unit-mesh'а, остальные — CPU-side набор квадов с собственными UV); nine-patch
 * настраивается бордами через setNinePatchInsets. Состояние режима синхронизировано
 * со storage уровня виджета ({@code setBackgroundStretchMode}), который читает
 * UIElementRenderer для любого текстурированного фона.
 */
public class ImageView extends Widget implements IImageView {

    protected AbstractTexture image;
    protected EnumImageStretch stretchMode;

    public ImageView() {
        super();
        this.image = null;
        this.stretchMode = EnumImageStretch.FILL;
        this.backgroundColor = new Color(0, 0, 0, 96);
    }

    @Override
    public void setImage(AbstractTexture image) {
        this.image = image;
        setBackgroundImage(image);
    }

    @Override
    public AbstractTexture getImage() {
        return this.image != null ? this.image : getBackgroundImage();
    }

    /** Режим растяжения изображения и состояния фона виджета (общий источник истины). */
    @Override
    public void setStretchMode(EnumImageStretch mode) {
        this.stretchMode = mode == null ? EnumImageStretch.FILL : mode;
        super.setBackgroundStretchMode(this.stretchMode);
    }

    @Override
    public EnumImageStretch getStretchMode() {
        return this.stretchMode;
    }

    /** Виджетный уровень задаёт режим напрямую — локальное зеркало остаётся в синхроне. */
    @Override
    public void setBackgroundStretchMode(EnumImageStretch mode) {
        super.setBackgroundStretchMode(mode);
        this.stretchMode = (mode == null) ? EnumImageStretch.FILL : mode;
    }

    @Override
    public EnumImageStretch getBackgroundStretchMode() {
        return getStretchMode();
    }

    /** Борды nine-patch для режима NINEPATCH (см. {@link ImageStretchLayout}). */
    @Override
    public void setNinePatchInsets(int left, int top, int right, int bottom) {
        super.setNinePatchInsets(left, top, right, bottom);
    }

}
