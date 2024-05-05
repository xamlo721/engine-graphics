package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.components.gui.Border;

public interface IBorderSupport {

    /**
     * Устанавливает толщину границы элемента.
     * 
     * @param size Толщина границы в пикселях
     */
    void setBorderSize(int size);

    /**
     * Возвращает толщину границы элемента.
     * 
     * @return Толщина границы в пикселях
     */
    int getBorderSize();

    /**
     * Устанавливает цвет границы кнопки.
     * 
     * @param color Цвет границы кнопки
     */
    void setBorderColor(IColor color);

    /**
     * Возвращает цвет границы кнопки.
     * 
     * @return Цвет границы кнопки
     */
    IColor getBorderColor();
    
    /**
     * Устанавливает радиус скругления углов кнопки.
     * 
     * @param radius Радиус скругления в пикселях
     */
    void setCornerRadius(int radius);

    /**
     * Возвращает радиус скругления углов кнопки.
     * 
     * @return Радиус скругления в пикселях
     */
    int getCornerRadius();

    /**
     *  Устанавливает границу для элемента
     *  
     * @param border граница элемента
     */
    public void setBorder(Border border);

    /** 
     * Возвращает границу виджета
     * 
     * @return граница элемента
     */
    public Border getBorder();
    
}
