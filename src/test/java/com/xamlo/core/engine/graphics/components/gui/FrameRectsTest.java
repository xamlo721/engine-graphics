package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Чистая геометрия рамок (E2): контур по периметру и внешнее кольцо фокуса —
 * без GL, только математика прямоугольников в абсолютных координатах.
 */
public class FrameRectsTest {

    /** Площадь пересечения двух прямоугольников (0 при касании краями). */
    private static float intersectionArea(FrameRects.Strip a, FrameRects.Strip b) {
        float w = Math.min(a.x() + a.width(), b.x() + b.width()) - Math.max(a.x(), b.x());
        float h = Math.min(a.y() + a.height(), b.y() + b.height()) - Math.max(a.y(), b.y());
        return (w > 0f && h > 0f) ? w * h : 0f;
    }

    @Test
    public void innerOutlinePartitionsPerimeterWithoutOverlap() {
        List<FrameRects.Strip> strips = FrameRects.innerOutline(10f, 20f, 100f, 60f, 4f);

        assertEquals(4, strips.size());
        // Верхняя/нижние кромки на всю ширину, боковые — только средняя часть.
        assertTrue(strips.get(0).width() == 100f && strips.get(0).height() == 4f, "верхняя полоса");
        assertTrue(strips.get(1).y() == 76f && strips.get(1).height() == 4f, "нижняя полоса у нижнего края");
        assertTrue(strips.get(2).x() == 10f && strips.get(2).height() == 52f, "левая сторона без углов");
        assertTrue(strips.get(3).x() == 106f && strips.get(3).height() == 52f, "правая сторона без углов");

        float totalArea = 0f;
        for (int i = 0; i < strips.size(); i++) {
            totalArea += strips.get(i).width() * strips.get(i).height();
            for (int j = i + 1; j < strips.size(); j++) {
                assertEquals(0f, intersectionArea(strips.get(i), strips.get(j)), "полосы не пересекаются");
            }
        }
        // Суммарная площадь равна площади периметральной ленты: w*h минус внутреннее.
        assertEquals(100f * 60f - 92f * 52f, totalArea, 1e-4f);
    }

    @Test
    public void thinElementClampsThicknessToHalfOfSmallestSide() {
        List<FrameRects.Strip> strips = FrameRects.innerOutline(0f, 0f, 6f, 20f, 4f);

        assertEquals(4, strips.size());
        assertTrue(strips.get(0).height() <= 3f && strips.get(0).width() == 6f, "толщина ограничена половиной ширины");
        float totalArea = 0f;
        for (FrameRects.Strip strip : strips) {
            assertTrue(strip.width() > 0f && strip.height() > 0f, "нет нулевых/отрицательных размеров");
            assertTrue(strip.x() >= 0f && strip.y() >= 0f
                    && strip.x() + strip.width() <= 6f && strip.y() + strip.height() <= 20f, "полосы внутри элемента");
            totalArea += strip.width() * strip.height();
        }
        // При clamped t=3 лента полностью закрывает узкий элемент: площадь равна площади самого.
        assertEquals(6f * 20f, totalArea, 1e-4f);
    }

    @Test
    public void degenerateInputsYieldNoStrips() {
        assertTrue(FrameRects.innerOutline(0f, 0f, 0f, 10f, 2f).isEmpty(), "нулевая ширина");
        assertTrue(FrameRects.innerOutline(0f, 0f, 10f, -5f, 2f).isEmpty(), "отрицательная высота");
        assertTrue(FrameRects.innerOutline(0f, 0f, 10f, 10f, 0f).isEmpty(), "нулевая толщина");
        assertTrue(FrameRects.outerRing(0f, 0f, 0f, 10f, 2f).isEmpty(), "кольцо при нулевой ширине");
        assertTrue(FrameRects.outerRing(0f, 0f, 10f, 10f, -1f).isEmpty(), "кольцо с отрицательной толщиной");
    }

    @Test
    public void outerRingSitsJustOutsideElementWithoutTouchingItsInterior() {
        List<FrameRects.Strip> ring = FrameRects.outerRing(30f, 40f, 50f, 30f, 2f);

        assertEquals(4, ring.size());
        // Внешняя граница кольца: элемент расширен на толщину со всех сторон.
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (FrameRects.Strip strip : ring) {
            minX = Math.min(minX, strip.x());
            minY = Math.min(minY, strip.y());
            maxX = Math.max(maxX, strip.x() + strip.width());
            maxY = Math.max(maxY, strip.y() + strip.height());
        }
        assertEquals(28f, minX, 1e-6f);
        assertEquals(38f, minY, 1e-6f);
        assertEquals(82f, maxX, 1e-6f);
        assertEquals(72f, maxY, 1e-6f);

        // Кольцо не заходит внутрь элемента: пересечение с его внутренностью пустое.
        FrameRects.Strip interior = new FrameRects.Strip(30f, 40f, 50f, 30f);
        for (FrameRects.Strip strip : ring) {
            assertEquals(0f, intersectionArea(strip, interior), "полоса кольца снаружи контура");
        }
    }

}
