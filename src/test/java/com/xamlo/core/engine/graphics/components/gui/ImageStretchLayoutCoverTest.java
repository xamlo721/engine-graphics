package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Режим COVER: пропорции сохраняются, область заполняется целиком, переполнение
 * обрезается симметрично (видна центральная часть изображения).
 */
public class ImageStretchLayoutCoverTest {

    private final float EPS = 1e-3f;

    @Test
    public void wideImageIntoNarrowAreaCropsHorizontally() {
        // scale = max(100/200, 50/50) = 1 -> видимое окно 100x50 из центра источника 200x50.
        var q = ImageStretchLayout.cover(200, 50, 100f, 50f).get(0);
        assertEquals(50f, q.sx(), EPS);
        assertEquals(0f, q.sy(), EPS);
        assertEquals(100f, q.sw(), EPS);
        assertEquals(50f, q.sh(), EPS);
        assertEquals(0f, q.dx(), EPS);
        assertEquals(0f, q.dy(), EPS);
        assertEquals(100f, q.dw(), EPS);
        assertEquals(50f, q.dh(), EPS);
    }

    @Test
    public void tallImageIntoWideAreaScalesUpAndCropsVertically() {
        // scale = max(200/50, 100/200) = 4 -> источник рисуется как 200x800, видно центральные 200x100.
        var q = ImageStretchLayout.cover(50, 200, 200f, 100f).get(0);
        assertEquals(0f, q.sx(), EPS);
        assertEquals(87.5f, q.sy(), EPS);    // (srcH - dstH/scale)/2 = (200 - 25) / 2
        assertEquals(50f, q.sw(), EPS);      // 200 / 4
        assertEquals(25f, q.sh(), EPS);      // 100 / 4
        assertEquals(0f, q.dx(), EPS);
        assertEquals(0f, q.dy(), EPS);
        assertEquals(200f, q.dw(), EPS);
        assertEquals(100f, q.dh(), EPS);
    }

    @Test
    public void matchingAspectIsByteEquivalentToFillPiece() {
        var q = ImageStretchLayout.cover(200, 100, 640f, 320f).get(0);
        assertEquals(0f, q.sx(), EPS);
        assertEquals(0f, q.sy(), EPS);
        assertEquals(200f, q.sw(), EPS);
        assertEquals(100f, q.sh(), EPS);
        assertEquals(640f, q.dw(), EPS);
        assertEquals(320f, q.dh(), EPS);
    }

    @Test
    public void dispatchAndNullDefaultStayIntact() {
        List<ImageStretchLayout.StretchPiece> c =
                ImageStretchLayout.layout(EnumImageStretch.COVER, 8, 4, 10f, 5f, null);
        // scale = max(1.25, 1.25) = 1.25 -> весь источник, без обрезки (стороны совпадают по аспекту).
        var p = c.get(0);
        assertEquals(8f, p.sw(), EPS);  // аспекты совпадают -> обрезки нет, весь источник
        assertEquals(4f, p.sh(), EPS);
        assertTrueFillStillLegacy();
    }

    private static void assertTrueFillStillLegacy() {
        boolean ok = ImageStretchLayout.isPlainFill(
                ImageStretchLayout.layout(null, 8, 4, 10f, 5f, null), 8, 4, 10f, 5f);
        if (!ok) throw new AssertionError("null mode must stay plain FILL");
    }
}
