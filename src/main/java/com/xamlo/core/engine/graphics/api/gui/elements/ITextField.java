package com.xamlo.core.engine.graphics.api.gui.elements;

import com.xamlo.core.engine.graphics.api.gui.ITextFieldChangeListener;

/**
 * Однострочное текстовое поле с вводом: заголовочный текст, caret, выделение,
 * placeholder, ограничение длины и слушатель изменения.
 */
public interface ITextField extends ILabel {

    /** Значение по умолчанию для maxLength (как в MC). */
    int DEFAULT_MAX_LENGTH = 512;

    void setMaxLength(int maxLength);

    int getMaxLength();

    void setPlaceholder(String placeholder);

    String getPlaceholder();

    int getCaretPosition();

    void setCaretPosition(int position);

    /** Начало выделения (индекс символа), -1 если выделения нет. */
    int getSelectionStart();

    /** Конец выделения (индекс символа), -1 если выделения нет. */
    int getSelectionEnd();

    void setSelection(int start, int end);

    void selectAll();

    String getSelectedText();

    void setChangeListener(ITextFieldChangeListener listener);

}
