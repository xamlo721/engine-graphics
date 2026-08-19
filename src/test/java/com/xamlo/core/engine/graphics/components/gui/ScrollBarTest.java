package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.gui.IDragListener;
import com.xamlo.core.engine.graphics.api.gui.IDropTarget;
import com.xamlo.core.engine.graphics.api.gui.IPointerListener;

/**
 * ScrollBar без GL: маппинг value→геометрия бегунка с краевыми случаями,
 * встроенные drag/pointer-слушатели и отсутствие drop-target'овского поведения.
 */
public class ScrollBarTest {

    private static ScrollBar verticalBar(int x, int y, int width, int height) {
        ScrollBar bar = new ScrollBar(EnumOrientation.VERTICAL);
        bar.resize(new UIElementGeometry(x, y, width, height));
        return bar;
    }

    // --- чистая математика ---------------------------------------------------

    @Test
    public void thumbLengthFollowsVisibleFraction() {
        assertEquals(100, ScrollBar.computeThumbLength(200, 0.5f, 0f, 0f), "половина трека");
        assertEquals(38, ScrollBar.computeThumbLength(100, 0.375f, 0f, 0f), "округление доли");
    }

    @Test
    public void minThumbFractionFloorsTheSize() {
        // База 35% = 70px, но минимум 40% от трека = 80px.
        assertEquals(80, ScrollBar.computeThumbLength(200, 0.35f, 0.4f, 0f));
    }

    @Test
    public void minThumbPxFloorAppliesAndCapsAtTrack() {
        assertEquals(180, ScrollBar.computeThumbLength(200, 0.9f, 0f, 60f), "база больше минимума — база");
        assertEquals(50, ScrollBar.computeThumbLength(50, 0.9f, 0f, 60f), "минимум не может превысить трек");
    }

    @Test
    public void degenerateTracksGiveZeroOrFullGeometry() {
        assertEquals(0, ScrollBar.computeThumbLength(0, 0.5f, 0.2f, 10f));
        assertEquals(0, ScrollBar.computeThumbLength(-5, 0.5f, 0.2f, 10f));
        assertEquals(100, ScrollBar.computeThumbLength(100, 1.7f, 0f, 0f), "доля > 1 клампится к треку целиком");
    }

    @Test
    public void thumbPositionScalesLinearlyWithRounding() {
        // L=200, T=40 → полезный ход 160px на диапазон maxValue.
        assertEquals(0, ScrollBar.computeThumbPosition(0f, 100f, 200, 40));
        assertEquals(80, ScrollBar.computeThumbPosition(50f, 100f, 200, 40), "середина диапазона — середина хода");
        assertEquals(40, ScrollBar.computeThumbPosition(25f, 100f, 200, 40));
        assertEquals(160, ScrollBar.computeThumbPosition(99.9f, 100f, 200, 40), "клампируется в конец хода");
        assertEquals(0, ScrollBar.computeThumbPosition(-7f, 100f, 200, 40), "отрицательное значение у начала");
    }

    @Test
    public void thumbPinnedWhenRangeOrUsableIsZero() {
        assertEquals(0, ScrollBar.computeThumbPosition(5f, 0f, 200, 40), "maxValue = 0 — бегунок у начала");
        assertEquals(0, ScrollBar.computeThumbPosition(3f, 10f, 200, 200), "бегунок на всю полосу — без хода");
        assertEquals(0, ScrollBar.computeThumbPosition(3f, 10f, 0, 40));
    }

    @Test
    public void cursorMappingIsProportionalAndClamped() {
        assertEquals(0f, ScrollBar.cursorToValue(-5f, 200, 300f), 1e-6f);
        assertEquals(150f, ScrollBar.cursorToValue(100f, 200, 300f), 1e-6f, "середина трека — середина диапазона");
        assertEquals(300f, ScrollBar.cursorToValue(999f, 200, 300f), 1e-6f, "за краем — максимум");
        assertEquals(0f, ScrollBar.cursorToValue(10f, 0, 300f), 1e-6f);
        assertEquals(0f, ScrollBar.cursorToValue(10f, 200, 0f), 1e-6f, "пустой диапазон — ноль");
    }

