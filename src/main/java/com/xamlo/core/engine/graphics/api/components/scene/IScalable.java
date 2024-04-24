package com.xamlo.core.engine.graphics.api.components.scene;

public interface IScalable {

	/**
	 * Изменить размер объекта в scale раз
	 */
	public void scale(float scaleIndex);
	
	/**
	 * Установить размер объекта
	 */
	public void setScale(float scaleIndex);
	
	/**
	 * Получить текущий масштаб
	 */
	public float getScale();
	
}
