package com.xamlo.core.engine.graphics.api.gui;

/**
 * Элемент с всплывающей подсказкой: при наведении курсора и удержании без
 * движения контроллер сцены показывает текст рядом с указателем (как в MC).
 * Пустой/отсутствующий текст означает «подсказки нет».
 */
public interface ITooltipSupport {

    default String getToolTipText() {
        return "";
    }

    default void setToolTipText(String text) {
    }

}
