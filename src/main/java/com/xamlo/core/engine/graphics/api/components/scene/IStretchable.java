package com.xamlo.core.engine.graphics.api.components.scene;

import org.joml.Vector3f;

public interface IStretchable {

	/**
	 * Растянуть геометрию объекта по указанным коэффициентам
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
	public void expandGeometry(Vector3f geometry);
	
	/**
	 * Растянуть геометрию объекта по указанным коэффициентам
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
	public void expandGeometry(float xCoord, float yCoord, float zCoord);
	
	/**
	 * Установить растяжение геометрии объекта по указанным коэффициентам
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
	public void setExpandGeometry(Vector3f geometry);
	
	/**
	 * Установить растяжение геометрии объекта по указанным коэффициентам
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
	public void setExpandGeometry(float xCoord, float yCoord, float zCoord);
	
	/**
	 * Восстанавливает геометрию объекта в исходное состояние
	 */
	public void resetGeometry();
	
}
