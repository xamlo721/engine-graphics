package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.IClippingElement;
import com.xamlo.core.engine.graphics.api.gui.IWheelTarget;
import com.xamlo.core.engine.graphics.api.gui.elements.IWidget;

/**
 * Контейнер с прокруткой: содержимое добавляется в {@link #getContent()},
 * при скролле вся «панорама» сдвигается одним узлом, а отрисовка и hit-test
 * потомков обрезаются по границам области (см. ClipContexts / IClippingElement).
 *
 * Колесо мыши над областью (или её потомками) двигает прокрутку на
 * {@code scrollStep} пикселей за тик: dy > 0 (колесо вверх) — к началу
 * содержимого, dx > 0 — вправо по содержимому.
 *
 * Скроллбары ({@link ScrollBar}) опциональны: включаются явным флагом
 * {@link #enableScrollBar(EnumOrientation, boolean)} и создаются лениво.
 * Полоса рисуется вдоль внутреннего края (правый для вертикальной, нижний
 * для горизонтальной) как ПОТОМОК самой области — то есть ВНУТРИ клип-региона:
 * она не может вылезти за границы окна прокрутки ни визуально, ни в hit-test'е
 * (оба пути используют ClipContexts.effectiveClippedRect), поэтому геометрия
 * существующего клипа не меняется. Правило видимости: полоса показывается только
 * если ось переполнена (maxScroll &gt; 0); при равных размерах окна/контента или
 * max = 0 полоса скрыта даже при установленном флаге. При обоих скроллбарах
 * горизонтальная укорачивается на толщину вертикальной (угол остаётся за ней).
 */
public class ScrollArea extends Widget implements IClippingElement, IWheelTarget {

    /** Пикселей прокрутки на один тик колеса. */
    protected int scrollStep = 40;

    protected Widget contentPane;

    private float scrollX;
    private float scrollY;
    private int contentWidth;
    private int contentHeight;

    /** Толщина полосы по умолчанию (px). */
    public static final int DEFAULT_SCROLLBAR_THICKNESS = 12;

    /** Слой над обычными потомками: полосы рисуются и ловят курсор поверх контента. */
    private static final int SCROLLBAR_Z_INDEX = 1;

    protected int scrollbarThickness = DEFAULT_SCROLLBAR_THICKNESS;

    private boolean verticalScrollBarEnabled;
    private boolean horizontalScrollBarEnabled;
    private ScrollBar verticalScrollBar;
    private ScrollBar horizontalScrollBar;

    public ScrollArea() {
        initializeArea();
    }

    public ScrollArea(IWidget parent) {
        super(parent);
        initializeArea();
    }

    private void initializeArea() {
        this.focusable = false;
        // Панель прозрачная: видна только фоновая текстура/цвет самой области
        this.backgroundColor = new Color(0, 0, 0, 0);
        this.contentPane = new Widget(this);
        contentPane.setBackgroundColor(new Color(0, 0, 0, 0));
        applyScrollPosition();
    }

    @Override
    public boolean clipsChildren() {
        return true;
    }

    /** Контейнер для скроллящегося содержимого. */
    public Widget getContent() {
        return contentPane;
    }

    @Override
    public void resize(UIElementGeometry geometry) {
        super.resize(geometry);
        syncPaneSize();
        setScroll(scrollX, scrollY);
    }

    /**
     * Размер панели — максимум из размеров области и содержимого: hit-test
     * обязан спускаться к детям даже за рамкой «окна», а лишнее обрезает клип.
     */
    private void syncPaneSize() {
        UIElementGeometry areaGeom = getGeometry();
        int paneWidth = Math.max(areaGeom.getWidth(), contentWidth);
        int paneHeight = Math.max(areaGeom.getHeight(), contentHeight);
        if (paneWidth <= 0 || paneHeight <= 0) {
            return;
        }
        UIElementGeometry p = contentPane.getGeometry();
        if (p.getWidth() != paneWidth || p.getHeight() != paneHeight) {
            contentPane.resize(new UIElementGeometry(p.getXCoord(), p.getYCoord(), paneWidth, paneHeight));
        }
    }

    public void setContentSize(int width, int height) {
        this.contentWidth = Math.max(0, width);
        this.contentHeight = Math.max(0, height);
        syncPaneSize();
        setScroll(scrollX, scrollY);
    }

    public int getContentWidth() {
        return contentWidth;
    }

    public int getContentHeight() {
        return contentHeight;
    }

    public float getScrollStep() {
        return scrollStep;
    }

    public void setScrollStep(int step) {
        this.scrollStep = Math.max(1, step);
    }

    public float getMaxScrollX() {
        return Math.max(0f, contentWidth - getGeometry().getWidth());
    }

    public float getMaxScrollY() {
        return Math.max(0f, contentHeight - getGeometry().getHeight());
    }

    public float getScrollX() {
        return scrollX;
    }

    public float getScrollY() {
        return scrollY;
    }

    /** Устанавливает прокрутку (клампится к допустимому диапазону). */
    public void setScroll(float x, float y) {
        this.scrollX = clamp(x, 0f, getMaxScrollX());
        this.scrollY = clamp(y, 0f, getMaxScrollY());
        applyScrollPosition();
        refreshBars();
    }

