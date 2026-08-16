package com.xamlo.core.engine.graphics.api.gui.elements;

/**
 * Интерфейс для реализации элементов с бинарным состоянием «включено/выключено».
 */
public interface ICheckbox extends ILabel {

    void setChecked(boolean checked);

    boolean isChecked();

}
