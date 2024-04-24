package com.xamlo.core.engine.graphics.api.components.scene;

import org.joml.Vector3f;

public interface IRotatable {

	/**
	 * Повернуть объект на вектор
	 * относительно текущего положения
	 * [0] - yaw
	 * [1] - pitch
	 * [2] - roll
	 */
	public void rotate(Vector3f vector);

	/**
	 * Повернуть объект на вектор
	 * относительно текущего положения
	 * [0] - yaw
	 * [1] - pitch
	 * [2] - roll
	 */
	public void rotate(float yaw, float pitch, float roll);
	
	/**
	 * Установить положение объекта согласно вектору 
	 * [0] - yaw
	 * [1] - pitch
	 * [2] - roll
	 */
	public void setRotation(Vector3f rot);

	/**
	 * Установить положение объекта согласно вектору 
	 * [0] - yaw
	 * [1] - pitch
	 * [2] - roll
	 */
	public void setRotation(float yaw, float pitch, float roll);
	

	/**
	 * Получение текущего угла поворота камеры
	 * [0] - yaw
	 * [1] - pitch
	 * [2] - roll
	 */
	public Vector3f getRotation();
	
	
}
