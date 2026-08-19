package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.components.ICamera;
import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.components.DefaultSceneController;
import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.device.events.MouseDragAndropEvent;
import com.xamlo.engine.device.events.EnumDragPhase;
import com.xamlo.engine.device.events.MouseScrollEvent;

/**
 * Скроллбары ScrollArea без GL: ленивое создание, геометрия вдоль внутреннего края,
 * правило «скрыт при равных размерах», согласованность колеса/drag/программного setScroll.
 */
public class ScrollAreaScrollbarTest {

    private static ScrollArea newArea(int x, int y, int w, int h) {
        ScrollArea area = new ScrollArea();
        area.resize(new UIElementGeometry(x, y, w, h));
        return area;
    }

    /** Инвариант согласованности: значение полосы == прокрутка области и бегунок на своём месте. */
    private void assertMirrorConsistency(ScrollArea area, boolean checkVertical, boolean checkHorizontal) {
        if (checkVertical) {
            ScrollBar bar = area.getVerticalScrollBar();
            assertTrue(bar != null && bar.isVisible(), "вертикальная полоса видна");
            float max = area.getMaxScrollY();
            assertEquals(max > 0f ? area.getScrollY() : 0f, bar.getValue(), 1e-6f, "значение полосы зеркалит прокрутку Y");
            int trackLen = bar.getGeometry().getHeight();
            int thumbLen = Math.max(0, Math.min(trackLen, bar.getThumb().getGeometry().getHeight()));
            int expectedPos = ScrollBar.computeThumbPosition(area.getScrollY(), max, trackLen, thumbLen);
            assertEquals(expectedPos, bar.getThumb().getGeometry().getYCoord(), "бегунок на позиции по общей формуле");
        }
        if (checkHorizontal) {
            ScrollBar bar = area.getHorizontalScrollBar();
            assertTrue(bar != null && bar.isVisible());
            float max = area.getMaxScrollX();
            assertEquals(max > 0f ? area.getScrollX() : 0f, bar.getValue(), 1e-6f, "значение полосы зеркалит прокрутку X");
            int trackLen = bar.getGeometry().getWidth();
            int thumbLen = Math.max(0, Math.min(trackLen, bar.getThumb().getGeometry().getWidth()));
            int expectedPos = ScrollBar.computeThumbPosition(area.getScrollX(), max, trackLen, thumbLen);
            assertEquals(expectedPos, bar.getThumb().getGeometry().getXCoord(), "бегунок на позиции по общей формуле");
        }
    }

    // --- создание и геометрия --------------------------------------------------

    @Test
    public void barsAreNotCreatedUntilEnabledAndOverflowAppears() {
        ScrollArea area = newArea(0, 0, 100, 100);
        area.setContentSize(300, 300); // переполнение есть, но флаги выключены

        assertNull(area.getVerticalScrollBar());
        assertNull(area.getHorizontalScrollBar());
        assertEquals(1, area.getChildElements().size(), "до включения полос потомок один — панель контента");
        assertSame(area.getContent(), area.getChildElements().get(0));
    }

    @Test
    public void verticalBarSitsOnRightInnerEdgeAndTracksWheelTicks() {
        ScrollArea area = newArea(50, 60, 100, 30);
        area.setContentSize(80, 90); // только Y переполнен: maxY = 60
        area.enableScrollBar(EnumOrientation.VERTICAL, true);

        ScrollBar bar = area.getVerticalScrollBar();
        assertTrue(bar.isVisible());
        UIElementGeometry place = bar.getGeometry();
        assertEquals(88, place.getXCoord(), "правый внутренний край (толщина по умолчанию 12)");
        assertEquals(0, place.getYCoord());
        assertEquals(12, place.getWidth());
        assertEquals(30, place.getHeight());
        assertEquals(1, bar.getZIndex(), "полоса над обычным контентом независимо от порядка добавления");

        // Бегунок следует за колесом без какого-либо пользовательского ввода.
        area.onScrolled(0f, -1f); // шаг 40px вниз
        assertEquals(40f, area.getScrollY(), 1e-6f);
        assertMirrorConsistency(area, true, false);

        area.onScrolled(0f, -1f); // ещё тик — клампится к максимуму
        assertEquals(60f, area.getScrollY(), 1e-6f);
        int trackLen = bar.getGeometry().getHeight();
        int thumbLen = Math.max(0, Math.min(trackLen, bar.getThumb().getGeometry().getHeight()));
        assertEquals(ScrollBar.computeThumbPosition(60f, 60f, trackLen, thumbLen), bar.getThumb().getGeometry().getYCoord(),
                "в конце прокрутки бегунок в самом низу трека");
    }

