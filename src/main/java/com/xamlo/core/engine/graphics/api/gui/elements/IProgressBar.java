package com.xamlo.core.engine.graphics.api.gui.elements;

import com.xamlo.core.engine.graphics.api.gui.IFillIndicator;

/**
 * Интерфейс для элементов отображения прогресса в пределах диапазона min..max.
 * Заполнение рисуется рендерером через контракт IFillIndicator, подпись — как у Label.
 */
public interface IProgressBar extends ILabel, IFillIndicator {

    void setValue(float value);

    float getValue();

    void setMinValue(float minValue);

    float getMinValue();

    void setMaxValue(float maxValue);

    float getMaxValue();

}
