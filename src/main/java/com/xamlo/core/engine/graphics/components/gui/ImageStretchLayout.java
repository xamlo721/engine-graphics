package com.xamlo.core.engine.graphics.components.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Чистая геометрия растяжения изображения внутри границ элемента (без OpenGL):
 * режимы FILL / FIT / CENTER / NINEPATCH из {@link EnumImageStretch}.
 *
 * <p>Все прямоугольники в координатах элемента с той же системой осей, что и
 * {@link UIElementGeometry} (x,y — верхний левый угол, y растёт вниз). Координаты
 * исходной текстуры ({@code s*}) — пиксели от левого верхнего угла изображения, как
 * их отдаёт загрузчик текстур.</p>
 *
 * <p>Nine-patch: угловые борды рисуются без масштабирования, кромки тянутся по одной
 * оси, центр — по обеим. Если элемент меньше суммы «углы + минимальная середина»,
 * вся раскладка равномерно уменьшается до размеров элемента (единственный случай,
 * когда углы масштабируются). Борды больше половины стороны источника пропорционально
 * ужимаются, чтобы противоположные пары не пересекались; нулевые фрагменты не выдаются.</p>
 */
public final class ImageStretchLayout {

    /** Прямоугольник в локальных координатах элемента (y растёт вниз). */
    public record StretchRect(float x, float y, float width, float height) {
    }

    /**
     * Фрагмент отрисовки: участок исходного изображения ({@code sx,sy,sw,sh}, px) и
     * его назначение внутри элемента ({@code dx,dy,dw,dh}). Для FILL/FIT/CENTER это
     * один фрагмент на весь режим, для NINEPATCH — до девяти.
     */
    public record StretchPiece(
            float sx, float sy, float sw, float sh,
            float dx, float dy, float dw, float dh) {
    }

    /** Борды nine-patch по сторонам изображения: левый, верхний, правый, нижний (px). */
    public record NinePatchInsets(int left, int top, int right, int bottom) {

        public static final NinePatchInsets NONE = new NinePatchInsets(0, 0, 0, 0);

        /** Отрицательные значения обрезаются до нуля (размер стороны здесь неизвестен). */
        public NinePatchInsets normalized() {
            return new NinePatchInsets(Math.max(left, 0), Math.max(top, 0),
                    Math.max(right, 0), Math.max(bottom, 0));
        }
    }

    /**
     * Подрегион исходной текстуры в пикселях от её левого верхнего угла. {@code FULL} — вся
     * текстура; смещения обрезаются внутрь источника при раскладке, нулевые/отрицательные
     * размеры означают «до края» по соответствующей оси.
     */
    public record TextureRegion(int u, int v, int width, int height) {

        public static final TextureRegion FULL = new TextureRegion(0, 0, 0, 0);

        /** Отрицательные смещения обрезаются до нуля (размеры разрешаются против фактического размера). */
        public TextureRegion normalized() {
            return new TextureRegion(Math.max(u, 0), Math.max(v, 0), width, height);
        }
    }

    private ImageStretchLayout() {
    }

    /** Диспетчер режима растяжения; {@code mode == null} трактуется как FILL. */
    public static List<StretchPiece> layout(EnumImageStretch mode, int srcWidth, int srcHeight,
                                           float dstWidth, float dstHeight, NinePatchInsets insets) {
        EnumImageStretch effective = (mode == null) ? EnumImageStretch.FILL : mode;
        switch (effective) {
            case FIT:
                return fit(srcWidth, srcHeight, dstWidth, dstHeight);
            case COVER:
                return cover(srcWidth, srcHeight, dstWidth, dstHeight);
            case CENTER:
                return centerCrop(srcWidth, srcHeight, dstWidth, dstHeight);
            case NINEPATCH:
                return ninePatch(srcWidth, srcHeight, insets, dstWidth, dstHeight);
            case FILL:
            default:
                return fill(srcWidth, srcHeight, dstWidth, dstHeight);
        }
    }

