package com.xamlo.core.engine.graphics.renderers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.components.gui.ClipContexts;
import com.xamlo.core.engine.graphics.viewport.LogicalViewport;
import com.xamlo.core.engine.graphics.viewport.ViewportSettings;

/**
 * Матрицы UI-проходов и глобальное состояние вьюпорта (E9): legacy-режим побайтово
 * совпадает с историческими константами, заданная сетка масштабирует орту.
 */
public class UiSpaceMatricesTest {

    @AfterEach
    public void resetViewport() {
        ViewportSettings.setActive(null);
    }

    private Vector4f map(Matrix4f m, float x, float y) {
        return new Vector4f(x, y, 0f, 1f).mul(m);
    }

    /** Матрица целого экрана при заданных W/H: углы уходят на края NDC по модулю. */
    private Matrix4f fullScreen(float wPx, float hPx) {
        Matrix4f out = new Matrix4f();
        UiSpaceMatrices.quadMatrix(out, 0f, wPx, hPx, 0f, wPx / 2f, hPx / 2f, wPx, hPx);
        return out;
    }

    @Test
    public void legacyDefaultsMatchHistoricalConstantsWhenInactive() {
        assertEquals(ClipContexts.WINDOW_WIDTH, ViewportSettings.effectiveLogicalWidth());
        assertEquals(ClipContexts.WINDOW_HEIGHT, ViewportSettings.effectiveLogicalHeight());
        assertFalse(ViewportSettings.isActive());
    }

    @Test
    public void activeViewportOverridesEffectiveSizeAndRestoresOnReset() {
        ViewportSettings.setActive(new LogicalViewport(640, 360));
        assertTrue(ViewportSettings.isActive());
        assertEquals(640, ViewportSettings.effectiveLogicalWidth());
        assertEquals(360, ViewportSettings.effectiveLogicalHeight());
        ViewportSettings.setActive(null);
        assertEquals(ClipContexts.WINDOW_WIDTH, ViewportSettings.effectiveLogicalWidth(), "сброс возвращает legacy");
    }

    @Test
    public void fullScreenCornersReachNdcEdgesForAnyGrid() {
        for (float[] grid : new float[][] {{1920f, 1080f}, {640f, 360f}, {100f, 75f}}) {
            Matrix4f m = fullScreen(grid[0], grid[1]);
            Vector4f bl = map(m, -0.5f * grid[0] / grid[0], -0.5f * grid[1] / grid[1]); // вершина «центр-минус» в единицах меша ниже
            Vector4f br = map(m, 0.5f, -0.5f);
            Vector4f tl = map(m, -0.5f, 0.5f);
            Vector4f tr = map(m, 0.5f, 0.5f);
            assertEquals(1f, Math.abs(bl.x), 1e-4f, "левый край NDC: " + java.util.Arrays.toString(new float[]{bl.x}));
            assertEquals(1f, Math.abs(br.x), 1e-4f);
            assertEquals(1f, Math.abs(tl.y), 1e-4f);
            assertEquals(1f, Math.abs(tr.y), 1e-4f);
        }
    }

    @Test
    public void halfSizeElementCenteredMapsToHalfNdcExtent() {
        Matrix4f m = new Matrix4f();
        UiSpaceMatrices.quadMatrix(m, 0f, 640f, 360f, 0f, 320f, 180f, 320f, 90f);
        // Правая кромка элемента в design x = 320+160=480 из [0..640] → ±0.5 по X от центра.
        Vector4f rightMid = map(m, 0.5f, -0.5f);
        assertEquals(0.5f, Math.abs(rightMid.x), 1e-4f, "половина ширины сетки — половина NDC: " + rightMid);
    }

    @Test
    public void logicalViewportMappingMathAndDegenerates() {
        LogicalViewport v = new LogicalViewport(1920, 1080);
        assertEquals(2f, v.scaleX(3840f), 1e-6f);
        assertEquals(1f, v.scaleY(1080f), 1e-6f);
        assertEquals(0f, v.scaleX(-7f), 1e-6f, "вырожденный физический размер");
        assertEquals(960f, v.toDesignX(1920f, 3840f), 1e-4f);
        assertEquals(0f, v.toDesignX(5f, 0f), 1e-6f, "нулевой физ. размер мапится в ноль");
        // Неравномерное отношение сторон не смешивает оси.
        LogicalViewport aniso = new LogicalViewport(100, 50);
        assertEquals(75f, aniso.toDesignX(75f, 100f), 1e-4f, "ось X использует только свои размеры");
        assertEquals(25f, aniso.toDesignY(25f, 50f), 1e-4f, "ось Y использует только свои размеры");

        assertTrue(v.equals(new LogicalViewport(1920, 1080)));
        assertNotEquals(v, new LogicalViewport(1920, 1));
    }
}
