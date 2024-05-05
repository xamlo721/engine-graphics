package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.components.AbstractTexture;
import com.xamlo.core.engine.graphics.components.gui.EnumIconPosition;

public interface IIconSupport {

    /**
     * Устанавливает иконку для элемен.
     * 
     * @param icon Текстура иконки (может быть null для удаления иконки)
     */
    void setIcon(AbstractTexture icon);

    /**
     * Возвращает иконку элемента.
     * 
     * @return Текстура иконки или null, если иконка не установлена
     */
    AbstractTexture getIcon();
    
    /**
     * Устанавливает позицию иконки относительно текста.
     * 
     * @param position Позиция иконки (слева, справа, сверху, снизу от текста)
     */
    void setIconPosition(EnumIconPosition position);

    /**
     * Возвращает позицию иконки относительно текста.
     * 
     * @return Позиция иконки
     */
    EnumIconPosition getIconPosition();

    /**
     * Устанавливает расстояние между иконкой и текстом.
     * 
     * @param spacing Расстояние в пикселях
     */
    void setIconSpacing(int spacing);
    
    /**
     * Возвращает расстояние между иконкой и текстом.
     * 
     * @return Расстояние в пикселях
     */
    int getIconSpacing();
    
}