    /**
     * Тот же диспетчер с учётом подрегина источника ({@code region == null}/{@link TextureRegion#FULL} — вся текстура).
     * Правило композиции: кроп применяется ПЕРВЫМ — до маппинга режима растяжения и до борд nine-patch;
     * при режиме NINEPATCH борды интерпретируются в пикселях вырезанного фрагмента. Для FILL/FIT/CENTER
     * подрегион ведёт себя как самостоятельное изображение своих нативных размеров. Координаты {@code s*}
     * всех выдаваемых фрагментов уже включают смещение от угла всей текстуры, поэтому рендерер нормализует
     * их против полного размера текстуры без дополнительных правок.
     */
    public static List<StretchPiece> layout(EnumImageStretch mode, TextureRegion region, int srcWidth, int srcHeight,
                                           float dstWidth, float dstHeight, NinePatchInsets insets) {
        if (!validSize(srcWidth, srcHeight, dstWidth, dstHeight)) {
            return Collections.emptyList();
        }
        TextureRegion r = (region == null) ? TextureRegion.FULL : region.normalized();
        int u = Math.min(r.u(), srcWidth);
        int v = Math.min(r.v(), srcHeight);
        int cropW = (r.width() <= 0) ? srcWidth - u : Math.min(r.width(), srcWidth - u);
        int cropH = (r.height() <= 0) ? srcHeight - v : Math.min(r.height(), srcHeight - v);
        if (cropW <= 0 || cropH <= 0) {
            return Collections.emptyList();
        }
        List<StretchPiece> pieces = layout(mode, cropW, cropH, dstWidth, dstHeight, insets);
        if (u == 0 && v == 0) {
            return pieces;
        }
        List<StretchPiece> shifted = new ArrayList<>(pieces.size());
        for (StretchPiece p : pieces) {
            shifted.add(new StretchPiece(p.sx() + u, p.sy() + v, p.sw(), p.sh(), p.dx(), p.dy(), p.dw(), p.dh()));
        }
        return shifted;
    }

    /**
     * COVER: масштабирование с сохранением пропорций до полного заполнения области;
     * избыток обрезается симметрично — видна центральная часть изображения. В отличие от
     * {@link #fit}, пустот не остаётся никогда; в отличие от FILL, пропорции не искажаются.
     */
    public static List<StretchPiece> cover(int srcWidth, int srcHeight, float dstWidth, float dstHeight) {
        if (!validSize(srcWidth, srcHeight, dstWidth, dstHeight)) {
            return Collections.emptyList();
        }
        // Масштаб «впотьмах»: сторона-лимит заполняет обе оси, вторая переполняется.
        float scale = Math.max(dstWidth / (float)srcWidth, dstHeight / (float)srcHeight);
        float w = srcWidth * scale;
        float h = srcHeight * scale;
        float sx0 = ((w - dstWidth) / 2f) / scale;   // смещение обреза в пикселях источника по X
        float sy0 = ((h - dstHeight) / 2f) / scale;  // ... и по Y
        float sw = dstWidth / scale;
        float sh = dstHeight / scale;
        return single(sx0, sy0, sw, sh, 0f, 0f, dstWidth, dstHeight);
    }

    /** FILL: изображение целиком на всю область элемента (историческое поведение). */
    public static List<StretchPiece> fill(int srcWidth, int srcHeight, float dstWidth, float dstHeight) {
        if (!validSize(srcWidth, srcHeight, dstWidth, dstHeight)) {
            return Collections.emptyList();
        }
        return single(0f, 0f, srcWidth, srcHeight, 0f, 0f, dstWidth, dstHeight);
    }

    /** Прямоугольник вписанного с сохранением пропорций по центру области. */
    public static StretchRect computeFitRect(int srcWidth, int srcHeight, float dstWidth, float dstHeight) {
        if (!validSize(srcWidth, srcHeight, dstWidth, dstHeight)) {
            return new StretchRect(0f, 0f, 0f, 0f);
        }
        float scale = Math.min(dstWidth / srcWidth, dstHeight / srcHeight);
        float w = srcWidth * scale;
        float h = srcHeight * scale;
        return new StretchRect((dstWidth - w) / 2f, (dstHeight - h) / 2f, w, h);
    }

