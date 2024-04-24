package com.xamlo.core.engine.graphics.api.components;

import org.joml.Vector3f;
import org.joml.Matrix4f;

/*
 * Интерфейс для описания камер
 * Позозреваю, что камеры будут разные в проекте и надо будет эксперементировать
 * с их вариантами реализации
 */
public interface ICamera {
	
	/*
	 * Матрица вида для камеры
	 */
	public Matrix4f getViewMatrix();

	/**
	 * Повернуть камеру на вектор 
	 * [0] - yaw
	 * [1] - pitch
	 * [2] - roll
	 */
	public void rotate(Vector3f vector);
	
	/**
	 * Подвинуть камеру
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
	public void move(Vector3f vector);
	
	/**
	 * Получение текущей позиции камеры
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
	public Vector3f getPosition();
	/**
	 * Получение текущего угла поворота камеры
	 * [0] - yaw
	 * [1] - pitch
	 * [2] - roll
	 */
	public Vector3f getRotation();
	
	/**
	 * Получение угла обора (Fiel Of Vief) в градусах
	 * Скорее всего надо будет перевести в радианы, 
	 * но пока градусы
	 */
	public float getFov();
	
	/**
	 * Устанавливает угол обзора камеры в градусах
	 * Скорее всего надо будет перевести в радианы, 
	 * но пока градусы.
	 * Да и вообще, кому надо - тот и переведёт!
	 */
	public void setFov(float fov);
	
	/**
	 *  Возвращает соотношение экрана дробью
	 *  например 3:4 для 1200х900
	 *  или 16:9 для 1920х1080
	 */
	public float getAspectRatio();
	
	/**
	/**
	 *  Устанавливает соотношение сторон поля видимости камеры
	 *  Для этого нужно передать Ширину и Длину прямоугольника
	 */
	public void setAspectRatio(int width, int height);
	
	/**
	 * Получить расстояние до ближней точки отсечения
	 */
	public float getNearDistance();
	
	/**
	 * Установить ближнее расстояние отсечения
	 */
	public void setNearDistance(float far);
	
	/**
	 * Получить расстояние до дальней точки отсечения
	 */
	public float getFarDistance();
	
	/**
	 * Установить расстояние отсечения
	 */
	public void setFarDistance(float near);

}