    // --- значение и слушатель --------------------------------------------------

    private static class CountingScrollListener implements ScrollBar.OnScroll {
        final AtomicInteger fired = new AtomicInteger();
        float last;

        @Override
        public void onChanged(ScrollBar bar, float newValue) {
            fired.incrementAndGet();
            last = newValue;
        }
    }

    @Test
    public void setValueClampsAndRepositionsThumb() {
        ScrollBar bar = verticalBar(70, 40, 12, 200);
        CountingScrollListener listener = new CountingScrollListener();
        bar.setScrollListener(listener);
        bar.setMaxValue(80f);
        bar.setVisibleFraction(0.5f); // бегунок 100px на треке 200 → ход 100px
        int silentBefore = listener.fired.get();

        bar.setValue(150f); // клампится к максимуму
        assertEquals(80f, bar.getValue(), 1e-6f);
        assertEquals(silentBefore + 1, listener.fired.get());
        UIElementGeometry thumbGeom = bar.getThumb().getGeometry();
        assertEquals(0, thumbGeom.getXCoord());
        assertEquals(100, thumbGeom.getYCoord(), "максимум — конец хода");
        assertEquals(12, thumbGeom.getWidth());
        assertEquals(100, thumbGeom.getHeight());

        bar.setValue(50f);
        assertEquals(50f, bar.getValue(), 1e-6f);
        assertEquals(silentBefore + 2, listener.fired.get());
        // Пропорционально: ход = трек − бегунок = 100px на диапазон maxValue=80 → round(50/80·100)=63.
        assertEquals(63, bar.getThumb().getGeometry().getYCoord());

        bar.setValue(50f); // без изменений — тихо
        assertEquals(silentBefore + 2, listener.fired.get(), "повторное значение не вызывает колбэк");
    }

    @Test
    public void setMaxValueShrinksCurrentValueAndFiresOnce() {
        ScrollBar bar = verticalBar(0, 0, 12, 200);
        CountingScrollListener listener = new CountingScrollListener();
        bar.setScrollListener(listener);
        bar.setMaxValue(100f);
        bar.setVisibleFraction(0.5f);
        bar.setValue(70f);
        int before = listener.fired.get();

        bar.setMaxValue(30f);
        assertEquals(30f, bar.getValue(), 1e-6f, "значение удерживается внутри нового диапазона");
        assertEquals(before + 1, listener.fired.get());

        bar.setMaxValue(30f); // повторный вызов с тем же диапазоном — тихо
        assertEquals(before + 1, listener.fired.get());
    }

    @Test
    public void minThumbSettingsAreGuardedAndApplied() {
        ScrollBar bar = verticalBar(0, 0, 12, 200);
        bar.setMaxValue(80f);
        bar.setVisibleFraction(0.9f); // база 180px
        assertEquals(180, bar.getThumb().getGeometry().getHeight());

        bar.setMinThumbLengthPx(-4f); // отрицательное игнорируется как ноль
        assertEquals(0f, bar.getMinThumbLengthPx(), 1e-6f);
        assertEquals(180, bar.getThumb().getGeometry().getHeight());

        bar.setMinThumbLengthPx(195f);
        assertEquals(195, bar.getThumb().getGeometry().getHeight(), "абсолютный минимум больше базы");

        bar.setMinThumbFraction(-1f);
        assertEquals(0f, bar.getMinThumbFraction(), 1e-6f);
    }

    @Test
    public void orientationSwapsTheTrackAxis() {
        ScrollBar bar = new ScrollBar(EnumOrientation.HORIZONTAL);
        bar.resize(new UIElementGeometry(30, 60, 300, 12));
        bar.setMaxValue(100f);
        bar.setVisibleFraction(0.5f);
        assertEquals(EnumOrientation.HORIZONTAL, bar.getOrientation());
        bar.setValue(50f);

        UIElementGeometry thumbGeom = bar.getThumb().getGeometry();
        assertEquals(75, thumbGeom.getXCoord(), "середина диапазона — середина трека (ход 150px)");
        assertEquals(0, thumbGeom.getYCoord());
        assertEquals(150, thumbGeom.getWidth());
        assertEquals(12, thumbGeom.getHeight());
    }

