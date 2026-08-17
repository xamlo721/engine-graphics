package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Поведение ScrollArea без GL: панель следует за прокруткой, границы клампятся. */
public class ScrollAreaTest {

    private static ScrollArea area(int x, int y, int width, int height) {
        ScrollArea area = new ScrollArea();
        area.resize(new UIElementGeometry(x, y, width, height));
        return area;
    }

    @Test
    public void paneFollowsViewportSizeAfterResize() {
        ScrollArea scrollArea = area(50, 60, 320, 240);

        assertEquals(320, scrollArea.getContent().getGeometry().getWidth());
        assertEquals(240, scrollArea.getContent().getGeometry().getHeight());
    }

    @Test
    public void contentSmallerThanViewportCannotScroll() {
        ScrollArea scrollArea = area(0, 0, 100, 100);
        scrollArea.setContentSize(50, 80);
        scrollArea.setScroll(10f, 10f);

        assertEquals(0f, scrollArea.getScrollX(), 1e-6f);
        assertEquals(0f, scrollArea.getScrollY(), 1e-6f);
        assertEquals(0, scrollArea.getContent().getGeometry().getXCoord());
        assertEquals(0, scrollArea.getContent().getGeometry().getYCoord());
    }

    @Test
    public void wheelUpMovesTowardStartOfContent() {
        ScrollArea scrollArea = area(0, 0, 100, 100);
        scrollArea.setContentSize(300, 300); // maxY = 200
        scrollArea.setScroll(0f, 150f);

        scrollArea.onScrolled(0f, 1f); // колесо вверх → к началу содержимого

        assertEquals(110f, scrollArea.getScrollY(), 1e-6f, "шаг прокрутки по умолчанию 40px");
    }

    @Test
    public void wheelDownClampsAtEndOfContent() {
        ScrollArea scrollArea = area(0, 0, 100, 100);
        scrollArea.setContentSize(300, 300);
        scrollArea.setScroll(0f, 190f);

        scrollArea.onScrolled(0f, -1f); // вниз на один тик: 190+40=230 > max 200

        assertEquals(200f, scrollArea.getScrollY(), 1e-6f);
    }

    @Test
    public void horizontalWheelMovesXRightward() {
        ScrollArea scrollArea = area(0, 0, 100, 100);
        scrollArea.setContentSize(400, 50); // maxX = 300
        scrollArea.setScroll(0f, 0f);

        scrollArea.onScrolled(1f, 0f);

        assertEquals(40f, scrollArea.getScrollX(), 1e-6f);
        assertEquals(0f, scrollArea.getScrollY(), 1e-6f);
    }

    @Test
    public void overscrollRequestsClampToBothBounds() {
        ScrollArea scrollArea = area(0, 0, 100, 100);
        scrollArea.setContentSize(300, 300);

        scrollArea.setScroll(-7f, 9999f);

        assertEquals(0f, scrollArea.getScrollX(), 1e-6f);
        assertEquals(200f, scrollArea.getScrollY(), 1e-6f);
    }

    @Test
    public void panePositionTracksNegativeRoundedScrollExactly() {
        ScrollArea scrollArea = area(10, 20, 100, 100);
        scrollArea.setContentSize(300, 300);
        scrollArea.setScroll(5.4f, 73.6f); // округление до целых пикселей панели

        assertEquals(-5, scrollArea.getContent().getGeometry().getXCoord());
        assertEquals(-74, scrollArea.getContent().getGeometry().getYCoord());
    }

    @Test
    public void shrinkingViewportReclampsCurrentScroll() {
        ScrollArea scrollArea = area(0, 0, 100, 400);
        scrollArea.setContentSize(800, 800); // maxY = 400
        scrollArea.setScroll(0f, 350f);

        scrollArea.resize(new UIElementGeometry(0, 0, 100, 500)); // max падает до 300

        assertEquals(300f, scrollArea.getScrollY(), 1e-6f);
        assertEquals(-300, scrollArea.getContent().getGeometry().getYCoord());
    }

}
