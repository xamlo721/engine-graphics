package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.components.gui.EnumAlignment;
import com.xamlo.core.engine.graphics.components.gui.ElementSize;

public interface IResizable {

    /**
     *  Возвращает геометрию (положение и размер) виджета
     * @return
     */
	public UIElementGeometry getGeometry();
	
    /**
     *  Возвращает размер виджета
     * @return
     */
	public ElementSize getWidSize();
	
    /**
     *  Изменяет размер и положение виджета согласно указанной геометрии
     * @param geometry
     */
	public void resize(UIElementGeometry geometry);
	
    /**
     *  Изменяет размер виджета согласно указанному размеру
     * @param size
     */
	public void resize(ElementSize size);

    /**
     * Устанавливает выравнивание содержимого (текста и иконки) внутри элемента.
     * 
     * @param alignment Способ выравнивания содержимого
     */
    void setAlignment(EnumAlignment alignment);

    /**
     * Возвращает выравнивание содержимого внутри кнопки.
     * 
     * @return Способ выравнивания содержимого
     */
    EnumAlignment getAlignment();

    /**
     * Устанавливает внутренние отступы кнопки.
     * 
     * @param padding Величина отступа в пикселях
     */
    void setPadding(int padding);

    /**
     * Возвращает внутренние отступы кнопки.
     * 
     * @return Величина отступа в пикселях
     */
    int getPadding();

    /**
     * Устанавливает внешние отступы элемента.
     * 
     * @param margin Величина отступа в пикселях
     */
    void setMargin(int margin);

    /**
     * Возвращает внешние отступы кнопки.
     * 
     * @return Величина отступа в пикселях
     */
    int getMargin();
}