    @Test
    public void equalSizesHideBarsAndInstanceSurvivesOverflowToggles() {
        ScrollArea area = newArea(0, 0, 100, 100);
        area.setContentSize(100, 100); // равные размеры: переполнения нет
        area.enableScrollBar(EnumOrientation.VERTICAL, true);
        area.enableScrollBar(EnumOrientation.HORIZONTAL, true);

        assertNull(area.getVerticalScrollBar(), "max = 0 — полоса не создаётся и не показывается даже при флаге");
        assertNull(area.getHorizontalScrollBar());

        area.setContentSize(100, 150); // появилось переполнение Y (maxY = 50)
        ScrollBar created = area.getVerticalScrollBar();
        assertTrue(created.isVisible());
        UIElementGeometry place = created.getGeometry();
        assertEquals(88, place.getXCoord());
        assertEquals(0, place.getYCoord());
        assertEquals(12, place.getWidth());
        assertEquals(100, place.getHeight());

        area.setScroll(0f, 25f);
        int trackLen = created.getGeometry().getHeight();
        int thumbLen = Math.max(0, Math.min(trackLen, created.getThumb().getGeometry().getHeight()));
        assertEquals(ScrollBar.computeThumbPosition(25f, 50f, trackLen, thumbLen), created.getThumb().getGeometry().getYCoord());

        area.setContentSize(100, 100); // переполнение исчезло — та же инстанция прячется
        assertSame(created, area.getVerticalScrollBar(), "инстанция переиспользуется, а не пересоздаётся");
        assertFalse(created.isVisible());
    }

    @Test
    public void horizontalBarSitsOnBottomEdgeAndCornerBelongsToVertical() {
        ScrollArea area = newArea(0, 0, 200, 160);
        area.setContentSize(400, 320); // обе оси переполнены
        area.enableScrollBar(EnumOrientation.VERTICAL, true);
        area.enableScrollBar(EnumOrientation.HORIZONTAL, true);

        UIElementGeometry vPlace = area.getVerticalScrollBar().getGeometry();
        assertEquals(188, vPlace.getXCoord());
        assertEquals(0, vPlace.getYCoord());
        assertEquals(12, vPlace.getWidth());
        assertEquals(160, vPlace.getHeight(), "вертикальная полоса на всю высоту");

        UIElementGeometry hPlace = area.getHorizontalScrollBar().getGeometry();
        assertEquals(0, hPlace.getXCoord());
        assertEquals(148, hPlace.getYCoord());
        assertEquals(188, hPlace.getWidth(), "горизонтальная укорочена: угол остаётся за вертикальной");
        assertEquals(12, hPlace.getHeight());

        area.enableScrollBar(EnumOrientation.VERTICAL, false);
        assertEquals(200, area.getHorizontalScrollBar().getGeometry().getWidth(),
                "без вертикальной полосы горизонтальная занимает весь нижний край");
    }

    @Test
    public void thicknessIsClampedAndMovesPlacement() {
        ScrollArea area = newArea(0, 0, 40, 80);
        area.setContentSize(10, 300); // maxY = 220
        area.setScrollBarThickness(99);
        area.enableScrollBar(EnumOrientation.VERTICAL, true);

        UIElementGeometry place = area.getVerticalScrollBar().getGeometry();
        assertEquals(0, place.getXCoord(), "толщина не может превысить ширину области — полоса во всю ширину");
        assertEquals(40, place.getWidth());
        assertEquals(80, place.getHeight());
        assertEquals(99, area.getScrollBarThickness());

        area.setScrollBarThickness(0);
        assertEquals(1, area.getScrollBarThickness(), "клампится к минимуму 1px");
        place = area.getVerticalScrollBar().getGeometry();
        assertEquals(39, place.getXCoord());
        assertEquals(1, place.getWidth());
    }

    // --- согласованность путей: колесо / drag / программный setScroll ----------

    private static ScrollArea fullOverflowArea(boolean withContentRow) {
        ScrollArea area = newArea(100, 100, 100, 100);
        area.setContentSize(300, 300); // maxX = maxY = 200
        if (withContentRow) {
            Widget row = new Widget(area.getContent());
            row.resize(new UIElementGeometry(5, 5, 60, 20));
        }
        return area;
    }

