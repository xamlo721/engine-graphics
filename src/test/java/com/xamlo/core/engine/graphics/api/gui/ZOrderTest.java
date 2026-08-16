package com.xamlo.core.engine.graphics.api.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.components.gui.Widget;

public class ZOrderTest {

    @Test
    public void defaultZIndexIsZero() {
        Widget widget = new Widget();
        assertEquals(0, widget.getZIndex());
        assertEquals(0, ZOrder.effectiveZIndex(widget));
    }

    @Test
    public void setZIndexStoresValue() {
        Widget widget = new Widget();
        widget.setZIndex(7);
        assertEquals(7, widget.getZIndex());
    }

    @Test
    public void effectiveZIndexFollowsHighestAncestor() {
        Widget root = new Widget();
        Widget child = new Widget(root);
        root.setZIndex(5);
        child.setZIndex(2);

        assertEquals(5, ZOrder.effectiveZIndex(child), "потомок наследует индекс предка");
        assertEquals(5, ZOrder.effectiveZIndex(root));
    }

    @Test
    public void sortedKeepsTreeOrderWithinEqualZ() {
        Widget a = new Widget();
        Widget b = new Widget();

        List<IUIElement> ordered = ZOrder.sorted(Arrays.asList(a, b));

        assertSame(a, ordered.get(0));
        assertSame(b, ordered.get(1));
    }

    @Test
    public void sortedMovesHigherZToBack() {
        Widget a = new Widget();
        Widget b = new Widget();
        Widget c = new Widget();
        c.setZIndex(10);

        List<IUIElement> ordered = ZOrder.sorted(Arrays.asList(a, b, c));

        assertSame(a, ordered.get(0));
        assertSame(b, ordered.get(1));
        assertSame(c, ordered.get(2), "элемент с большим z рисуется последним");
    }

    @Test
    public void sortedLiftsChildrenOfHighZContainer() {
        Widget a = new Widget();
        Widget container = new Widget();
        Widget child = new Widget(container);
        Widget b = new Widget();
        container.setZIndex(10);

        // Плоский список в порядке дерева: a, container, child, b
        List<IUIElement> ordered = ZOrder.sorted(Arrays.asList(a, container, child, b));

        assertSame(a, ordered.get(0));
        assertSame(b, ordered.get(1));
        assertSame(container, ordered.get(2), "контейнер с высоким z уходит в конец");
        assertSame(child, ordered.get(3), "потомок рисуется вместе с контейнером");
    }

    @Test
    public void sortedDoesNotMutateOriginalList() {
        Widget a = new Widget();
        Widget b = new Widget();
        b.setZIndex(5);
        List<IUIElement> original = Arrays.asList(a, b);

        List<IUIElement> ordered = ZOrder.sorted(original);

        assertSame(a, original.get(0));
        assertSame(b, original.get(1));
        assertSame(a, ordered.get(0));
        assertSame(b, ordered.get(1));
    }

}