    // --- встроенные слушатели ввода -------------------------------------------

    private static IDragListener recordingDragListener(java.util.concurrent.atomic.AtomicInteger calls) {
        return new IDragListener() {
            @Override
            public void onDragStart(com.xamlo.core.engine.graphics.api.gui.IDraggable element, float xCoord, float yCoord) {
                calls.incrementAndGet();
            }

            @Override
            public void onDrag(com.xamlo.core.engine.graphics.api.gui.IDraggable element, float xCoord, float yCoord, float deltaX, float deltaY) {
                calls.incrementAndGet();
            }

            @Override
            public void onDrop(com.xamlo.core.engine.graphics.api.gui.IDraggable element, IDropTarget target, float xCoord, float yCoord) {
            }

            @Override
            public void onDragEnd(com.xamlo.core.engine.graphics.api.gui.IDraggable element, float xCoord, float yCoord) {
            }

            @Override
            public void onDragEnterTarget(com.xamlo.core.engine.graphics.api.gui.IDraggable element, IDropTarget target) {
            }

            @Override
            public void onDragLeaveTarget(com.xamlo.core.engine.graphics.api.gui.IDraggable element, IDropTarget target) {
            }
        };
    }

    @Test
    public void dragListenerMapsAbsoluteCursorToValue() {
        ScrollBar bar = verticalBar(70, 40, 12, 200); // abs: X=70..82, Y=40..240
        bar.setMaxValue(80f);
        bar.setVisibleFraction(0.5f); // ход бегунка 100px

        IDragListener listener = bar.getDragListener();
        assertFalse(listener == null);
        listener.onDragStart(bar, 76f, 90f);
        listener.onDrag(null, 76f, 40f + 50f, 0f, 10f);

        assertEquals(20f, bar.getValue(), 1e-6f, "локальная координата 50/200 → четверть диапазона");
        assertEquals(25, bar.getThumb().getGeometry().getYCoord());

        // Курсор за нижним краем трека — клампится к максимуму.
        listener.onDrag(null, 76f, 40f + 300f, 0f, 10f);
        assertEquals(80f, bar.getValue(), 1e-6f);
    }

    @Test
    public void pointerClickJumpsProportionallyAndNullRestoresBuiltIns() {
        ScrollBar bar = verticalBar(70, 40, 12, 200);
        bar.setMaxValue(80f);
        bar.setVisibleFraction(0.5f);

        IPointerListener builtIn = bar.getPointerListener();
        assertNotSame(builtIn, null);
        builtIn.onPointer(bar, 76f, 40f + 100f);
        assertEquals(40f, bar.getValue(), 1e-6f, "клик по середине полосы — середина диапазона");

        IDragListener custom = recordingDragListener(new AtomicInteger());
        bar.setDragListener(custom);
        assertSame(custom, bar.getDragListener());
        bar.setValue(0f);
        ((IDragListener) bar.getDragListener()).onDragStart(bar, 76f, 90f); // кастомный слушатель не двигает значение
        assertEquals(0f, bar.getValue(), 1e-6f);

        bar.setDragListener(null); // null возвращает встроенную логику (как у Slider)
        assertNotSame(custom, bar.getDragListener());
        bar.getDragListener().onDrag(null, 76f, 40f + 100f, 0f, 10f);
        assertEquals(40f, bar.getValue(), 1e-6f, "встроенная логика снова активна после null");
    }

    @Test
    public void barIsNeverADropTarget() {
        ScrollBar bar = verticalBar(0, 0, 12, 200);
        Color trackBefore = bar.getTrackColor();

        IDropTarget asDropTarget = bar;
        asDropTarget.setDropActive(true);

        assertFalse(asDropTarget.isDropActive());
        assertTrue(trackBefore == bar.getTrackColor(), "цвет трека не меняется при попытке подсветки таргета");
    }

}
