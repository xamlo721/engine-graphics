package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.elements.IImageView;
import com.xamlo.core.engine.graphics.components.AbstractTexture;

/**
 * Отображение текстуры в пределах геометрии элемента. Рендеринг идёт по стандартному
 * каналу фона (IBackgroundSupport), поэтому режим FILL работает сразу; FIT/CENTER
 * будут поддерживаться после доработки UV в шейдере GUI-элементов.
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

    @Override
    public void setStretchMode(EnumImageStretch mode) {
        this.stretchMode = mode == null ? EnumImageStretch.FILL : mode;
    }

    @Override
    public EnumImageStretch getStretchMode() {
        return this.stretchMode;
    }

}
