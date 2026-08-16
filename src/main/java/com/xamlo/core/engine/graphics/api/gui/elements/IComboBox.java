package com.xamlo.core.engine.graphics.api.gui.elements;

import java.util.List;

/**
 * Интерфейс для выпадающего списка: кнопка-заголовок со списком вариантов под ней.
 */
public interface IComboBox extends IPushButton {

    /** Добавляет вариант в конец списка. @return индекс добавленного варианта */
    int addItem(String label);

    List<String> getItems();

    void setSelectedIndex(int index);

    int getSelectedIndex();

    boolean isOpen();

}
