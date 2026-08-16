package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CheckBoxTest {

    @Test
    public void clickTogglesStateAndNotifiesHandler() {
        final CheckBox box = new CheckBox("sound");
        int[] handlerCalls = {0};
        box.setClickListener(button -> handlerCalls[0]++);

        assertFalse(box.isChecked());
        box.getClickListener().onClicked(box);
        assertTrue(box.isChecked());
        assertEquals(1, handlerCalls[0]);

        box.getClickListener().onClicked(box);
        assertFalse(box.isChecked());
        assertEquals(2, handlerCalls[0]);
    }

    @Test
    public void setCheckedWithoutClickDoesNotNotifyHandler() {
        final CheckBox box = new CheckBox();
        int[] calls = {0};
        box.setClickListener(button -> calls[0]++);

        box.setChecked(true);
        assertTrue(box.isChecked());
        assertEquals(0, calls[0]);
    }

    @Test
    public void indicatorsFollowGeometryOnResize() {
        CheckBox box = new CheckBox("x");
        box.resize(new UIElementGeometry(50, 60, 300, 40));

        // Коробка: x=8 (BOX_X_PAD), y=(40-20)/2=10 относительно локальных координат элемента.
        assertEquals(8, box.boxIndicator.getGeometry().getXCoord());
        assertEquals(10, box.boxIndicator.getGeometry().getYCoord());
        assertEquals(20, box.boxIndicator.getGeometry().getWidth());

        // Галочка вписана внутрь коробки с отступом TICK_INSET=5.
        assertEquals(13, box.tickMark.getGeometry().getXCoord());
        assertEquals(15, box.tickMark.getGeometry().getYCoord());
        assertEquals(10, box.tickMark.getGeometry().getWidth());
    }

    @Test
    public void tickVisibleOnlyWhenCheckedAndParentShown() {
        CheckBox box = new CheckBox();
        assertFalse(box.tickMark.isVisible());

        box.setChecked(true);
        assertTrue(box.tickMark.isVisible());

        box.hide();
        assertFalse(box.boxIndicator.isVisible());
        assertFalse(box.tickMark.isVisible());

        box.show();
        assertTrue(box.boxIndicator.isVisible());
        assertTrue(box.tickMark.isVisible(), "галочка возвращается после show при checked=true");
    }

}
