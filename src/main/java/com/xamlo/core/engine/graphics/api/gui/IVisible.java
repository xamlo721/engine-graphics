package com.xamlo.core.engine.graphics.api.gui;

public interface IVisible {

    /**
     * Скрывает элемента, делая его невидимым на экране
     */
	public void hide();
	
    /**
     *  Показывает элемент, делая его видимым на экране
     */
	public void show();

    /**
     * Устанавливает видимость элемента.
     * 
     * @param visible true - элемент видим, false - элемент скрыт
     */
    void setVisible(boolean visible);

    /**
     * Проверяет, видим ли элемент.
     * 
     * @return true - элемент видима, false - элемент скрыт
     */
    boolean isVisible();
}
