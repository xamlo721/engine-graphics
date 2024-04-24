package com.xamlo.core.engine.graphics.api.components.scene;

import org.joml.Vector3f;

public interface IMovable {

	/**
	 * Подвинуть объект на вектор
	 * относительно текущего расположения
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
	public void move(Vector3f vector);
	
	/**
	 * Подвинуть объект на вектор
	 * относительно текущего расположения
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
	public void move(float xCoord, float yCoord, float zCoord);
	
	/**
	 * Установить объект на позицию
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
	public void setPosition(Vector3f pos);
	
	/**
	 * Получение текущей позиции камеры
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
	public Vector3f getPosition();
	
	
	/**
	 * Установить объект на позицию
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
	public void setPosition(float xCoord, float yCoord, float zCoord);
	

	
}
