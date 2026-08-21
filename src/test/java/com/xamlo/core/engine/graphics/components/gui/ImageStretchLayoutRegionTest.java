package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Подрегион источника в раскладке растяжения: кроп применяется первым, координаты
 * фрагментов включают смещение от угла всей текстуры, «до края» и обрезка на выходе.
 */
public class ImageStretchLayoutRegionTest {

    private final float EPS = 1e-3f;

    @Test
    public void fillFullRegionMatchesLegacyFillByteForByte() {
        List<ImageStretchLayout.StretchPiece> legacy = ImageStretchLayout.layout(
                null, 200, 100, 640f, 360f, ImageStretchLayout.NinePatchInsets.NONE);
        List<ImageStretchLayout.StretchPiece> withFull = ImageStretchLayout.layout(
                null, ImageStretchLayout.TextureRegion.FULL, 200, 100, 640f, 360f,
                ImageStretchLayout.NinePatchInsets.NONE);
        assertEquals(legacy.size(), withFull.size());
        for (int i = 0; i < legacy.size(); i++) {
            var a = legacy.get(i);
            var b = withFull.get(i);
            assertEquals(a.sx(), b.sx(), EPS);
            assertEquals(a.sy(), b.sy(), EPS);
            assertEquals(a.sw(), b.sw(), EPS);
            assertEquals(a.sh(), b.sh(), EPS);
            assertEquals(a.dx(), b.dx(), EPS);
            assertEquals(a.dy(), b.dy(), EPS);
            assertEquals(a.dw(), b.dw(), EPS);
            assertEquals(a.dh(), b.dh(), EPS);
        }
    }

    @Test
    public void fillCropsToSubRegionAndShiftsSourceCoords() {
        // логотип-полоса: вырез 75x40 из (50,10) текстуры 200x100 на весь элемент
        List<ImageStretchLayout.StretchPiece> pieces = ImageStretchLayout.layout(
                EnumImageStretch.FILL,
                new ImageStretchLayout.TextureRegion(50, 10, 75, 40), 200, 100, 640f, 360f,
                ImageStretchLayout.NinePatchInsets.NONE);
        assertEquals(1, pieces.size());
        var p = pieces.get(0);
        assertEquals(50f, p.sx(), EPS);
        assertEquals(10f, p.sy(), EPS);
        assertEquals(75f, p.sw(), EPS);
        assertEquals(40f, p.sh(), EPS);
        assertEquals(0f, p.dx(), EPS);
        assertEquals(0f, p.dy(), EPS);
        assertEquals(640f, p.dw(), EPS);
        assertEquals(360f, p.dh(), EPS);
    }

    @Test
    public void zeroSizeMeansToEdgeAndOverflowClamps() {
        // до правого/нижнего края: w/h <= 0 разрешаются против фактического размера
        List<ImageStretchLayout.StretchPiece> toEdge = ImageStretchLayout.layout(
                EnumImageStretch.FILL,
                new ImageStretchLayout.TextureRegion(60, 20, -1, 40), 200, 100, 80f, 40f,
                ImageStretchLayout.NinePatchInsets.NONE);
        assertEquals(1, toEdge.size());
        var e = toEdge.get(0);
        assertEquals(60f, e.sx(), EPS);
        assertEquals(20f, e.sy(), EPS);
        assertEquals(140f, e.sw(), EPS);
        assertEquals(40f, e.sh(), EPS);

        // переполнение обрезаются внутрь источника
        List<ImageStretchLayout.StretchPiece> clamped = ImageStretchLayout.layout(
                EnumImageStretch.FILL,
                new ImageStretchLayout.TextureRegion(190, 95, 50, 50), 200, 100, 32f, 32f,
                ImageStretchLayout.NinePatchInsets.NONE);
        assertEquals(1, clamped.size());
        var c = clamped.get(0);
        assertEquals(190f, c.sx(), EPS);
        assertEquals(95f, c.sy(), EPS);
        assertEquals(10f, c.sw(), EPS);
        assertEquals(5f, c.sh(), EPS);

        // полностью вне текстуры — пусто
        assertTrue(ImageStretchLayout.layout(
                EnumImageStretch.FILL,
                new ImageStretchLayout.TextureRegion(250, 0, 10, 10), 200, 100, 32f, 32f,
                ImageStretchLayout.NinePatchInsets.NONE).isEmpty());
    }

    @Test
    public void fitTreatsCropAsOwnNativeSizedImage() {
        List<ImageStretchLayout.StretchPiece> pieces = ImageStretchLayout.layout(
                EnumImageStretch.FIT,
                new ImageStretchLayout.TextureRegion(50, 10, 75, 40), 200, 100, 200f, 100f,
                ImageStretchLayout.NinePatchInsets.NONE);
        assertEquals(1, pieces.size());
        var p = pieces.get(0);
        // scale = min(200/75, 100/40) = 2.5 -> 187.5 x 100 по центру dst
        assertEquals(187.5f, p.dw(), EPS);
        assertEquals(100f, p.dh(), EPS);
        assertEquals((200f - 187.5f) / 2f, p.dx(), EPS);
        assertEquals(0f, p.dy(), EPS);
        assertEquals(50f, p.sx(), EPS);
        assertEquals(10f, p.sy(), EPS);
    }

    @Test
    public void ninePatchInsetsAreRelativeToCroppedFragment() {
        List<ImageStretchLayout.StretchPiece> full = ImageStretchLayout.layout(
                EnumImageStretch.NINEPATCH,
                new ImageStretchLayout.TextureRegion(0, 0, 60, 30), 200, 100, 120f, 60f,
                new ImageStretchLayout.NinePatchInsets(8, 6, 8, 6));
        // до девяти фрагментов; все исходные координаты лежат внутри выреза
        assertTrue(!full.isEmpty());
        for (var piece : full) {
            assertTrue(piece.sx() >= 0f && piece.sx() + piece.sw() <= 60f, "sx inside crop: " + piece);
            assertTrue(piece.sy() >= 0f && piece.sy() + piece.sh() <= 30f, "sy inside crop: " + piece);
        }

        var shifted = ImageStretchLayout.layout(
                EnumImageStretch.NINEPATCH,
                new ImageStretchLayout.TextureRegion(40, 25, 60, 30), 200, 100, 120f, 60f,
                new ImageStretchLayout.NinePatchInsets(8, 6, 8, 6));
        assertEquals(full.size(), shifted.size());
        for (int i = 0; i < full.size(); i++) {
            assertEquals(full.get(i).sx() + 40f, shifted.get(i).sx(), EPS);
            assertEquals(full.get(i).sy() + 25f, shifted.get(i).sy(), EPS);
        }
    }
}
