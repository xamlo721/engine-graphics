package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.gui.IClippingElement;
import com.xamlo.core.engine.graphics.api.gui.elements.IWidget;

/** Чистые расчёты ClipContexts без GL: эффективный клип-прямоугольник и перевод в NDC. */
public class ClipContextsTest {

    /** Обычный Widget с включённым обрезанием потомков. */
    private static final class ClipBox extends Widget implements IClippingElement {
        ClipBox() {
            super();
        }

        ClipBox(IWidget parent) {
            super(parent);
        }

        @Override
        public boolean clipsChildren() {
            return true;
        }
    }

    private static void place(Widget widget, int x, int y, int w, int h) {
        widget.resize(new UIElementGeometry(x, y, w, h));
    }

    @Test
    public void noClippingAncestorMeansNoClipRect() {
        Widget root = new Widget();
        place(root, 10, 20, 300, 200);
        Widget mid = new Widget(root);
        place(mid, 5, 5, 100, 80);
        Widget leaf = new Widget(mid);
        place(leaf, 1, 2, 40, 30);

        assertNull(ClipContexts.effectiveClippedRect(leaf));
        assertNull(ClipContexts.effectiveClippedRect(root), "сам по себе без клиперов тоже null");
    }

    @Test
    public void singleClipperYieldsItsBoundsInScreenCoords() {
        ClipBox clipper = new ClipBox();
        place(clipper, 100, 50, 400, 300);
        Widget child = new Widget(clipper);
        place(child, 10, 10, 60, 40);

        assertArrayEquals(new int[] {100, 50, 400, 300}, ClipContexts.effectiveClippedRect(child));
    }

    @Test
    public void windowEdgesClampOffscreenClipper() {
        ClipBox clipper = new ClipBox();
        // Часть прямоугольника за левым/верхним краем окна
        place(clipper, -50, -50, 200, 200);
        Widget child = new Widget(clipper);
        place(child, 80, 80, 20, 20);

        assertArrayEquals(new int[] {0, 0, 150, 150}, ClipContexts.effectiveClippedRect(child));
    }

    @Test
    public void nestedClippersIntersectWithEachOther() {
        ClipBox outer = new ClipBox();
        place(outer, 0, 0, 800, 600);
        ClipBox inner = new ClipBox(outer);
        place(inner, 700, 500, 500, 500); // выходит за правый и нижний край внешнего
        Widget leaf = new Widget(inner);
        place(leaf, 5, 5, 30, 30);

        // Пересечение: x [700..800), y [500..600)
        assertArrayEquals(new int[] {700, 500, 100, 100}, ClipContexts.effectiveClippedRect(leaf));
    }

    @Test
    public void nonClippingAncestorsAreWalkedThroughButIgnored() {
        ClipBox clipper = new ClipBox();
        place(clipper, 40, 40, 200, 150);
        Widget plainA = new Widget(clipper);
        place(plainA, 10, 10, 180, 130);
        Widget plainB = new Widget(plainA);
        place(plainB, 5, 5, 160, 110);

        assertArrayEquals(new int[] {40, 40, 200, 150}, ClipContexts.effectiveClippedRect(plainB));
    }

    @Test
    public void fullyOffscreenOrZeroSizedClipperGivesNull() {
        ClipBox offscreen = new ClipBox();
        place(offscreen, 2000, 1200, 50, 50); // за правым/нижним краем окна
        assertNull(ClipContexts.effectiveClippedRect(new Widget(offscreen)));

        ClipBox zero = new ClipBox();
        place(zero, 10, 10, 0, 0);
        assertNull(ClipContexts.effectiveClippedRect(new Widget(zero)), "нулевой клипер не обрезает");
    }

    @Test
    public void fullWindowRectConvertsToFullNdcSquare() {
        Vector4f ndc = ClipContexts.rectToNdc(new int[] {0, 0, ClipContexts.WINDOW_WIDTH, ClipContexts.WINDOW_HEIGHT});
        assertEquals(-1f, ndc.x, 1e-6f);
        assertEquals(-1f, ndc.y, 1e-6f);
        assertEquals(2f, ndc.z, 1e-6f);
        assertEquals(2f, ndc.w, 1e-6f);
    }

    @Test
    public void topHalfOfScreenMapsToUpperLeftNdcQuadrant() {
        // Прямоугольник [x=0,y=0,w=960,h=540]: верхняя половина экрана.
        // NDC: левый нижний угол (-1, 0), размер (1, 1).
        Vector4f ndc = ClipContexts.rectToNdc(new int[] {0, 0, 960, 540});
        assertEquals(-1f, ndc.x, 1e-6f);
        assertEquals(0f, ndc.y, 1e-6f);
        assertEquals(1f, ndc.z, 1e-6f);
        assertEquals(1f, ndc.w, 1e-6f);
    }

    @Test
    public void bottomRightQuarterMapstoLowerRightNdcCorner() {
        // [x=960,y=540,w=960,h=540] → правый нижний квадрант: NDC (0,-1) + (1,1).
        Vector4f ndc = ClipContexts.rectToNdc(new int[] {960, 540, 960, 540});
        assertEquals(0f, ndc.x, 1e-6f);
        assertEquals(-1f, ndc.y, 1e-6f);
        assertEquals(1f, ndc.z, 1e-6f);
        assertEquals(1f, ndc.w, 1e-6f);
    }

}
