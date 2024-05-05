package com.xamlo.core.engine.graphics.api.gui;

/**
 * Интерфейс для элементов, поддерживающих состояние наведения
 */
public interface IHoverable {
	
    /**
     * Устанавливает состояние наведения элемента
     * @param hovered true - курсор над элементом, false - курсор вне элемента
     */
    void setHovered(boolean hovered);
    
    /**
     * Проверяет состояние наведения элемента
     * @return true - курсор над элементом, false - курсор вне элемента
     */
    boolean isHovered();
    
    /**
     * Устанавливает цвет элемент при наведении курсора.
     * 
     * @param color Цвет элемента в состоянии hover
     */
    void setHoverColor(IColor color);

    /**
     * Возвращает цвет элемента при наведении курсора.
     * 
     * @return Цвет элемента в состоянии hover
     */
    IColor getHoverColor();
    
}