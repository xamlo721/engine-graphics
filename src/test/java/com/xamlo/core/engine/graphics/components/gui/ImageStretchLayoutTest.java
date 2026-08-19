package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout.NinePatchInsets;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout.StretchPiece;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout.StretchRect;

/** Чистая геометрия режимов растяжения изображений (E3): все режимы + вырожденные случаи. */
public class ImageStretchLayoutTest {

    private final NinePatchInsets none = NinePatchInsets.NONE;

    @Test
    public void fillModeReturnsSingleFullPieceCoveringBoundsAndImage() {
        List<StretchPiece> pieces = ImageStretchLayout.layout(EnumImageStretch.FILL, 64, 32, 96f, 48f, none);

        assertEquals(1, pieces.size());
        StretchPiece p = pieces.get(0);
        // Источниковое изображение целиком…
        assertEquals(0f, p.sx(), 1e-6);
        assertEquals(0f, p.sy(), 1e-6);
        assertEquals(64f, p.sw(), 1e-6);
        assertEquals(32f, p.sh(), 1e-6);
        // …на всю область элемента.
        assertEquals(0f, p.dx(), 1e-6);
        assertEquals(0f, p.dy(), 1e-6);
        assertEquals(96f, p.dw(), 1e-6);
        assertEquals(48f, p.dh(), 1e-6);
    }

    @Test
    public void nullStretchModeFallsBackToFillLayout() {
        List<StretchPiece> asNull = ImageStretchLayout.layout(null, 8, 4, 16f, 8f, none);
        List<StretchPiece> asFill = ImageStretchLayout.fill(8, 4, 16f, 8f);

        assertEquals(asFill, asNull);
    }

    @Test
    public void fitScalesDownPreservingAspectAndCenters() {
        // Изображение уже области: масштаб по ограничивающей стороне (высота).
        StretchRect rect = ImageStretchLayout.computeFitRect(200, 100, 300f, 300f);
        assertEquals(300f, rect.width(), 1e-5);
        assertEquals(150f, rect.height(), 1e-5);
        assertEquals(0f, rect.x(), 1e-5);
        assertEquals(75f, rect.y(), 1e-5);

        List<StretchPiece> pieces = ImageStretchLayout.fit(200, 100, 300f, 300f);
        assertEquals(1, pieces.size());
        StretchPiece p = pieces.get(0);
        assertEquals(new StretchRect(rect.x(), rect.y(), rect.width(), rect.height()),
                new StretchRect(p.dx(), p.dy(), p.dw(), p.dh()));
        // Образец — всё изображение целиком.
        assertEquals(0f, p.sx(), 1e-6);
        assertEquals(200f, p.sw(), 1e-6);
        assertEquals(100f, p.sh(), 1e-6);
    }

    @Test
    public void fitScalesUpWhenBoxSmallerThanImage() {
        // Масштаб ограничен шириной: 90/400 < 50/200 → пропорции сохраняются ровно.
        List<StretchPiece> pieces = ImageStretchLayout.layout(EnumImageStretch.FIT, 400, 200, 90f, 50f, none);

        assertEquals(1, pieces.size());
        StretchPiece p = pieces.get(0);
        assertEquals(90f, p.dw(), 1e-5);
        assertEquals(45f, p.dh(), 1e-5);
        assertEquals(0f, p.dx(), 1e-5);
        assertEquals((50f - 45f) / 2f, p.dy(), 1e-5);
        float scaleW = p.dw() / p.sw();
        float scaleH = p.dh() / p.sh();
        assertEquals(scaleW, scaleH, 1e-5, "FIT сохраняет единый масштаб по осям");
    }

    @Test
    public void centerCropClipsOversizedImageToCenteredWindow() {
        // Изображение больше области: без масштаба видна центральная часть.
        List<StretchPiece> pieces = ImageStretchLayout.centerCrop(100, 50, 60f, 30f);

        assertEquals(1, pieces.size());
        StretchPiece p = pieces.get(0);
        // Окно в координатах источника: x∈[20..80], y∈[10..40].
        assertEquals(20f, p.sx(), 1e-5);
        assertEquals(10f, p.sy(), 1e-5);
        assertEquals(60f, p.sw(), 1e-5);
        assertEquals(30f, p.sh(), 1e-5);
        // Назначение — вся область элемента от её начала.
        assertEquals(0f, p.dx(), 1e-5);
        assertEquals(0f, p.dy(), 1e-5);
        assertEquals(60f, p.dw(), 1e-5);
        assertEquals(30f, p.dh(), 1e-5);
    }

