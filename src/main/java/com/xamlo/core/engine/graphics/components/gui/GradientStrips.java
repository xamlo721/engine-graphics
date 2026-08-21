package com.xamlo.core.engine.graphics.components.gui;

import java.util.ArrayList;
import java.util.List;

/**
 * Чистая геометрия вертикального двухцветного градиента внутри границ элемента (без GL):
 * разбивка области на горизонтальные полосы с равномерным шагом и интерполяция цвета
 * верхней кромки в цвет нижней. Все прямоугольники в локальных координатах элемента
 * ({@link UIElementGeometry}: y растёт вниз), как у {@link FrameRects}.
 */
public final class GradientStrips {

    /** Максимум полос на элемент: достаточно для плавности при любых разумных размерах окна. */
    public static final int MAX_STRIP_COUNT = 96;

    /** Полоса градиента: {@code index}/{@code count} — позиция в ряду (для интерполяции цвета), y/height — смещение от верха и высота. */
    public record Strip(int index, int count, float y, float height) {
    }

    private GradientStrips() {
    }

    /**
     * Разбивка вертикальной области высотой {@code dstHeight} px на равные горизонтальные полосы:
     * по одной на пиксель вплоть до {@link #MAX_STRIP_COUNT}, минимум две (края всегда различимы).
     * Невалидная высота — пустой список.
     */
    public static List<Strip> vertical(float dstWidth, float dstHeight) {
        if (dstWidth <= 0f || dstHeight <= 0f) {
            return new ArrayList<>();
        }
        int n = Math.max(2, Math.min((int) Math.round(dstHeight), MAX_STRIP_COUNT));
        float h = dstHeight / n;
        List<Strip> strips = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            // Накопительные границы без накопления ошибок округления.
            float y = i == 0 ? 0f : strips.get(i - 1).y + strips.get(i - 1).height;
            float stripH = (i == n - 1) ? dstHeight - y : h;
            strips.add(new Strip(i, n, y, stripH));
        }
        return strips;
    }

}
