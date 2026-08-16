package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SeparatorTest {

    @Test
    public void horizontalPresetIsWideAndThin() {
        Separator h = new Separator();
        assertEquals(240, h.getGeometry().getWidth());
        assertEquals(2, h.getGeometry().getHeight());
    }

    @Test
    public void verticalPresetIsNarrowAndTall() {
        Separator v = new Separator(false);
        assertEquals(2, v.getGeometry().getWidth());
        assertEquals(180, v.getGeometry().getHeight());
    }

    @Test
    public void geometryCanBeOverriddenLikeAnyWidget() {
        Separator s = new Separator(true);
        s.resize(new UIElementGeometry(5, 9, 300, 6));
        assertEquals(5, s.getGeometry().getXCoord());
        assertEquals(9, s.getGeometry().getYCoord());
        assertEquals(300, s.getGeometry().getWidth());
        assertEquals(6, s.getGeometry().getHeight());
    }

}
