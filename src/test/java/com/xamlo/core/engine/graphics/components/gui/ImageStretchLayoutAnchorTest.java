package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout.StretchPiece;

/**
 * Якорь обрезания/выравнивания (COVER/CENTER/FIT): углы и середины, эквивалентность
 * центрального якоря историческим формулам, игнорирование FILL/NINEPATCH, климпинг.
 */
public class ImageStretchLayoutAnchorTest {

    private final float EPS = 1e-3f;

    // ---------- COVER ----------

    @Test
    public void coverTopLeftAnchorKeepsSourceCorner() {
        // scale = max(100/200, 50/50) = 1 -> окно 100x50 прижато к левому верхнему углу источника.
        var q = ImageStretchLayout.cover(200, 50, 100f, 50f, 0f, 0f).get(0);
        assertEquals(0f, q.sx(), EPS);
        assertEquals(0f, q.sy(), EPS);
        assertEquals(100f, q.sw(), EPS);
        assertEquals(50f, q.sh(), EPS);
    }

    @Test
    public void coverBottomRightAnchorCropsFromOppositeEdges() {
        var q = ImageStretchLayout.cover(200, 50, 100f, 50f, 1f, 1f).get(0);
        assertEquals(100f, q.sx(), EPS);   // обрезана вся левая половина
        assertEquals(0f, q.sy(), EPS);     // по Y переполнения нет — якорь не действует
        assertEquals(100f, q.sw(), EPS);
        assertEquals(50f, q.sh(), EPS);
    }

    @Test
    public void coverAnchorsMixAxesIndependently() {
        // src 240x120 -> dst 80x80: scale = max(80/240, 80/120) = 2/3; w = 160 > 80 (обрезка по X), h = 80 ровно.
        var tr = ImageStretchLayout.cover(240, 120, 80f, 80f, 1f, 0f).get(0);
        float sw = 80f / (2f / 3f);                         // 120 px источника видно по X
        assertEquals(240f - sw, tr.sx(), EPS);              // якорь x=1: обрезаются ЛЕВЫЕ (240-120)/... пиксели
        assertEquals(0f, tr.sy(), EPS);                    // переполнения по Y нет — якорь не действует
        assertEquals(sw, tr.sw(), EPS);
        assertEquals(80f / (2f / 3f), tr.sh(), EPS);

        var bl = ImageStretchLayout.cover(240, 120, 80f, 80f, 0f, 1f).get(0);
        assertEquals(0f, bl.sx(), EPS);                     // якорь x=0: прижат к левому краю источника
        assertEquals(0f, bl.sy(), EPS);
    }