    /** FIT: изображение целиком, уменьшенное/увеличенное до вписывания и отцентрованное. */
    public static List<StretchPiece> fit(int srcWidth, int srcHeight, float dstWidth, float dstHeight) {
        StretchRect rect = computeFitRect(srcWidth, srcHeight, dstWidth, dstHeight);
        if (rect.width() <= 0f || rect.height() <= 0f) {
            return Collections.emptyList();
        }
        return single(0f, 0f, srcWidth, srcHeight, rect.x(), rect.y(), rect.width(), rect.height());
    }

    /**
     * CENTER: без масштабирования, по центру. Если изображение больше области —
     * обрезается пересечением с границами элемента (видна центральная часть).
     */
    public static List<StretchPiece> centerCrop(int srcWidth, int srcHeight, float dstWidth, float dstHeight) {
        if (!validSize(srcWidth, srcHeight, dstWidth, dstHeight)) {
            return Collections.emptyList();
        }
        float imgX = (dstWidth - srcWidth) / 2f;
        float imgY = (dstHeight - srcHeight) / 2f;
        // Видимое окно в координатах исходного изображения.
        float leftSrc = Math.max(0f, -imgX);
        float topSrc = Math.max(0f, -imgY);
        float rightSrc = Math.min((float) srcWidth, dstWidth - imgX);
        float bottomSrc = Math.min((float) srcHeight, dstHeight - imgY);
        if (rightSrc <= leftSrc || bottomSrc <= topSrc) {
            return Collections.emptyList();
        }
        float sw = rightSrc - leftSrc;
        float sh = bottomSrc - topSrc;
        return single(leftSrc, topSrc, sw, sh, leftSrc + imgX, topSrc + imgY, sw, sh);
    }

    /** Nine-patch с бордами по сторонам источника (см. класс). */
    public static List<StretchPiece> ninePatch(int srcWidth, int srcHeight, NinePatchInsets insets,
                                              float dstWidth, float dstHeight) {
        return ninePatch(srcWidth, srcHeight,
                insets == null ? 0 : insets.left(), insets == null ? 0 : insets.top(),
                insets == null ? 0 : insets.right(), insets == null ? 0 : insets.bottom(),
                dstWidth, dstHeight);
    }

    /** Nine-patch: {@code l,t,r,b} — пиксели бордов слева/сверху/справа/снизу изображения. */
    public static List<StretchPiece> ninePatch(int srcWidth, int srcHeight,
                                              int leftInset, int topInset, int rightInset, int bottomInset,
                                              float dstWidth, float dstHeight) {
        if (!validSize(srcWidth, srcHeight, dstWidth, dstHeight)) {
            return Collections.emptyList();
        }
        // Борды не могут выходить за пределы стороны и не могут пересекаться парами.
        float l = clampSide(leftInset, srcWidth);
        float r = clampSide(rightInset, srcWidth);
        float t = clampSide(topInset, srcHeight);
        float b = clampSide(bottomInset, srcHeight);
        if (l + r > srcWidth) {
            float k = (float) srcWidth / (l + r);
            l *= k;
            r *= k;
        }
        if (t + b > srcHeight) {
            float k = (float) srcHeight / (t + b);
            t *= k;
            b *= k;
        }

        // Разбивка источника на 3×3: углы — нативный размер, середина может быть нулевой.
        float midW = Math.max(0f, (float) srcWidth - l - r);
        float midH = Math.max(0f, (float) srcHeight - t - b);
        float[] sx = {0f, l, l + midW, (float) srcWidth};
        float[] sy = {0f, t, t + midH, (float) srcHeight};

        // Назначение в элементе: при достаточном размере края сохраняют пиксели и центр
        // поглощает остаток; иначе вся раскладка равномерно сжимается до размеров элемента.
        float totalW = l + midW + r;
        float totalH = t + midH + b;
        float dL, dC, dR, dT, dM, dB;
        if (dstWidth >= totalW && dstHeight >= totalH) {
            dL = l;
            dR = r;
            dC = Math.max(0f, dstWidth - l - r);
            dT = t;
            dB = b;
            dM = Math.max(0f, dstHeight - t - b);
        } else {
            float kx = (totalW > 0f) ? dstWidth / totalW : 0f;
            float ky = (totalH > 0f) ? dstHeight / totalH : 0f;
            dL = l * kx;
            dC = midW * kx;
            dR = r * kx;
            dT = t * ky;
            dM = midH * ky;
            dB = b * ky;
        }
        // Накопительные границы без накопления ошибок округления.
        float[] dx = {0f, dL, dL + dC, dL + dC + dR};
        float[] dy = {0f, dT, dT + dM, dT + dM + dB};

        List<StretchPiece> pieces = new ArrayList<>(9);
        addPiece(pieces, sx[0], sy[0], sx[1] - sx[0], sy[1] - sy[0], dx[0], dy[0], dx[1] - dx[0], dy[1] - dy[0]); // TL
        addPiece(pieces, sx[1], sy[0], sx[2] - sx[1], sy[1] - sy[0], dx[1], dy[0], dx[2] - dx[1], dy[1] - dy[0]); // T
        addPiece(pieces, sx[2], sy[0], sx[3] - sx[2], sy[1] - sy[0], dx[2], dy[0], dx[3] - dx[2], dy[1] - dy[0]); // TR
        addPiece(pieces, sx[0], sy[1], sx[1] - sx[0], sy[2] - sy[1], dx[0], dy[1], dx[1] - dx[0], dy[2] - dy[1]); // L
        addPiece(pieces, sx[1], sy[1], sx[2] - sx[1], sy[2] - sy[1], dx[1], dy[1], dx[2] - dx[1], dy[2] - dy[1]); // C
        addPiece(pieces, sx[2], sy[1], sx[3] - sx[2], sy[2] - sy[1], dx[2], dy[1], dx[3] - dx[2], dy[2] - dy[1]); // R
        addPiece(pieces, sx[0], sy[2], sx[1] - sx[0], sy[3] - sy[2], dx[0], dy[2], dx[1] - dx[0], dy[3] - dy[2]); // BL
        addPiece(pieces, sx[1], sy[2], sx[2] - sx[1], sy[3] - sy[2], dx[1], dy[2], dx[2] - dx[1], dy[3] - dy[2]); // B
        addPiece(pieces, sx[2], sy[2], sx[3] - sx[2], sy[3] - sy[2], dx[2], dy[2], dx[3] - dx[2], dy[3] - dy[2]); // BR
        return pieces;
    }

