package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.api.gui.ITextureHandle;

public interface IBackgroundSupport {

    // Проверяет, есть ли у виджета фоновое изображение
	public boolean hasBackgroundImage();
	
    // Возвращает фоновое изображение виджета
	public ITextureHandle getBackgroundImage();
	
    // Устанавливает фоновое изображение виджета
	public void setBackgroundImage(ITextureHandle image);

    // Устанавливает цвет фона виджета
    public void setBackgroundColor(IColor color);

    // Возвращает цвет фона виджета
    public IColor getBackgroundColor();

}
