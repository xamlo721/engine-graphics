package com.xamlo.core.engine.graphics.api.gui.elements;

import com.xamlo.core.engine.graphics.api.gui.IFillIndicator;
import com.xamlo.core.engine.graphics.components.gui.EnumOrientation;

/**
 * Интерфейс для элементов непрерывного выбора значения в пределах min..max с шагом.
 */
public interface ISlider extends IFillIndicator {

    void setValue(float value);

    float getValue();

    void setMinValue(float minValue);

    float getMinValue();

    void setMaxValue(float maxValue);

    float getMaxValue();

    void setStep(float step);

    float getStep();

    void setOrientation(EnumOrientation orientation);

    EnumOrientation getOrientation();

}
