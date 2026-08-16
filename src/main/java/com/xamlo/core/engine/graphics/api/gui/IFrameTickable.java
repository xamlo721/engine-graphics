package com.xamlo.core.engine.graphics.api.gui;

/**
 * Элемент, которому контроллер сцены шлёт тик каждый кадр,
 * пока он в фокусе (например, для мигания caret в текстовом поле).
 */
public interface IFrameTickable {

    void onFrame();

}