    @Test
    public void wheelDragAndProgrammaticPathsLandOnSameClampedState() {
        ScrollArea wheeled = fullOverflowArea(false);
        wheeled.enableScrollBar(EnumOrientation.VERTICAL, true);
        for (int i = 0; i < 8 && wheeled.getMaxScrollY() > wheeled.getScrollY(); i++) {
            wheeled.onScrolled(0f, -1f);
        }
        float endX = wheeled.getScrollX();
        float endY = wheeled.getScrollY();
        assertEquals(200f, endY, 1e-6f, "колесо докатывается ровно до конца");

        ScrollArea dragged = fullOverflowArea(true);
        dragged.enableScrollBar(EnumOrientation.VERTICAL, true);
        ScrollBar bar = dragged.getVerticalScrollBar();
        com.xamlo.core.engine.graphics.api.gui.IDragListener listener = bar.getDragListener();
        // Курсор в середине трека, затем за нижним краем — значение зеркалит колесовый результат.
        listener.onDragStart(bar, 194f, 150f);
        listener.onDrag(null, 194f, 150f, 0f, 30f);
        assertMirrorConsistency(dragged, true, false);
        listener.onDrag(null, 194f, 100f + 100f + 50f, 0f, 80f); // далеко за концом → максимум

        assertEquals(endX, dragged.getScrollX(), 1e-6f, "drag и колесо дают одинаковый X (не тронут)");
        assertEquals(endY, dragged.getScrollY(), 1e-6f, "drag и колесо дают одинаково клампированный Y");
        assertEquals(wheeled.getContent().getGeometry().getYCoord(), dragged.getContent().getGeometry().getYCoord());

        // Программный setScroll(...): панель контента и бегунок остаются согласованы с прокруткой.
        dragged.setScroll(77f, 33f);
        assertMirrorConsistency(dragged, true, false);
    }

    // --- контроллер сцены: жест drag через реальные события ----------------------

    /** Сцена из плоского списка; hit-test спускается к детям в обратном порядке (как в реальной сцене). */
    private static class TestScene implements IScene {
        private final List<IUIElement> elements = new ArrayList<>();

        void add(IUIElement element) {
            elements.add(element);
        }

        @Override
        public void load() {
        }

        @Override
        public List<AbstractSceneElement> getRenderableObject() {
            return new ArrayList<>();
        }

        @Override
        public IUIElement findElementAt(float xCoord, float yCoord) {
            for (int i = elements.size() - 1; i >= 0; i--) {
                IUIElement hit = hitTest(elements.get(i), xCoord, yCoord);
                if (hit != null) {
                    return hit;
                }
            }
            return null;
        }

        private IUIElement hitTest(IUIElement element, float x, float y) {
            if (!element.containsPoint(x, y)) {
                return null;
            }
            List<IUIElement> children = element.getChildElements();
            for (int i = children.size() - 1; i >= 0; i--) {
                IUIElement hit = hitTest(children.get(i), x, y);
                if (hit != null) {
                    return hit;
                }
            }
            return element;
        }

        @Override
        public List<IUIElement> getGuiElements() {
            return elements;
        }

        @Override
        public Matrix4f getProjectionMatrix() {
            return new Matrix4f();
        }

        @Override
        public void setProjectionMatrix(Matrix4f transformMatrix) {
        }

        @Override
        public void unload() {
        }
    }

    /** Камера-заглушка: в тестах drag/скролла камерой не управляем. */
    private static class NoopCamera implements ICamera {
        @Override
        public Matrix4f getViewMatrix() {
            return new Matrix4f();
        }

        @Override
        public void rotate(Vector3f vector) {
        }

        @Override
        public void move(Vector3f vector) {
        }

        @Override
        public Vector3f getPosition() {
            return new Vector3f();
        }

        @Override
        public Vector3f getRotation() {
            return new Vector3f();
        }

        @Override
        public float getFov() {
            return 70f;
        }

        @Override
        public void setFov(float fov) {
        }

        @Override
        public float getAspectRatio() {
            return 16f / 9f;
        }

        @Override
        public void setAspectRatio(int width, int height) {
        }

        @Override
        public float getNearDistance() {
            return 0.2f;
        }

        @Override
        public void setNearDistance(float far) {
        }

        @Override
        public float getFarDistance() {
            return 1000f;
        }

        @Override
        public void setFarDistance(float near) {
        }
    }

