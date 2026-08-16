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

}