    @Test
    public void coverCenterAnchorMatchesLegacyCover() {
        List<StretchPiece> legacy = ImageStretchLayout.cover(200, 50, 100f, 50f);
        List<StretchPiece> anchored = ImageStretchLayout.cover(200, 50, 100f, 50f, 0.5f, 0.5f);
        for (int i = 0; i < legacy.size(); i++) {
            StretchPiece a = legacy.get(i), b = anchored.get(i);
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

    // ---------- CENTER ----------

    @Test
    public void centerCropOverflowHonorsAnchorPerAxis() {
        var tl = ImageStretchLayout.centerCrop(300, 60, 100f, 30f, 0f, 0f).get(0);
        assertEquals(0f, tl.sx(), EPS);      // видно левые 100 пикселей
        assertEquals(0f, tl.sy(), EPS);
        assertEquals(100f, tl.sw(), EPS);
        assertEquals(30f, tl.sh(), EPS);

        var br = ImageStretchLayout.centerCrop(300, 60, 100f, 30f, 1f, 1f).get(0);
        assertEquals(200f, br.sx(), EPS);    // обрезаны левые 200 -> виден правый край
        assertEquals(30f, br.sy(), EPS);     // по Y переполнение 30: якорь 1 => верхние уходят за край
        assertEquals(100f, br.sw(), EPS);
        assertEquals(30f, br.sh(), EPS);
    }

    @Test
    public void centerCropWithoutOverflowAlignsByAnchor() {
        // Изображение меньше области — работает как выравнивание letterbox'а.
        var leftTop = ImageStretchLayout.centerCrop(50, 20, 200f, 100f, 0f, 0f).get(0);
        assertEquals(0f, leftTop.dx(), EPS);
        assertEquals(0f, leftTop.dy(), EPS);
        assertEquals(50f, leftTop.dw(), EPS);
        assertEquals(20f, leftTop.dh(), EPS);

        var rightBottom = ImageStretchLayout.centerCrop(50, 20, 200f, 100f, 1f, 1f).get(0);
        assertEquals(150f, rightBottom.dx(), EPS);   // 200 - 50
        assertEquals(80f, rightBottom.dy(), EPS);    // 100 - 20
        assertEquals(50f, rightBottom.dw(), EPS);
        assertEquals(20f, rightBottom.dh(), EPS);
    }

    @Test
    public void centerCropCenterMatchesLegacyCenterCrop() {
        List<StretchPiece> legacy = ImageStretchLayout.centerCrop(300, 60, 100f, 30f);
        StretchPiece a = legacy.get(0), b = ImageStretchLayout.centerCrop(300, 60, 100f, 30f, 0.5f, 0.5f).get(0);
        for (int i = 0; i < 4; i++) {
            float la = new float[] {a.sx(), a.sy(), a.sw(), a.sh()}[i];
            float lb = new float[] {b.sx(), b.sy(), b.sw(), b.sh()}[i];
            assertEquals(la, lb, EPS);
        }
    }

    // ---------- FIT ----------

    @Test
    public void fitLetterboxMarginsDistributeByAnchor() {
        var tl = ImageStretchLayout.fit(200, 100, 300f, 300f, 0f, 0f).get(0);
        // масштаб по ограничивающей стороне: w=300*? -> h=150... проверим формулой.
        float scale = Math.min(300f / 200f, 300f / 100f);   // 1.5
        assertEquals(0f, tl.dx(), EPS);                     // прижато к левому краю
        assertEquals(0f, tl.dy(), EPS);                    // и верхнему
        assertEquals(200f * scale, tl.dw(), EPS);
        assertEquals(100f * scale, tl.dh(), EPS);

        var br = ImageStretchLayout.fit(200, 100, 300f, 300f, 1f, 1f).get(0);
        assertEquals(300f - 200f * scale, br.dx(), EPS);    // свободное поле слева целиком ушло влево
        assertEquals(300f - 100f * scale, br.dy(), EPS);
        assertEquals(200f * scale, br.dw(), EPS);
        assertEquals(100f * scale, br.dh(), EPS);
    }

    @Test
    public void fitCenterMatchesLegacyFit() {
        List<StretchPiece> legacy = ImageStretchLayout.fit(200, 100, 300f, 300f);
        StretchPiece a = legacy.get(0), b = ImageStretchLayout.fit(200, 100, 300f, 300f, 0.5f, 0.5f).get(0);
        for (int i = 0; i < 4; i++) {
            float la = new float[] {a.sx(), a.sy(), a.sw(), a.sh()}[i];
            float lb = new float[] {b.sx(), b.sy(), b.sw(), b.sh()}[i];
            assertEquals(la, lb, EPS);
        }
    }

    // ---------- Диспетчер: якорь игнорируется FILL/NINEPATCH и пробрасывается в region-вариант ----------

    @Test
    public void dispatcherIgnoresAnchorForFillAndNinePatch() {
        var fillAnyA = ImageStretchLayout.layout(EnumImageStretch.FILL, 64, 32, 96f, 48f, null, 0f, 0f);
        var fillAnyB = ImageStretchLayout.layout(null, 64, 32, 96f, 48f, null, 1f, 0.7f);
        assertEquals(fillAnyA, fillAnyB);

        var npA = ImageStretchLayout.layout(EnumImageStretch.NINEPATCH, 64, 32, 96f, 48f,
                new ImageStretchLayout.NinePatchInsets(8, 4, 8, 4), 0f, 0f);
        var npB = ImageStretchLayout.layout(EnumImageStretch.NINEPATCH, 64, 32, 96f, 48f,
                new ImageStretchLayout.NinePatchInsets(8, 4, 8, 4), 0.3f, 0.9f);
        assertEquals(npA, npB);
    }

    @Test
    public void dispatcherWithRegionPassesAnchorThrough() {
        // Подрегион u=40 шириной 160 из источника 200x50; область 80x40:
        // scale = max(80/160, 40/50) = 0.8 -> w = 128 > 80 (обрезка по X), h = 40 ровно.
        List<StretchPiece> pieces = ImageStretchLayout.layout(EnumImageStretch.COVER,
                new ImageStretchLayout.TextureRegion(40, 0, 160, 50), 200, 50, 80f, 40f, null, 1f, 0f);
        StretchPiece p = pieces.get(0);
        float sw = 80f / 0.8f;                               // 100 px видно по X
        assertEquals(40f + (160f - sw), p.sx(), EPS);        // якорь x=1 + смещение региона от угла текстуры
        assertEquals(0f, p.sy(), EPS);
        assertEquals(sw, p.sw(), EPS);
        assertEquals(40f / 0.8f, p.sh(), EPS);               // вся высота подрегиона
    }

    // ---------- Климпинг якоря ----------

    @Test
    public void outOfRangeAnchorsClampToUnitSquareViaPublicPath() {
        var clampedTl = ImageStretchLayout.cover(200, 50, 100f, 50f, -3f, 9f).get(0);
        var tl = ImageStretchLayout.cover(200, 50, 100f, 50f, 0f, 0f).get(0);
        assertEquals(tl.sx(), clampedTl.sx(), EPS);
        assertEquals(tl.sy(), clampedTl.sy(), EPS);

        assertTrue(new ImageStretchLayout.StretchAnchor(-1f, 2f).clamped().equals(
                new ImageStretchLayout.StretchAnchor(0f, 1f)));
        assertTrue(Float.isNaN(ImageStretchLayout.StretchAnchor.CENTER.x()) == false);
    }
}
