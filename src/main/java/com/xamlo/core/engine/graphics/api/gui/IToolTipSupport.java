package com.xamlo.core.engine.graphics.api.gui;

public interface IToolTipSupport {

    // Устанавливает всплывающую подсказку для виджета
    public void setToolTipText(String tooltip);

    // Возвращает текст всплывающей подсказки для виджета
    public String getToolTipText();
    
}
