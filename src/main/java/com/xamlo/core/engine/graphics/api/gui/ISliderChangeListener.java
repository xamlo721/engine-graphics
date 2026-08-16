package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.api.gui.elements.ISlider;

/**
 * Обработчик изменения значения слайдера (вызывается при перетаскивании и программном setValue).
 */
public interface ISliderChangeListener {

    void onChanged(ISlider slider, float value);

}