    @Test
    public void centeredSmallImageStaysUnscaledInsideBounds() {
        List<StretchPiece> pieces = ImageStretchLayout.layout(EnumImageStretch.CENTER, 40, 20, 100f, 60f, none);

        assertEquals(1, pieces.size());
        StretchPiece p = pieces.get(0);
        assertEquals(0f, p.sx(), 1e-5);
        assertEquals(0f, p.sy(), 1e-5);
        assertEquals(40f, p.sw(), 1e-5);
        assertEquals(20f, p.sh(), 1e-5);
        assertEquals((100f - 40f) / 2f, p.dx(), 1e-5);
        assertEquals((60f - 20f) / 2f, p.dy(), 1e-5);
        // Без масштабирования: назначение ровно размером с источник.
        assertEquals(p.sw(), p.dw(), 1e-5);
        assertEquals(p.sh(), p.dh(), 1e-5);
    }

    @Test
    public void zeroOrNegativeDimensionsYieldNoPiecesInEveryMode() {
        int[] srcWidths = {0, -3};
        float[] dstSizes = {0f, -7f};
        EnumImageStretch[] modes = {EnumImageStretch.FILL, EnumImageStretch.FIT, EnumImageStretch.CENTER, EnumImageStretch.NINEPATCH};

        for (int sw : srcWidths) {
            for (float dw : dstSizes) {
                for (EnumImageStretch mode : modes) {
                    assertTrue(ImageStretchLayout.layout(mode, sw, 8, dw, 8f, new NinePatchInsets(1, 1, 1, 1)).isEmpty());
                    assertTrue(ImageStretchLayout.layout(mode, 8, sw, 8f, dw, none).isEmpty());
                }
            }
        }
        assertTrue(ImageStretchLayout.computeFitRect(0, 4, 16f, 8f).width() <= 0f);
    }

    @Test
    public void ninePatchStandardNinePiecesExactSourceDestPairs() {
        // Классический случай: квадрат с бордами по 4px, элемент больше изображения.
        List<StretchPiece> pieces = ImageStretchLayout.ninePatch(32, 32, new NinePatchInsets(4, 4, 4, 4), 96f, 72f);
        assertEquals(9, pieces.size());

        assertPiece(pieces.get(0), 0, 0, 4, 4,   0, 0, 4, 4);     // TL corner — нативный размер
        assertPiece(pieces.get(1), 4, 0, 24, 4,  4, 0, 88, 4);   // верхняя кромка тянется по X
        assertPiece(pieces.get(2), 28, 0, 4, 4,  92, 0, 4, 4);   // TR corner
        assertPiece(pieces.get(3), 0, 4, 4, 24,  0, 4, 4, 64);   // левая кромка тянется по Y
        assertPiece(pieces.get(4), 4, 4, 24, 24, 4, 4, 88, 64);  // центр растянут в обе стороны
        assertPiece(pieces.get(5), 28, 4, 4, 24, 92, 4, 4, 64);  // правая кромка
        assertPiece(pieces.get(6), 0, 28, 4, 4,  0, 68, 4, 4);   // BL corner
        assertPiece(pieces.get(7), 4, 28, 24, 4, 4, 68, 88, 4);  // нижняя кромка
        assertPiece(pieces.get(8), 28, 28, 4, 4, 92, 68, 4, 4);  // BR corner

        // Назначения доходят точно до краёв элемента (правый и нижний углы сетки).
        assertEquals(96f, pieces.get(2).dx() + pieces.get(2).dw(), 1e-4);
        assertEquals(72f, pieces.get(8).dy() + pieces.get(8).dh(), 1e-4);
    }

    @Test
    public void ninePatchZeroInsetsMatchesFillExactly() {
        List<StretchPiece> asNine = ImageStretchLayout.layout(EnumImageStretch.NINEPATCH, 16, 8, 32f, 16f, none);
        List<StretchPiece> asFill = ImageStretchLayout.fill(16, 8, 32f, 16f);

        assertEquals(asFill, asNine);
        assertTrue(ImageStretchLayout.isPlainFill(asNine, 16, 8, 32f, 16f));
    }

