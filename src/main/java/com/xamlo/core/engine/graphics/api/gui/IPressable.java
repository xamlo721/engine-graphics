package com.xamlo.core.engine.graphics.api.gui;


/**
 * Интерфейс для элементов, поддерживающих состояние нажатия
 */
public interface IPressable {
	
    /**
     * Устанавливает состояние нажатия элемента
     * @param pressed true - элемент нажат, false - элемент не нажат
     */
    void setPressed(boolean pressed);
    
    /**
     * Проверяет состояние нажатия элемента
     * @return true - элемент нажат, false - элемент не нажат
     */
    boolean isPressed();
    
}
