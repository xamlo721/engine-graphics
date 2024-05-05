package com.xamlo.core.engine.graphics.api.gui;

public interface IActivatable {

    /**
     * Устанавливает доступность элемента для взаимодействия.
     * 
     * @param enabled true - элемент активен, false - элемент неактивен
     */
    void setEnabled(boolean enabled);

    /**
     * Проверяет, доступен ли элемент для взаимодействия.
     * 
     * @return true - элемент активнен, false - элемент неактивен
     */
    boolean isEnabled();

    /**
     * Устанавливает цвет элемента в неактивном состоянии.
     * 
     * @param color Цвет элемента при отключении
     */
    void setDisabledColor(IColor color);

    /**
     * Возвращает цвет элемента в неактивном состоянии.
     * 
     * @return Цвет элемента при отключении
     */
    IColor getDisabledColor();

}
