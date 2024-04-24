package com.xamlo.core.engine.graphics.api.gui;

import java.util.List;

import com.xamlo.core.engine.graphics.components.AbstractTexture;
import com.xamlo.core.engine.graphics.components.gui.Border;
import com.xamlo.core.engine.graphics.components.gui.WidgetGeometry;
import com.xamlo.core.engine.graphics.components.gui.WidgetSize;

public interface IWidget {
	
    // Устанавливает родительский виджет
	public void setParent(IWidget parent);
	
	// Проверяет, есть ли родитель у этого виджета
	public boolean hasParent();
	
    // Возвращает родительский виджет
	public IWidget getParent();
	
	// Возвращает имя виджета
	public void setWidgetName(String widgetName);
	
	// Устанавливает имя виджета
	public String getWidgetName();
	
    // Возвращает геометрию (положение и размер) виджета
	public WidgetGeometry getWidgetGeometry();
	
    // Возвращает размер виджета
	public WidgetSize getWidSize();
	
    // Изменяет размер и положение виджета согласно указанной геометрии
	public void resize(WidgetGeometry geometry);
	
    // Изменяет размер виджета согласно указанному размеру
	public void resize(WidgetSize size);

    // Скрывает виджет, делая его невидимым на экране
	public void hide();
	
    // Показывает виджет, делая его видимым на экране
	public void show();
	
    // Устанавливает видимость виджета в зависимости от переданного значения
	public void setVisible(boolean visible);

    // Закрывает виджет и освобождает ресурсы, связанные с ним
	public void close();
	
    // Возвращает список дочерних виджетов данного виджета
	public List<IWidget> getChildElements();
	
	// Добавляет в список дочерних виджетов
	public void addChild(IWidget child);
	
    // Проверяет, есть ли у виджета фоновое изображение
	public boolean hasBackgroundImage();
	
    // Возвращает фоновое изображение виджета
	public AbstractTexture getBackgroundImage();
	
    // Устанавливает фоновое изображение виджета
	public void setBackgroundImage(AbstractTexture image);

    // Устанавливает позицию виджета по координатам x и y
    public void setPosition(int x, int y);

    // Устанавливает доступность виджета для пользовательского взаимодействия
    public void setEnabled(boolean enabled);

    // Возвращает true, если виджет доступен для пользовательского взаимодействия, иначе false
    public boolean isEnabled();

    // Устанавливает цвет фона виджета
    public void setBackgroundColor(IColor color);

    // Возвращает цвет фона виджета
    public IColor getBackgroundColor();

    // Устанавливает шрифт для текстовых элементов виджета
    public void setFont(IFont font);

    // Возвращает шрифт, используемый в виджете
    public IFont getFont();

    // Устанавливает всплывающую подсказку для виджета
    public void setToolTipText(String tooltip);

    // Возвращает текст всплывающей подсказки для виджета
    public String getToolTipText();

    // Устанавливает границу для виджета
    public void setBorder(Border border);

    // Устанавливает границу для виджета
    public void setBorder(int borderSize);

    // Возвращает границу виджета
    public Border getBorder();

    // Устанавливает возможность фокусировки на виджете
    public void setFocusable(boolean focusable);

    // Возвращает true, если виджет может быть сфокусирован, иначе false
    public boolean isFocusable();
    
}
