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

    /** Режим пароля: отрисовка заменяет каждый символ маской (реальный текст не меняется). */
    void setPasswordMode(boolean enabled);

    boolean isPasswordMode();

    /** Символ маски в режиме пароля (по умолчанию «•»). */
    void setMaskCharacter(char maskChar);

    char getMaskCharacter();

    /** Текущее горизонтальное смещение длинной строки в пикселях (0..максимум). */
    float getHorizontalScroll();

    /** Устанавливает прокрутку; клампится к [0, максимум по ширине содержимого]. */
    void setHorizontalScroll(float pixels);

}
