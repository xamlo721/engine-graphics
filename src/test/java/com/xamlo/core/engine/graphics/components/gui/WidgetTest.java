package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WidgetTest {

    @Test
    void parentConstructorKeepsParent() {
        Widget parent = new Widget();
        Widget child = new Widget(parent);

        assertSame(parent, child.getParent());
        assertTrue(child.hasParent());
        assertTrue(parent.getChildElements().contains(child));
    }

    @Test
    void resizeDoesNotMutateArgumentAndStoresLocalCoords() {
        Widget parent = new Widget();
        parent.resize(new UIElementGeometry(30, 40, 60, 60));
        Widget child = new Widget(parent);

        UIElementGeometry argument = new UIElementGeometry(5, 5, 10, 10);
        child.resize(argument);

        assertEquals(5, argument.getXCoord(), "resize must not mutate the passed geometry");
        assertEquals(5, argument.getYCoord(), "resize must not mutate the passed geometry");
        assertEquals(5, child.getGeometry().getXCoord(), "stored coords are local (relative to parent)");
        assertEquals(35f, child.getAbsX(), 0.001f, "absolute X sums the parent chain");
        assertEquals(45f, child.getAbsY(), 0.001f, "absolute Y sums the parent chain");
    }

    @Test
    void repeatedResizeIsIdempotent() {
        Widget parent = new Widget();
        parent.resize(new UIElementGeometry(30, 40, 60, 60));
        Widget child = new Widget(parent);

        child.resize(new UIElementGeometry(5, 5, 10, 10));
        float absAfterFirst = child.getAbsX();
        child.resize(new UIElementGeometry(5, 5, 10, 10));

        assertEquals(absAfterFirst, child.getAbsX(), 0.001f, "re-resizing with the same local coords must not accumulate offsets");
    }

    @Test
    void grandparentChainSumsOffsetsAndHitTestUsesAbsoluteCoords() {
        Widget root = new Widget();
        root.resize(new UIElementGeometry(0, 0, 800, 600));
        Widget mid = new Widget(root);
        mid.resize(new UIElementGeometry(10, 20, 200, 100));
        Widget leaf = new Widget(mid);
        leaf.resize(new UIElementGeometry(5, 7, 50, 20));

        assertEquals(15f, leaf.getAbsX(), 0.001f);
        assertEquals(27f, leaf.getAbsY(), 0.001f);

        assertTrue(leaf.containsPoint(15f, 27f), "top-left corner is inside");
        assertFalse(leaf.containsPoint(14.9f, 27f));
        assertTrue(leaf.containsPoint(64.9f, 46.9f), "bottom-right area is inside");
        assertFalse(leaf.containsPoint(65.1f, 27f));
        assertFalse(leaf.containsPoint(15f, 47.1f));
    }

    @Test
    void resizeWithSizePreservesPosition() {
        Widget widget = new Widget();
        widget.resize(new UIElementGeometry(3, 8, 10, 10));

        widget.resize(new ElementSize(40, 60));

        assertEquals(3, widget.getGeometry().getXCoord());
        assertEquals(8, widget.getGeometry().getYCoord());
        assertEquals(40, widget.getGeometry().getWidth());
        assertEquals(60, widget.getGeometry().getHeight());
    }

    @Test
    void setPositionStoresLocalCoordinates() {
        Widget parent = new Widget();
        parent.resize(new UIElementGeometry(100, 0, 300, 1080));
        Widget child = new Widget(parent);

        child.setPosition(20, 180);

        assertEquals(20, child.getGeometry().getXCoord(), "setPosition stores local coords relative to the parent");
        assertEquals(120f, child.getAbsX(), 0.001f);
        assertTrue(child.containsPoint(120f, 180f));
    }

}
