package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

public class SliderTest {

    private static final float EPS = 0.0001f;

    @Test
    public void dragListenerMapsCursorPositionToSnappedValue() {
        Slider slider = new Slider(EnumOrientation.HORIZONTAL);
        slider.resize(new UIElementGeometry(0, 0, 300, 40));
        slider.setPosition(100, 50); // без родителя абсолютные координаты совпадают с локальными
        slider.setMinValue(0f);
        slider.setMaxValue(100f);
        slider.setStep(10f);

        AtomicReference<Float> lastReported = new AtomicReference<>();
        AtomicInteger changeEvents = new AtomicInteger();
        slider.setChangeListener((s, value) -> {
            lastReported.set(value);
            changeEvents.incrementAndGet();
        });

        IDragHandlerProxy proxy = new IDragHandlerProxy(slider.getDragListener());

        // Курсор в начале трека → min; в середине → ~50 (со snap'ом к шагу).
        proxy.onDrag(100 + 0f, 70f);
        assertEquals(0f, slider.getValue(), EPS);
        proxy.onDrag(100 + 150f, 70f);
        assertEquals(50f, slider.getValue(), EPS);
        proxy.onDrag(100 + 290f, 70f);
        assertEquals(100f, slider.getValue(), "позиция за концом трека обрезается до max");

        // Позиции вне геометрии не выводят значение за границы.
        proxy.onDrag(-400f, 70f);
        assertTrue(slider.getValue() >= 0f && slider.getValue() <= 100f);
    }

    @Test
    public void noChangeEventForUnchangedValue() {
        Slider slider = new Slider(EnumOrientation.HORIZONTAL);
        slider.resize(new UIElementGeometry(0, 0, 200, 30));
        slider.setPosition(0, 0);
        AtomicInteger events = new AtomicInteger();
        slider.setChangeListener((s, v) -> events.incrementAndGet());

        IDragHandlerProxy proxy = new IDragHandlerProxy(slider.getDragListener());
        float middleAbsX = 100; // центр 200-пиксельного трека при min=0,max=100 → value 50 (step по умолчанию 1)
        proxy.onDrag(middleAbsX, 15f);
        int afterFirst = events.get();
        assertTrue(afterFirst == 1, "первое изменение сообщает о себе один раз");

        proxy.onDrag(middleAbsX + 0.4f, 15f); // тот же snapped-value после округления к шагу
        assertEquals(afterFirst, events.get(), "повторное движение в пределах того же шага событий не даёт");
    }

    @Test
    public void knobFollowsValueAndOrientation() {
        Slider horizontal = new Slider(EnumOrientation.HORIZONTAL);
        horizontal.resize(new UIElementGeometry(0, 0, 206, 40)); // usable = 206 - 12(pad*2) - 18(knob) = 176
        horizontal.setValue(0.5f * (horizontal.getMaxValue() - horizontal.getMinValue()) + horizontal.getMinValue());

        int expectedKnobX = 6 + Math.round(0.5f * 176);
        assertEquals(expectedKnobX, horizontal.knob.getGeometry().getXCoord());
        assertEquals((40 - 18) / 2, horizontal.knob.getGeometry().getYCoord(), "бегунок центрирован по высоте трека");

        horizontal.setOrientation(EnumOrientation.VERTICAL);
        assertEquals(Math.max(0, (206 - 18) / 2), horizontal.knob.getGeometry().getXCoord(), "в вертикальной ориентации бегунок центрируется по ширине");
        assertTrue(horizontal.knob.getGeometry().getYCoord() > 0 && horizontal.knob.getGeometry().getYCoord() < 40);
    }

    @Test
    public void fillFractionStaysWithinUnitRangeForExtremeValues() {
        Slider slider = new Slider(EnumOrientation.HORIZONTAL);
        slider.resize(new UIElementGeometry(0, 0, 300, 40));
        slider.setValue(slider.getMaxValue());
        float maxFill = slider.getFillFraction();
        slider.setValue(slider.getMinValue());
        float minFill = slider.getFillFraction();
        assertTrue(minFill >= 0f && minFill <= 1f);
        assertTrue(maxFill >= 0f && maxFill <= 1f);
        assertFalse(maxFill == 0f || minFill == 1f, "крайние значения дают различимые доли заполнения" + "");
    }

    /** Тонкая обёртка над IDragListener для компактных вызовов в тестах. */
    private static final class IDragHandlerProxy implements com.xamlo.core.engine.graphics.api.gui.IDragListener {
        private final com.xamlo.core.engine.graphics.api.gui.IDragListener delegate;

        IDragHandlerProxy(com.xamlo.core.engine.graphics.api.gui.IDragListener delegate) {
            this.delegate = delegate;
        }

        void onDrag(float xCoord, float yCoord) {
            delegate.onDrag(null, xCoord, yCoord, 0f, 0f);
        }

        @Override
        public void onDragStart(com.xamlo.core.engine.graphics.api.gui.IDraggable element, float xCoord, float yCoord) {
            delegate.onDragStart(element, xCoord, yCoord);
        }

        @Override
        public void onDrag(com.xamlo.core.engine.graphics.api.gui.IDraggable element, float xCoord, float yCoord, float deltaX, float deltaY) {
            delegate.onDrag(element, xCoord, yCoord, deltaX, deltaY);
        }

        @Override
        public void onDrop(com.xamlo.core.engine.graphics.api.gui.IDraggable element, com.xamlo.core.engine.graphics.api.gui.IDropTarget target, float xCoord, float yCoord) {
            delegate.onDrop(element, target, xCoord, yCoord);
        }

        @Override
        public void onDragEnd(com.xamlo.core.engine.graphics.api.gui.IDraggable element, float xCoord, float yCoord) {
            delegate.onDragEnd(element, xCoord, yCoord);
        }

        @Override
        public void onDragEnterTarget(com.xamlo.core.engine.graphics.api.gui.IDraggable element, com.xamlo.core.engine.graphics.api.gui.IDropTarget target) {
            delegate.onDragEnterTarget(element, target);
        }

        @Override
        public void onDragLeaveTarget(com.xamlo.core.engine.graphics.api.gui.IDraggable element, com.xamlo.core.engine.graphics.api.gui.IDropTarget target) {
            delegate.onDragLeaveTarget(element, target);
        }
    }

}
