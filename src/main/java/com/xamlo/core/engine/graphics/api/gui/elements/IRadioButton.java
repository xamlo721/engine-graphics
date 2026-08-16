package com.xamlo.core.engine.graphics.api.gui.elements;

/**
 * Интерфейс для реализации элементов выбора одного варианта из группы.
 * Элементы с одинаковым именем группы взаимоисключающи: выбор одного
 * снимает отметку со всех остальных в группе.
 */
public interface IRadioButton extends ILabel {

    void setGroup(String group);

    String getGroup();

    void setChecked(boolean checked);

    boolean isChecked();

}
