package com.xamlo.core.engine.graphics.api.gui.font;

public interface IFontSupport {

    // Устанавливает шрифт для текстовых элементов виджета
    public void setFont(IFont font);

    // Возвращает шрифт, используемый в виджете
    public IFont getFont();
    
}
