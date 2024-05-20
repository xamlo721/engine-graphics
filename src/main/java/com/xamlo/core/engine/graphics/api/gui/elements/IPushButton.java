package com.xamlo.core.engine.graphics.api.gui.elements;

import com.xamlo.core.engine.graphics.api.gui.IClickable;
import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.IHoverable;
import com.xamlo.core.engine.graphics.api.gui.IIconSupport;
import com.xamlo.core.engine.graphics.api.gui.IResizable;

/**
 * Интерфейс для реализации кнопок с различными состояниями и стилями.
 * Предоставляет методы для управления визуальным представлением и поведением кнопки.
 */
public interface IPushButton extends ILabel, 
									 IHoverable,
									 IIconSupport,
									 IClickable,
									 IResizable {

    /**
     * Устанавливает основной цвет кнопки.
     * 
     * @param color Цвет кнопки в нормальном состоянии
     */
    void setButtonColor(IColor color);

    /**
     * Возвращает основной цвет кнопки.
     * 
     * @return Цвет кнопки в нормальном состоянии
     */
    IColor getButtonColor();

    /**
     * Устанавливает цвет кнопки при нажатии.
     * 
     * @param color Цвет кнопки в нажатом состоянии
     */
    void setButtonPressedColor(IColor color);

    /**
     * Возвращает цвет кнопки при нажатии.
     * 
     * @return Цвет кнопки в нажатом состоянии
     */
    IColor getButtonPressedColor();

    
}