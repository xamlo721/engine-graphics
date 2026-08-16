package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.elements.IProgressBar;

/**
 * Индикатор прогресса: заполнение трека через IFillIndicator + подпись с процентами.
 */
public class ProgressBar extends Label implements IProgressBar {

    protected static final Color DEFAULT_FILL_COLOR = new Color(72, 140, 220);

    protected float value;
    protected float minValue;
    protected float maxValue;
    protected Color fillColor;

    public ProgressBar() {
        super("");
        this.value = 0f;
        this.minValue = 0f;
        this.maxValue = 1f;
        this.fillColor = DEFAULT_FILL_COLOR;
        this.backgroundColor = new Color(38, 38, 46, 220);
        setAlignment(EnumAlignment.CENTER);
        setPadding(6);
        updateText();
    }

    @Override
    public void setValue(float value) {
        this.value = clampToRange(value);
        updateText();
    }

    @Override
    public float getValue() {
        return this.value;
    }

    /** Границы меняются независимо; текущее значение всегда удерживается внутри фактического диапазона. */
    @Override
    public void setMinValue(float minValue) {
        this.minValue = minValue;
        clampValueToRange();
        updateText();
    }

    @Override
    public float getMinValue() {
        return this.minValue;
    }

    /** Границы меняются независимо; текущее значение всегда удерживается внутри фактического диапазона. */
    @Override
    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
        clampValueToRange();
        updateText();
    }

    @Override
    public float getMaxValue() {
        return this.maxValue;
    }

    /** Доля заполненного трека для рендерера GUI-элементов. */
    @Override
    public float getFillFraction() {
        float range = maxValue - minValue;
        if (range <= 0f) {
            return 0f;
        }
        float fraction = (value - minValue) / range;
        return Math.max(0f, Math.min(1f, fraction));
    }

    @Override
    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color color) {
        this.fillColor = color == null ? DEFAULT_FILL_COLOR : color;
    }

    /** Держит значение внутри фактического диапазона; при инвертированных границах берёт ближайшую из них. */
    private void clampValueToRange() {
        float low = Math.min(minValue, maxValue);
        float high = Math.max(minValue, maxValue);
        if (value < low || value > high) {
            value = Math.max(low, Math.min(high, value));
        }
    }

    private float clampToRange(float v) {
        return Math.max(Math.min(minValue, maxValue), Math.min(Math.max(minValue, maxValue), v));
    }

    private void updateText() {
        setText(String.format("%d%%", Math.round(getFillFraction() * 100)));
    }

}
