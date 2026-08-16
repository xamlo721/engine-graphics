package com.xamlo.core.engine.graphics.api.gui;

import java.util.List;

import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;

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

	/**
	 * Индекс слоя (z-порядок) элемента. Элемент с большим индексом рисуется поверх
	 * элементов с меньшим; при равных индексах сохраняется порядок дерева.
	 * Эффективный индекс учитывается вместе с родителями — см. {@link ZOrder}.
	 */
	default int getZIndex() {
		return 0;
	}

	// Устанавливает индекс слоя элемента
	default void setZIndex(int zIndex) {
	}

	/**
	 * Абсолютная X-координата элемента в экранных координатах:
	 * сумма смещений по цепочке родителей плюс локальная координата самого элемента.
	 */
	default float getAbsX() {
		float x = hasParent() ? getParent().getAbsX() : 0f;
		if (this instanceof IResizable) {
			UIElementGeometry geometry = ((IResizable) this).getGeometry();
			if (geometry != null) {
				x += geometry.getXCoord();
			}
		}
		return x;
	}

	/**
	 * Абсолютная Y-координата элемента в экранных координатах.
	 */
	default float getAbsY() {
		float y = hasParent() ? getParent().getAbsY() : 0f;
		if (this instanceof IResizable) {
			UIElementGeometry geometry = ((IResizable) this).getGeometry();
			if (geometry != null) {
				y += geometry.getYCoord();
			}
		}
		return y;
	}

}
