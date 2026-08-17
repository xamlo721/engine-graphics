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
 */
public class ScrollArea extends Widget implements IClippingElement, IWheelTarget {

    /** Пикселей прокрутки на один тик колеса. */
    protected int scrollStep = 40;

    protected Widget contentPane;

    private float scrollX;
    private float scrollY;
    private int contentWidth;
    private int contentHeight;

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

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

}
