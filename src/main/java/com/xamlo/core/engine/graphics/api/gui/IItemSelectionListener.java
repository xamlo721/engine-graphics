package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.api.gui.elements.IComboBox;

/**
 * Обработчик выбора пункта в ComboBox (вызывается только при выборе пользователем,
 * программный setSelectedIndex событие не генерирует).
 */
public interface IItemSelectionListener {

    void onItemSelected(IComboBox comboBox, int index);

}
