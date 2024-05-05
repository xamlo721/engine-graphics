package com.xamlo.core.engine.graphics.api.gui;

import java.util.List;

public interface IUIElement {

    // Устанавливает позицию виджета по координатам x и y
    public void setPosition(int xCoord, int yCoord);

    /**
     * Проверяет, содержит ли виджет указанную точку
     * @param x Координата X точки
     * @param y Координата Y точки
     * @return true, если точка находится внутри виджета
     */
    public boolean containsPoint(float x, float y);

    // Устанавливает родительский виджет
	public void setParent(IUIElement parent);
	
	// Проверяет, есть ли родитель у этого виджета
	public boolean hasParent();
	
    // Возвращает родительский виджет
	public IUIElement getParent();
	
	// Возвращает имя виджета
	public void setWidgetName(String widgetName);
	
	// Устанавливает имя виджета
	public String getWidgetName();
	
    // Возвращает список дочерних виджетов данного виджета
	public List<IUIElement> getChildElements();
	
	// Добавляет в список дочерних виджетов
	public void addChild(IUIElement child);
	
    // Закрывает виджет и освобождает ресурсы, связанные с ним
	public void free();

}
