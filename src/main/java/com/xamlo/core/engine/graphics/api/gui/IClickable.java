package com.xamlo.core.engine.graphics.api.gui;

public interface IClickable {

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
