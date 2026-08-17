package com.xamlo.core.engine.graphics.api.gui;

/**
 * Элемент, реагирующий на прокрутку колесом мыши над собой (или своими
 * потомками): контроллер сцены делает hit-test по координатам события и
 * маршрутизирует дельту ближайшему предку-таргету.
 */
public interface IWheelTarget {

    /** dx/dy — накопленная за кадр дельта колеса (GLFW: dy > 0 — вверх). */
    void onScrolled(float deltaX, float deltaY);

}
