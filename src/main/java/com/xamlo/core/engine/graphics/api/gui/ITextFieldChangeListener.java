package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.api.gui.elements.ITextField;

/**
 * Слушатель изменения текста текстового поля. Вызывается только при
 * пользовательском вводе (символы, Backspace, Delete, удаление выделения);
 * программный setText() событие не генерирует.
 */
public interface ITextFieldChangeListener {

    void onTextChanged(ITextField field, String newText);

}
