package com.xamlo.core.engine.graphics.api.gui;

/**
 * Элемент, поддерживающий выделение перетаскиванием мыши (текстовое поле).
 * Контроллер сцены отслеживает жест press→drag→release левой кнопкой и
 * маршрутизирует его сюда. Координаты — экранные (как в событиях мыши).
 */
public interface ITextSelectionHandler {

    /** ЛКМ нажата на элементе: начать выделение (якорь под курсором). */
    void onSelectionStart(float x, float y);

    /** ЛКМ удерживается, курсор двигается: растянуть выделение до курсора. */
    void onSelectionDrag(float x, float y);

    /** ЛКМ отпущена: завершить жест (выделение остаётся как есть). */
    void onSelectionEnd(float x, float y);

}
