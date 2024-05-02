package com.xamlo.core.engine.graphics.api.components;

import java.util.List;

import org.joml.Matrix4f;

import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;

/*
 * Интерфейс, описывающий правила в заимодействия с понятием "Сцена"
 * Сцена, это набор графических элементов (возможно сложная структура списка)
 * которую необходимо сначала подгрузить в память, потом выгрузить
 * для рисования пока существует метод renderFrame(), позволяющий
 * отдать на откуп наследникам IScene всю логику по отрисовке объектов
 * Выглядит не безопасно, но пусть пока будет так
 * @author Satomi
 */
public interface IScene {
	
	void load();

	List<AbstractSceneElement> getRenderableObject();
	
	public IUIElement findElementAt(float xCoord, float yCoord);

	List<IUIElement> getGuiElements();
	
	//TODO: Есть мнение, что сцена это совокупность объектов, которую могут рисовать
	// всякие Renderer, они и определяют её положение в мире, а она сама не знает где она расположена
	// Пока не уверен как правильно - сделаем World + несколько Renderer, тогда и разберемся
	Matrix4f getProjectionMatrix();
	
	void tranformScene(Matrix4f transformMatrix);
	
	void unload();
	
}