    private static DefaultSceneController controllerFor(TestScene scene) {
        DefaultSceneController controller = new DefaultSceneController();
        controller.setScene(scene);
        controller.setCamera(new NoopCamera());
        return controller;
    }

    /**
     * Полный цикл жеста через события контроллера: START на полосе захватывает её как IDraggable,
     * DRAG двигает прокрутку по общему маппингу, END завершает без drop-подсветки трека.
     */
    @Test
    public void dragGestureThroughControllerMovesScrollAndNeverHighlightsTrack() {
        TestScene scene = new TestScene();
        ScrollArea area = fullOverflowArea(true); // с рядом контента для отрицательного контроля
        area.enableScrollBar(EnumOrientation.VERTICAL, true);
        scene.add(area);
        DefaultSceneController controller = controllerFor(scene);

        ScrollBar bar = area.getVerticalScrollBar();
        UIElementGeometry place = bar.getGeometry();
        // Полоса в абсолютных координатах x ∈ [188..200], y ∈ [100..200] (область в (100, 100)).
        float midX = 194f;
        assertEquals(188f, bar.getAbsX(), 1e-6f, "полоса на правом внутреннем крае");
        assertTrue(midX >= bar.getAbsX() && midX < bar.getAbsX() + place.getWidth());

        controller.onMouseDragAndDropEvent(new MouseDragAndropEvent(midX, 150f, 6f, 6f, EnumMouseButtons.MOUSE_BUTTON_1, EnumDragPhase.START));

        for (float cursorY : new float[] {130f, 170f, 195f}) {
            controller.onMouseDragAndDropEvent(new MouseDragAndropEvent(midX, cursorY, 0f, 10f, EnumMouseButtons.MOUSE_BUTTON_1, EnumDragPhase.DRAG));
            int trackLen = bar.getGeometry().getHeight();
            float expected = ScrollBar.cursorToValue(cursorY - bar.getAbsY(), trackLen, area.getMaxScrollY());
            assertEquals(expected, bar.getValue(), 1e-6f, "каждый DRAG кадра — общий пропорциональный маппинг");
            assertMirrorConsistency(area, true, false);
            assertEquals((int) Math.round(-area.getScrollY()), area.getContent().getGeometry().getYCoord(),
                    "панель контента следует за прокруткой в каждом кадре жеста");
        }

        Color trackBeforeGesture = bar.getTrackColor();
        controller.onMouseDragAndDropEvent(new MouseDragAndropEvent(midX, 195f, 0f, 4f, EnumMouseButtons.MOUSE_BUTTON_1, EnumDragPhase.END));
        assertSame(trackBeforeGesture, bar.getTrackColor(), "подсветка drop-target не активировалась на треке");
        assertFalse(bar.isDropActive());
    }

    @Test
    public void gestureOverPlainContentRowDoesNotStartDragging() {
        TestScene scene = new TestScene();
        ScrollArea area = fullOverflowArea(true); // ряд контента в локальных (5..65, 5..25)
        area.enableScrollBar(EnumOrientation.VERTICAL, true);
        scene.add(area);
        DefaultSceneController controller = controllerFor(scene);

        float before = area.getScrollY();
        // Жест начинается над строкой контента — она не IDraggable.
        controller.onMouseDragAndDropEvent(new MouseDragAndropEvent(110f, 108f, 6f, 6f, EnumMouseButtons.MOUSE_BUTTON_1, EnumDragPhase.START));
        for (float cursorY : new float[] {130f, 170f}) {
            controller.onMouseDragAndDropEvent(new MouseDragAndropEvent(110f, cursorY, 0f, 10f, EnumMouseButtons.MOUSE_BUTTON_1, EnumDragPhase.DRAG));
        }
        assertEquals(before, area.getScrollY(), 1e-6f, "перетаскивание по контенту прокрутку не двигает");
    }

    @Test
    public void wheelOverScrollbarStillRoutesToTheArea() {
        TestScene scene = new TestScene();
        ScrollArea area = fullOverflowArea(false);
        area.enableScrollBar(EnumOrientation.VERTICAL, true);
        scene.add(area);
        DefaultSceneController controller = controllerFor(scene);

        // Курсор прямо на полосе: колесо поднимается к ближайшему предку-таргету — самой области.
        controller.onMouseScrollEvent(new MouseScrollEvent(194f, 150f, 0f, -1f));

        assertEquals(40f, area.getScrollY(), 1e-6f, "шаг колеса через полосу равен шагу напрямую (scrollStep)");
        assertMirrorConsistency(area, true, false);
    }

}
