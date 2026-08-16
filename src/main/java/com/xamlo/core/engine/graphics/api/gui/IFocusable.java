package com.xamlo.core.engine.graphics.api.gui;

/**
 * Интерфейс для элементов, которые могут получать фокус
 */
public interface IFocusable {
	
    /**
     * Устанавливает состояние фокуса элемента
     * @param focused true - элемент в фокусе, false - элемент не в фокусе
     */
    void setFocused(boolean focused);
    
    /**
     * Проверяет состояние фокуса элемента
     * @return true - элемент в фокусе, false - элемент не в фокусе
     */
    boolean isFocused();

    /**
     * Может ли элемент принимать фокус (по клику или программно)
     * @return true - элемент фокусируемый
     */
    boolean isFocusable();

    /**
     * Устанавливает, может ли элемент принимать фокус
     * @param focusable true - элемент фокусируемый
     */
    void setFocusable(boolean focusable);

}
