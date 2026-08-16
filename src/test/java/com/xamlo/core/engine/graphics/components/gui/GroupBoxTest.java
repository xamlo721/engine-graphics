package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class GroupBoxTest {

    @Test
    public void widgetDelegatesIBorderSupportToItsBorderField() {
        Widget w = new Widget();
        assertEquals(4, w.getBorderSize(), "толщина рамки по умолчанию из конструктора");
        assertNotNull(w.getBorderColor());

        Color borderColor = new Color(10, 20, 30);
        w.setBorder(new Border(6, borderColor));
        assertEquals(6, w.getBorderSize());
        assertArrayEquals(new int[]{10, 20, 30},
                new int[]{w.getBorderColor().getRed(), w.getBorderColor().getGreen(), w.getBorderColor().getBlue()});

        w.setBorderSize(9);
        assertEquals(9, w.getBorderSize());
        assertEquals(10, w.getBorderColor().getRed(), "смена толщины сохраняет цвет");

        w.setCornerRadius(7);
        assertEquals(7, w.getCornerRadius());
    }

    @Test
    public void setTitleCreatesAndShowsTitleLabelChild() {
        GroupBox box = new GroupBox();
        assertTrue(box.getChildElements().isEmpty(), "до первого заголовка дочерних элементов нет");

        box.setTitle("Options");
        assertEquals("Options", box.getTitle());
        assertFalse(box.getChildElements().isEmpty(), "заголовок — дочерний Label");
        assertTrue(((Label) box.getChildElements().get(0)).isVisible());
    }

    @Test
    public void emptyTitleHidesTheLabelAgain() {
        GroupBox box = new GroupBox();
        box.setTitle("Temp");
        assertNotNull(box.titleLabel);

        box.setTitle("");
        assertFalse(box.titleLabel.isVisible(), "пустой заголовок скрывает метку");
    }

    @Test
    public void titleFollowsGroupBoxWidthOnResize() {
        GroupBox box = new GroupBox();
        box.setTitle("Performance");
        box.resize(new UIElementGeometry(14, 328, 352, 170));

        int expectedWidth = Math.max(352, 48) - 16;
        assertEquals(expectedWidth, box.titleLabel.getGeometry().getWidth());
        assertEquals(8, box.titleLabel.getGeometry().getXCoord());
    }

}
