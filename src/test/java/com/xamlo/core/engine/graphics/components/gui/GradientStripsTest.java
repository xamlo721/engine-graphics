package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Чистая геометрия вертикального градиента: равные полосы без накопления ошибок
 * округления, потолок MAX_STRIP_COUNT, минимум две полосы на элемент.
 */
public class GradientStripsTest {

    private final float EPS = 1e-3f;

    @Test
    public void invalidSizeYieldsNoStrips() {
        assertTrue(GradientStrips.vertical(0f, 50f).isEmpty());
        assertTrue(GradientStrips.vertical(-2f, 40f).isEmpty());
        assertTrue(GradientStrips.vertical(80f, -4f).isEmpty());
    }

    @Test
    public void oneStripPerPixelUpToCapAndCumulativeBoundariesDriftFree() {
        List<GradientStrips.Strip> strips = GradientStrips.vertical(64f, 50f);
        assertEquals(50, strips.size());
        for (int i = 0; i < strips.size(); i++) {
            var s = strips.get(i);
            assertEquals(i, s.index());
            assertEquals(strips.size(), s.count());
            if (i == strips.size() - 1) {
                // последняя полоса доводит сумму ровно до высоты без накопления ошибок
                assertEquals(50f, s.y() + s.height(), EPS);
            } else {
                assertEquals(50f / strips.size(), s.height(), EPS);
            }
        }
        assertEquals(0f, strips.get(0).y(), EPS);
    }

    @Test
    public void hugeHeightCapsAtMaxStripsWithUniformSplit() {
        List<GradientStrips.Strip> strips = GradientStrips.vertical(32f, 5000f);
        assertEquals(GradientStrips.MAX_STRIP_COUNT, strips.size());
        float expectedH = 5000f / GradientStrips.MAX_STRIP_COUNT;
        for (var s : strips) {
            assertTrue(Math.abs(s.height() - expectedH) <= 1e-2f || s == strips.get(strips.size() - 1), "uniform: " + s);
        }
        var last = strips.get(strips.size() - 1);
        assertEquals(5000f, last.y() + last.height(), 1e-2f);
    }

    @Test
    public void subPixelHeightStillGivesTwoDistinctEdges() {
        List<GradientStrips.Strip> strips = GradientStrips.vertical(48f, 0.6f);
        assertEquals(2, strips.size());
        assertEquals(0f, strips.get(0).y(), EPS);
        assertEquals(0.6f, strips.get(1).y() + strips.get(1).height(), EPS);
    }
}
