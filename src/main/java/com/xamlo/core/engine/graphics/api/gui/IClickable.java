package com.xamlo.core.engine.graphics.api.gui;

public interface IClickable {

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
    
    /**
     * Устанавливает обработчик клика по элементу.
     * 
     * @param listener Реализация интерфейса обработчика (может быть null)
     */
    void setClickListener(IClickListener listener);

    /**
     * Возвращает обработчик клика по элементу.
     * 
     * @return Текущий обработчик клика или null, если не установлен
     */
    IClickListener getClickListener();
    
}