    @Test
    public void ninePatchInsetsBeyondHalfSideShrinkProportionally() {
        // Борд больше половины стороны: пары ужимаются до середины, середина исчезает —
        // остаются только четыре угла нативного (уже уменьшенного) размера.
        List<StretchPiece> pieces = ImageStretchLayout.ninePatch(10, 10, new NinePatchInsets(8, 8, 8, 8), 40f, 40f);

        assertEquals(4, pieces.size());
        assertPiece(pieces.get(0), 0, 0, 5, 5,   0, 0, 5, 5);     // TL
        assertPiece(pieces.get(1), 5, 0, 5, 5,   35, 0, 5, 5);    // TR
        assertPiece(pieces.get(2), 0, 5, 5, 5,   0, 35, 5, 5);    // BL
        assertPiece(pieces.get(3), 5, 5, 5, 5,   35, 35, 5, 5);   // BR
    }

    @Test
    public void ninePatchElementSmallerThanImageScalesWholeLayoutDown() {
        // Элемент меньше изображения: вся раскладка (включая углы) сжимается равномерно.
        List<StretchPiece> pieces = ImageStretchLayout.ninePatch(64, 64, new NinePatchInsets(8, 8, 8, 8), 32f, 16f);

        assertEquals(9, pieces.size());
        assertPiece(pieces.get(0), 0, 0, 8, 8,   0, 0, 4, 2);     // TL: нативный угол сжат по осям (×0.5 / ×0.25)
        assertPiece(pieces.get(4), 8, 8, 48, 48, 4, 2, 24, 12);   // центр растянут остатком (после сжатия)
        assertPiece(pieces.get(6), 0, 56, 8, 8,  0, 14, 4, 2);    // BL в нижней строке сетки
        assertPiece(pieces.get(8), 56, 56, 8, 8, 28, 14, 4, 2);   // BR у правого нижнего угла элемента
    }

    @Test
    public void onePixelBordersOnThreeByThreeTextureKeepAllNinePieces() {
        List<StretchPiece> pieces = ImageStretchLayout.ninePatch(3, 3, new NinePatchInsets(1, 1, 1, 1), 9f, 9f);

        assertEquals(9, pieces.size());
        assertPiece(pieces.get(0), 0, 0, 1, 1,   0, 0, 1, 1);   // TL
        assertPiece(pieces.get(4), 1, 1, 1, 1,   1, 1, 7, 7);   // центр поглощает остаток
        assertPiece(pieces.get(8), 2, 2, 1, 1,   8, 8, 1, 1);   // BR
    }

    @Test
    public void isPlainFillDetectsEquivalentLayoutsOnly() {
        int srcW = 64;
        int srcH = 32;

        assertTrue(ImageStretchLayout.isPlainFill(ImageStretchLayout.fill(srcW, srcH, 96f, 48f), srcW, srcH, 96f, 48f));
        // FIT с совпадающими пропорциями эквивалентен FILL — тоже легаси-путь.
        assertTrue(ImageStretchLayout.isPlainFill(ImageStretchLayout.fit(64, 32, 128f, 64f), srcW, srcH, 128f, 64f));
        // CENTER маленького изображения не покрывает область целиком.
        assertFalse(ImageStretchLayout.isPlainFill(ImageStretchLayout.centerCrop(32, 16, 100f, 50f), 32, 16, 100f, 50f));
        // Nine-patch с бордами — нет.
        assertFalse(ImageStretchLayout.isPlainFill(
                ImageStretchLayout.ninePatch(32, 32, new NinePatchInsets(4, 4, 4, 4), 96f, 72f), 32, 32, 96f, 72f));
    }

    private static void assertPiece(StretchPiece p, float sx, float sy, float sw, float sh,
                                   float dx, float dy, float dw, float dh) {
        assertEquals(sx, p.sx(), 1e-4, "sx");
        assertEquals(sy, p.sy(), 1e-4, "sy");
        assertEquals(sw, p.sw(), 1e-4, "sw");
        assertEquals(sh, p.sh(), 1e-4, "sh");
        assertEquals(dx, p.dx(), 1e-4, "dx");
        assertEquals(dy, p.dy(), 1e-4, "dy");
        assertEquals(dw, p.dw(), 1e-4, "dw");
        assertEquals(dh, p.dh(), 1e-4, "dh");
    }

}