    @Override
    public void onScrolled(float deltaX, float deltaY) {
        // Колесо вверх (dy > 0) — к началу содержимого; вправо (dx > 0) — по X.
        setScroll(scrollX + deltaX * scrollStep, scrollY - deltaY * scrollStep);
    }

    private void applyScrollPosition() {
        int px = Math.round(-scrollX);
        int py = Math.round(-scrollY);
        if (px != contentPane.getGeometry().getXCoord() || py != contentPane.getGeometry().getYCoord()) {
            UIElementGeometry g = contentPane.getGeometry();
            contentPane.resize(new UIElementGeometry(px, py, g.getWidth(), g.getHeight()));
        }
    }

    // --- Скроллбары -----------------------------------------------------------

    /** Включает/выключает скроллбар заданной оси; экземпляр создаётся лениво и переиспользуется. */
    public void enableScrollBar(EnumOrientation orientation, boolean enabled) {
        if (orientation == EnumOrientation.HORIZONTAL) {
            horizontalScrollBarEnabled = enabled;
        } else {
            verticalScrollBarEnabled = enabled;
        }
        refreshBars();
    }

    public boolean isScrollBarEnabled(EnumOrientation orientation) {
        return orientation == EnumOrientation.HORIZONTAL ? horizontalScrollBarEnabled : verticalScrollBarEnabled;
    }

    public ScrollBar getVerticalScrollBar() {
        return verticalScrollBar;
    }

    public ScrollBar getHorizontalScrollBar() {
        return horizontalScrollBar;
    }

    /** Толщина полосы в px (клампится к ≥ 1). */
    public void setScrollBarThickness(int thickness) {
        this.scrollbarThickness = Math.max(1, thickness);
        refreshBars();
    }

    public int getScrollBarThickness() {
        return scrollbarThickness;
    }

    private ScrollBar ensureBar(boolean vertical) {
        ScrollBar bar = vertical ? verticalScrollBar : horizontalScrollBar;
        if (bar != null) {
            return bar;
        }
        // Потомок самой области: рисуется внутри клип-региона и не двигается при прокрутке.
        bar = new ScrollBar(this, vertical ? EnumOrientation.VERTICAL : EnumOrientation.HORIZONTAL);
        bar.setZIndex(SCROLLBAR_Z_INDEX);
        bar.setVisible(false);
        // Коммит пользовательского ввода: применяем только если область ещё не там —
        // программные изменения прокрутки синхронизируют полосу без обратного цикла.
        bar.setScrollListener((b, v) -> {
            float current = vertical ? scrollY : scrollX;
            if (v == current) {
                return;
            }
            if (vertical) {
                setScroll(scrollX, v);
            } else {
                setScroll(v, scrollY);
            }
        });
        if (vertical) {
            verticalScrollBar = bar;
        } else {
            horizontalScrollBar = bar;
        }
        return bar;
    }

    /** Пересчитывает полосы: видимость по переполнению оси, геометрия вдоль внутреннего края, диапазон/бегунок. */
    private void refreshBars() {
        UIElementGeometry g = getGeometry();
        int w = g.getWidth();
        int h = g.getHeight();
        boolean showV = verticalScrollBarEnabled && w > 0 && h > 0 && getMaxScrollY() > 0f;
        boolean showH = horizontalScrollBarEnabled && w > 0 && h > 0 && getMaxScrollX() > 0f;

        // Фактическая толщина каждой полосы не больше крестового размера области —
        // координаты полосы никогда не уходят за её границу даже на крошечных окнах.
        int tv = Math.min(scrollbarThickness, w);
        int th = Math.min(scrollbarThickness, h);
        syncAxisBar(true, showV, new UIElementGeometry(w - tv, 0, tv, h));
        if (showH) {
            // Нижний край; при видимой вертикальной полосе угол остаётся за ней.
            int width = showV ? Math.max(1, w - tv) : w;
            syncAxisBar(false, true, new UIElementGeometry(0, h - th, width, th));
        } else {
            syncAxisBar(false, false, null);
        }
    }

    private void syncAxisBar(boolean vertical, boolean shown, UIElementGeometry place) {
        ScrollBar existing = vertical ? verticalScrollBar : horizontalScrollBar;
        if (!shown || place == null) {
            if (existing != null) {
                existing.setVisible(false);
            }
            return;
        }
        float max = vertical ? getMaxScrollY() : getMaxScrollX();
        int contentDim = vertical ? contentHeight : contentWidth;
        int viewportDim = vertical ? getGeometry().getHeight() : getGeometry().getWidth();

        ScrollBar bar = ensureBar(vertical);
        if (!geometryEquals(bar.getGeometry(), place)) {
            bar.resize(place);
        }
        bar.setVisible(true);
        bar.setMaxValue(max);
        bar.setVisibleFraction(contentDim > viewportDim && contentDim > 0 ? (float) viewportDim / contentDim : 1f);
        bar.setValue(vertical ? scrollY : scrollX);
    }

    private static boolean geometryEquals(UIElementGeometry a, UIElementGeometry b) {
        return a.getXCoord() == b.getXCoord()
                && a.getYCoord() == b.getYCoord()
                && a.getWidth() == b.getWidth()
                && a.getHeight() == b.getHeight();
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

}