    /**
     * Раскладка эквивалентна историческому FILL-кваду (один фрагмент на всё изображение
     * и всю область элемента) — рендерер может использовать прежний unit-mesh путь.
     */
    public static boolean isPlainFill(List<StretchPiece> pieces, int srcWidth, int srcHeight,
                                     float dstWidth, float dstHeight) {
        if (pieces == null || pieces.size() != 1) {
            return false;
        }
        StretchPiece p = pieces.get(0);
        final float eps = 0.5f; // допуск субпиксельных ошибок округления
        return Math.abs(p.sx()) < eps && Math.abs(p.sy()) < eps
                && Math.abs(p.sw() - srcWidth) <= eps && Math.abs(p.sh() - srcHeight) <= eps
                && Math.abs(p.dx()) < eps && Math.abs(p.dy()) < eps
                && Math.abs(p.dw() - dstWidth) <= eps && Math.abs(p.dh() - dstHeight) <= eps;
    }

    private static List<StretchPiece> single(float sx, float sy, float sw, float sh,
                                            float dx, float dy, float dw, float dh) {
        List<StretchPiece> list = new ArrayList<>(1);
        addPiece(list, sx, sy, sw, sh, dx, dy, dw, dh);
        return list;
    }

    /** Нулевые/отрицательные фрагменты не выдаются — рисовать их незачем. */
    private static void addPiece(List<StretchPiece> out,
                                 float sx, float sy, float sw, float sh,
                                 float dx, float dy, float dw, float dh) {
        if (sw > 0f && sh > 0f && dw > 0f && dh > 0f) {
            out.add(new StretchPiece(sx, sy, sw, sh, dx, dy, dw, dh));
        }
    }

    private static boolean validSize(int srcWidth, int srcHeight, float dstWidth, float dstHeight) {
        return srcWidth > 0 && srcHeight > 0 && dstWidth > 0f && dstHeight > 0f;
    }

    private static float clampSide(int inset, int sideLength) {
        if (inset <= 0) {
            return 0f;
        }
        return Math.min((float) inset, (float) sideLength);
    }

}
