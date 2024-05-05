package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.components.AbstractTexture;

public interface IBackgroundSupport {

    // Проверяет, есть ли у виджета фоновое изображение
	public boolean hasBackgroundImage();
	
    // Возвращает фоновое изображение виджета
	public AbstractTexture getBackgroundImage();
	
    // Устанавливает фоновое изображение виджета
	public void setBackgroundImage(AbstractTexture image);

    // Устанавливает цвет фона виджета
    public void setBackgroundColor(IColor color);

    // Возвращает цвет фона виджета
    public IColor getBackgroundColor();

}
