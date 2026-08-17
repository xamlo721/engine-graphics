package com.xamlo.core.engine.graphics.components.gui;

import org.joml.Vector4f;

import com.xamlo.core.engine.graphics.api.gui.IClippingElement;
import com.xamlo.core.engine.graphics.api.gui.IResizable;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;

/**
 * Чистая геометрия скисора UI: эффективный клип-прямоугольник элемента и его
 * перевод в NDC координаты окна для юниформов GUI-шейдера (useClip/clipRectNdc).
 * Без GL — вся логика покрывается юнит-тестами без контекста.
 */
public final class ClipContexts {

    /** Логический размер окна, зашитый в ортографические матрицы рендереров UI. */
    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;

    private ClipContexts() {
    }

    /**
     * Эффективный клип-прямоугольник элемента [x, y, w, h] в абсолютных
     * логических координатах окна: пересечение границ всех предков с
     * {@link IClippingElement#clipsChildren()} и рамками самого окна.
     * Null, если ни один предок не обрезает (клиповать нечего).
     */
    public static int[] effectiveClippedRect(IUIElement element) {
        int x0 = 0;
        int y0 = 0;
        int x1 = WINDOW_WIDTH;
        int y1 = WINDOW_HEIGHT;
        boolean anyClipper = false;

        for (IUIElement anc = parentOf(element); anc != null && !isDegenerate(x0, y0, x1, y1); anc = parentOf(anc)) {
            if (!(anc instanceof IClippingElement clipper) || !clipper.clipsChildren()) {
                continue;
            }
            UIElementGeometry geom = geometryOf(anc);
            if (geom == null) {
                continue;
            }
            int ax = Math.round(anc.getAbsX());
            int ay = Math.round(anc.getAbsY());
            x0 = Math.max(x0, ax);
            y0 = Math.max(y0, ay);
            x1 = Math.min(x1, ax + geom.getWidth());
            y1 = Math.min(y1, ay + geom.getHeight());
            anyClipper = true;
        }

        return (anyClipper && !isDegenerate(x0, y0, x1, y1)) ? new int[] {x0, y0, x1 - x0, y1 - y0} : null;
    }

    /**
     * Прямоугольник [x, y, w, h] в логических координатах окна → NDC
     * {левый нижний угол X, левый нижний угол Y, ширина, высота}.
     * Соответствует ортоматрице рендереров: ortho(0, W, H, 0, ...) — ось Y
     * экранных координат направлена вниз и мапится на ndc.y = 1 − 2·sy/H.
     */
    public static Vector4f rectToNdc(int[] rect) {
        float nx = 2f * rect[0] / WINDOW_WIDTH - 1f;
        float ny = 1f - 2f * (rect[1] + rect[3]) / WINDOW_HEIGHT;
        float nw = 2f * rect[2] / WINDOW_WIDTH;
        float nh = 2f * rect[3] / WINDOW_HEIGHT;
        return new Vector4f(nx, ny, nw, nh);
    }

    private static IUIElement parentOf(IUIElement element) {
        return element.hasParent() ? element.getParent() : null;
    }

    private static UIElementGeometry geometryOf(IUIElement element) {
        if (!(element instanceof IResizable resizable)) {
            return null;
        }
        return resizable.getGeometry();
    }

    private static boolean isDegenerate(int x0, int y0, int x1, int y1) {
        return x0 >= x1 || y0 >= y1;
    }

}
