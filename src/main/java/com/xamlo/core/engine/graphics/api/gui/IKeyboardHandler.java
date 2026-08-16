package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.engine.api.devices.EnumKeyboardButtons;

/**
 * Элемент, принимающий клавиатурный ввод, пока находится в фокусе
 * (маршрутизацию по фокусу делает контроллер сцены).
 */
public interface IKeyboardHandler {

    /**
     * Нажата клавиша (edge-событие).
     * @param key нажатая клавиша
     * @param ctrl удерживается ли Ctrl в момент нажатия
     * @param shift удерживается ли Shift в момент нажатия
     */
    void onKeyPressed(EnumKeyboardButtons key, boolean ctrl, boolean shift);

    /**
     * Набран символ (включая Unicode, из GLFW char-callback'а).
     * @param c набранный символ
     */
    void onCharTyped(char c);

}
