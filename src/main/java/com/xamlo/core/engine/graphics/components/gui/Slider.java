package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.IDragListener;
import com.xamlo.core.engine.graphics.api.gui.IDraggable;
import com.xamlo.core.engine.graphics.api.gui.IDropTarget;
import com.xamlo.core.engine.graphics.api.gui.ISliderChangeListener;
import com.xamlo.core.engine.graphics.api.gui.elements.ISlider;
import com.xamlo.core.engine.graphics.components.gui.EnumOrientation;

/**
 * Слайдер: трек с заполнением до бегунка + перетаскивание мышью. Встроенный drag-слушатель
 * переводит позицию курсора в значение со шагом; его не следует заменять через setDragListener().
 */
public class Slider extends Widget implements ISlider, IDraggable {

    protected static final Color DEFAULT_FILL_COLOR = new Color(72, 140, 220);
    private static final int KNOB_SIZE = 18;
    private static final float TRACK_PAD = 6f;

    protected float value;
    protected float minValue;
    protected float maxValue;
    protected float step;
    protected EnumOrientation orientation;
    protected NonInteractiveQuad knob;
    protected Color fillColor;
    protected IDragListener activeDragListener;
    protected ISliderChangeListener changeListener;

    public Slider() {
        this(EnumOrientation.HORIZONTAL);
    }

    public Slider(EnumOrientation orientation) {
        super();
        this.value = 0f;
        this.minValue = 0f;
        this.maxValue = 100f;
        this.step = 1f;
        this.orientation = orientation == null ? EnumOrientation.HORIZONTAL : orientation;
        this.fillColor = DEFAULT_FILL_COLOR;
        this.backgroundColor = new Color(38, 38, 46, 220);

        this.knob = new NonInteractiveQuad();
        this.knob.setBackgroundColor(new Color(235, 235, 235, 230));
        addChild(this.knob);

        this.activeDragListener = new IDragHandler();

        repositionKnob();
    }

    @Override
    public void setValue(float value) {
        float snapped = snap(clampToRange(value));
        if (snapped != this.value) {
            this.value = snapped;
            repositionKnob();
            fireChange();
        }
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
        repositionKnob();
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
        repositionKnob();
    }

    @Override
    public float getMaxValue() {
        return this.maxValue;
    }

    /** Шаг должен быть положительным; иначе сохраняется текущий/единичный. */
    @Override
    public void setStep(float step) {
        if (step > 0f) {
            this.step = step;
        }
    }

    @Override
    public float getStep() {
        return this.step;
    }

    @Override
    public void setOrientation(EnumOrientation orientation) {
        this.orientation = orientation == null ? EnumOrientation.HORIZONTAL : orientation;
        repositionKnob();
    }

    @Override
    public EnumOrientation getOrientation() {
        return this.orientation;
    }

    public void setChangeListener(ISliderChangeListener listener) {
        this.changeListener = listener;
    }

    /** Доля заполнения трека до центра бегунка — для рендерера GUI-элементов. */
    @Override
    public float getFillFraction() {
        float range = maxValue - minValue;
        if (range <= 0f) {
            return 0f;
        }
        boolean horizontal = isHorizontal();
        int length = horizontal ? getGeometry().getWidth() : getGeometry().getHeight();
        if (length <= KNOB_SIZE + TRACK_PAD * 2f) {
            return 0f;
        }
        float fraction = (value - minValue) / range;
        float knobCenterLocal = TRACK_PAD + fraction * (length - TRACK_PAD * 2f - KNOB_SIZE) + KNOB_SIZE / 2f;
        return Math.max(0f, Math.min(1f, knobCenterLocal / length));
    }

    @Override
    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color color) {
        this.fillColor = color == null ? DEFAULT_FILL_COLOR : color;
    }

    private boolean isHorizontal() {
        return orientation != EnumOrientation.VERTICAL;
    }

    /** Позиционирует бегунок внутри текущей геометрии согласно значению и ориентации. */
    protected void repositionKnob() {
        float range = maxValue - minValue;
        float fraction = range > 0f ? clampUnit((value - minValue) / range) : 0f;

        int w = getGeometry().getWidth();
        int h = getGeometry().getHeight();
        if (isHorizontal()) {
            int usable = Math.max(0, w - (int) (TRACK_PAD * 2f) - KNOB_SIZE);
            int x = (int) Math.round(TRACK_PAD + fraction * usable);
            int y = Math.max(0, (h - KNOB_SIZE) / 2);
            knob.resize(new UIElementGeometry(x, y, KNOB_SIZE, KNOB_SIZE));
        } else {
            int usable = Math.max(0, h - (int) (TRACK_PAD * 2f) - KNOB_SIZE);
            int y = (int) Math.round(TRACK_PAD + fraction * usable);
            int x = Math.max(0, (w - KNOB_SIZE) / 2);
            knob.resize(new UIElementGeometry(x, y, KNOB_SIZE, KNOB_SIZE));
        }
    }

    private void fireChange() {
        if (changeListener != null) {
            changeListener.onChanged(this, value);
        }
    }

    /** Переводит абсолютную координату курсора в значение с шагом и диапазоном. */
    protected float positionToValue(float absoluteCoord) {
        boolean horizontal = isHorizontal();
        float startAbs = horizontal ? getAbsX() : getAbsY();
        int length = horizontal ? getGeometry().getWidth() : getGeometry().getHeight();
        if (length <= 0) {
            return minValue;
        }
        float rawFraction = clampUnit((absoluteCoord - startAbs) / length);
        float range = maxValue - minValue;
        return snap(minValue + rawFraction * range);
    }

    private float snap(float v) {
        if (step <= 0f) {
            return v;
        }
        return Math.round(v / step) * step;
    }

    private float clampToRange(float v) {
        return Math.max(minValue, Math.min(maxValue, v));
    }

    private static float clampUnit(float f) {
        return Math.max(0f, Math.min(1f, f));
    }

    /** Держит значение внутри фактического диапазона; при инвертированных границах берёт ближайшую из них. */
    private void clampValueToRange() {
        float low = Math.min(minValue, maxValue);
        float high = Math.max(minValue, maxValue);
        if (value < low || value > high) {
            value = Math.max(low, Math.min(high, value));
        }
    }

    /** По умолчанию слушатель не заменяется: null возвращает встроенную логику слайдера. */
    @Override
    public void setDragListener(IDragListener listener) {
        this.activeDragListener = (listener == null) ? new IDragHandler() : listener;
    }

    /** Возвращает активный drag-слушатель: по умолчанию встроенная логика слайдера. */
    @Override
    public IDragListener getDragListener() {
        return this.activeDragListener;
    }

    /** Встроенное поведение перетаскивания бегунка. */
    protected class IDragHandler implements IDragListener {

        @Override
        public void onDragStart(IDraggable element, float xCoord, float yCoord) {
            // Без состояния: каждая позиция DRAG независимо переводится в значение.
        }

        @Override
        public void onDrag(IDraggable element, float xCoord, float yCoord, float deltaX, float deltaY) {
            boolean horizontal = isHorizontal();
            float coord = horizontal ? xCoord : yCoord;
            setValue(positionToValue(coord));
        }

        @Override
        public void onDrop(IDraggable element, IDropTarget target, float xCoord, float yCoord) {
        }

        @Override
        public void onDragEnd(IDraggable element, float xCoord, float yCoord) {
        }

        @Override
        public void onDragEnterTarget(IDraggable element, IDropTarget target) {
        }

        @Override
        public void onDragLeaveTarget(IDraggable element, IDropTarget target) {
        }

    }

}
