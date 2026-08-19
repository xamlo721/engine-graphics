package com.xamlo.core.engine.graphics.components.gui;

import java.util.ArrayList;
import java.util.List;

/**
 * Чистая геометрия рамок элементов (без GL): контур по периметру и внешнее
 * кольцо-индикатор фокуса. Все прямоугольники в абсолютных экранных координатах
 * с той же системой осей, что и {@link UIElementGeometry} (y растёт вниз).
 */
public final class FrameRects {

    /** Прямоугольник отрисовки полосы рамки (x, y — верхний левый угол, размеры > 0). */
    public record Strip(float x, float y, float width, float height) {
    }

    private FrameRects() {
    }

    /**
     * Контур из четырёх прямоугольных полос вдоль внутреннего края элемента (рамка
     * виджета при borderSize &gt; 0). Толщина ограничивается половиной меньшей
     * стороны, чтобы полосы не пересекались на узких/низких элементах.
     * Скругление углов пока не поддерживается — полосы всегда прямые.
     */
    public static List<Strip> innerOutline(float x, float y, float w, float h, float thickness) {
        if (w <= 0f || h <= 0f || thickness <= 0f) {
            return new ArrayList<>();
        }
        float t = Math.min(thickness, Math.min(w / 2f, h / 2f));
        List<Strip> strips = new ArrayList<>(4);
        strips.add(new Strip(x, y, w, t));                       // верхняя кромка
        strips.add(new Strip(x, y + h - t, w, t));               // нижняя кромка
        float sideHeight = h - 2 * t;
        if (sideHeight > 0f) {
            strips.add(new Strip(x, y + t, t, sideHeight));                  // левая сторона
            strips.add(new Strip(x + w - t, y + t, t, sideHeight));          // правая сторона
        }
        return strips;
    }

    /**
     * Кольцо-индикатор фокуса: та же разбивка на полосы, но по внешнему краю —
     * вплотную снаружи прямоугольника элемента (внутренний край кольца совпадает с его рамкой).
     */
    public static List<Strip> outerRing(float x, float y, float w, float h, float thickness) {
        if (w <= 0f || h <= 0f || thickness <= 0f) {
            return new ArrayList<>();
        }
        float ox = x - thickness;
        float oy = y - thickness;
        float ow = w + 2 * thickness;
        float oh = h + 2 * thickness;
        List<Strip> strips = new ArrayList<>(4);
        strips.add(new Strip(ox, oy, ow, thickness));                    // верхняя кромка
        strips.add(new Strip(ox, oy + oh - thickness, ow, thickness));   // нижняя кромка
        float sideHeight = oh - 2 * thickness;
        if (sideHeight > 0f) {
            strips.add(new Strip(ox, oy + thickness, thickness, sideHeight));           // левая сторона
            strips.add(new Strip(ox + ow - thickness, oy + thickness, thickness, sideHeight)); // правая сторона
        }
        return strips;
    }

}
