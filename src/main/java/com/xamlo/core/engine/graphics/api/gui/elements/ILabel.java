package com.xamlo.core.engine.graphics.api.gui.elements;

import java.util.Collections;
import java.util.List;

import com.xamlo.core.engine.graphics.api.gui.IActivatable;
import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.TextLineSpec;
import com.xamlo.core.engine.graphics.api.gui.font.IFont;
import com.xamlo.core.engine.graphics.api.gui.font.IFontSupport;

public interface ILabel extends IWidget, 
								IFontSupport,
								IActivatable {

    void setText(String text);

    String getText();

    /**
     * Строка для отрисовки (по умолчанию совпадает с {@link #getText()}).
     * Поле ввода в режиме пароля возвращает здесь маску, а реальный текст —
     * через обычный getter.
     */
    default String getDisplayText() {
        return getText();
    }

    /** Смещение прокрученного содержимого влево в пикселях (0 — без скролла). */
    default float getHorizontalScroll() {
        return 0f;
    }

    void setTextColor(IColor color);

    IColor getTextColor();

    void setFont(IFont font);

    IFont getFont();

    /**
     * Стилизированные строки многострочного содержимого. Пустой список означает,
     * что элемент рисуется обычным однострочным путём через {@link #getDisplayText()}.
     */
    default List<TextLineSpec> getStyledLines() {
        return Collections.emptyList();
    }

}