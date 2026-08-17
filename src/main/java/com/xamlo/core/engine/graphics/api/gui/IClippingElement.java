package com.xamlo.core.engine.graphics.api.gui;

/**
 * Элемент, обрезающий отрисовку и hit-test своих потомков по собственным
 * границам (см. com.xamlo.core.engine.graphics.components.gui.ClipContexts).
 * По умолчанию не обрезает ничего.
 */
public interface IClippingElement {

    default boolean clipsChildren() {
        return false;
    }

}
