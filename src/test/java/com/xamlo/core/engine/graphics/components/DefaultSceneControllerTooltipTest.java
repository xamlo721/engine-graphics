package com.xamlo.core.engine.graphics.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.components.ICamera;
import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.core.engine.graphics.api.gui.IResizable;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.components.gui.Label;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.components.gui.Widget;
import com.xamlo.engine.device.events.MouseHoldEvent;
import com.xamlo.engine.device.events.MouseHoverEvent;

/**
 * Всплывающие подсказки: таймер на каждом кадре (MouseHoldEvent), показ рядом с курсором,
 * пере-задержка при смене цели, обход к ближайшему предку с текстом, краевые клампы.
 */
public class DefaultSceneControllerTooltipTest {

    /** Сцена из плоского списка элементов, hit-test по containsPoint (обратный порядок). */
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

    /** Камера-заглушка. */
    private static class StubCamera implements ICamera {
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

    /** Контроллер с управляемым временем: таймер подсказки детерминирован. */
    private static class TooltipController extends DefaultSceneController {
        private long fakeNanos;

        @Override
        protected long nowNanos() {
            return fakeNanos;
        }

        void advanceMillis(long millis) {
            fakeNanos += millis * 1_000_000L;
        }
    }

    private Widget newWidget(TestScene scene, int x, int y, int w, int h, String hint) {
        Widget widget = new Widget();
        widget.resize(new UIElementGeometry(x, y, w, h));
        if (hint != null) {
            widget.setToolTipText(hint);
        }
        scene.add(widget);
        return widget;
    }

    private TooltipController newController(TestScene scene, long delayMs) {
        TooltipController controller = new TooltipController();
        controller.tooltipDelayMillis = delayMs;
        controller.setScene(scene);
        controller.setCamera(new StubCamera());
        return controller;
    }

    private void hover(TooltipController controller, float x, float y) {
        controller.onMouseHoverEvent(new MouseHoverEvent(x, y, 0f, 0f));
    }

    /** Кадр без движения мыши: ровно то, что DeviceController шлёт каждый тик. */
    private void frameTick(TooltipController controller) {
        controller.onMouseHoldEvent(new MouseHoldEvent(List.of()));
    }

    @Test
    public void elementWithoutHintShowsNoTooltip() {
        TestScene scene = new TestScene();
        newWidget(scene, 10, 10, 80, 32, null); // подсказка не задана
        TooltipController controller = newController(scene, 500L);

        hover(controller, 40f, 26f);
        controller.advanceMillis(5_000L);
        for (int i = 0; i < 3; i++) {
            frameTick(controller);
        }

        assertNull(controller.getToolTipBoxForTests(), "без текста ящик вообще не создаётся");
    }

    @Test
    public void hintedElementShowsTooltipAfterDelayWithoutMovement() {
        TestScene scene = new TestScene();
        newWidget(scene, 350, 250, 60, 20, "Open world");
        TooltipController controller = newController(scene, 500L);

        hover(controller, 380f, 260f);
        frameTick(controller); // тот же кадр: таймер только пошёл
        assertFalse(visible(controller), "сразу после наведения подсказки ещё нет");

        controller.advanceMillis(501L);
        frameTick(controller); // дошло без единого движения мыши

        Label box = controller.getToolTipBoxForTests();
        assertTrue(box != null && visible(box), "подсказка появилась по таймеру");
        assertEquals("Open world", box.getText());
        int x = ((IResizable) box).getGeometry().getXCoord();
        int y = ((IResizable) box).getGeometry().getYCoord();
        assertEquals(394, x, "ящик со смещением +14 от курсора");
        assertEquals(280, y, "ящик со смещением +20 от курсора");
    }

    @Test
    public void movingAwayHidesTooltipImmediately() {
        TestScene scene = new TestScene();
        newWidget(scene, 350, 250, 60, 20, "Hint A");
        TooltipController controller = newController(scene, 500L);

        hover(controller, 380f, 260f);
        controller.advanceMillis(600L);
        frameTick(controller);
        assertTrue(visible(controller));

        hover(controller, 900f, 500f); // пусто — без ожидания таймера

        assertFalse(visible(controller), "уход курсора прячет подсказку сразу");
    }

    @Test
    public void switchingTargetsReappliesFullDelay() {
        TestScene scene = new TestScene();
        Widget first = newWidget(scene, 100, 100, 60, 20, "First tip");
        Widget second = newWidget(scene, 700, 400, 60, 20, "Second tip");
        TooltipController controller = newController(scene, 500L);

        hover(controller, 130f, 110f);
        controller.advanceMillis(600L);
        frameTick(controller);
        assertEquals("First tip", boxText(controller));

        // Смена цели: полный пере-таймер (как в MC).
        hover(controller, 730f, 410f);
        controller.advanceMillis(200L);
        for (int i = 0; i < 2; i++) {
            frameTick(controller);
        }
        assertFalse(visible(controller), "после смены цели таймер отсчитывается заново");

        controller.advanceMillis(350L); // всего >500 мс с момента переключения
        frameTick(controller);
        assertTrue(visible(controller));
        assertEquals("Second tip", boxText(controller));
    }

    @Test
    public void deepChildResolvesNearestAncestorHint() {
        TestScene scene = new TestScene();
        Widget parent = newWidget(scene, 10, 10, 100, 80, "Parent tip");
        Widget child = new Widget(parent);
        child.resize(new UIElementGeometry(20, 30, 40, 20)); // у самого подсказки нет
        TooltipController controller = newController(scene, 0L);

        hover(controller, 40f, 40f);
        frameTick(controller);
        frameTick(controller);

        assertEquals("Parent tip", boxText(controller), "предок-владелец подсказки найден обходом вверх");
    }

    @Test
    public void ownHintBeatsAncestorHint() {
        TestScene scene = new TestScene();
        Widget parent = newWidget(scene, 10, 10, 100, 80, "Parent tip");
        Widget child = new Widget(parent);
        child.resize(new UIElementGeometry(20, 30, 40, 20));
        child.setToolTipText("Child tip");
        TooltipController controller = newController(scene, 0L);

        hover(controller, 40f, 40f);
        frameTick(controller);
        frameTick(controller);

        assertEquals("Child tip", boxText(controller), "своя подсказка перекрывает подсказку предка");
    }

    @Test
    public void tooltipBoxDoesNotStealHitsFromElementsBelow() {
        TestScene scene = new TestScene();
        newWidget(scene, 350, 250, 60, 20, "Hover me");
        TooltipController controller = newController(scene, 0L);

        hover(controller, 380f, 260f);
        frameTick(controller);
        frameTick(controller);
        assertTrue(visible(controller));

        // Пиксель внутри видимого ящика (ящик у курсора +14/+20): hit-test его не видит.
        assertNull(scene.findElementAt(410f, 300f), "подсказка прозрачна для кликов");
    }

    @Test
    public void rightEdgeFlipsLeftAndBottomEdgeMovesAboveCursor() {
        TestScene scene = new TestScene();
        Widget nearRight = newWidget(scene, 1900, 100, 12, 12, "R");
        Widget nearBottom = newWidget(scene, 500, 1070, 12, 12, "B");
        TooltipController controller = newController(scene, 0L);

        hover(controller, 1905f, 106f);
        for (int i = 0; i < 2; i++) {
            frameTick(controller);
        }
        Label box = controller.getToolTipBoxForTests();
        int x = ((IResizable) box).getGeometry().getXCoord();
        int w = ((IResizable) box).getGeometry().getWidth();
        assertTrue(visible(controller));
        assertTrue(x < 1905 && x + w <= 1920 - 8, "у правого края ящик перевёрнут влево: x=" + x);

        // Смена цели у нижнего края: полный пере-таймер при delay=0 закрывается на следующем кадре.
        hover(controller, 506f, 1076f);
        for (int i = 0; i < 3; i++) {
            frameTick(controller);
        }
        int y = ((IResizable) box).getGeometry().getYCoord();
        assertEquals(1038, y, "у нижнего края ящик поднят над курсором");
    }

    private boolean visible(TooltipController controller) {
        return visible(controller.getToolTipBoxForTests());
    }

    private boolean visible(Label box) {
        return box != null && box.isVisible();
    }

    private String boxText(TooltipController controller) {
        Label box = controller.getToolTipBoxForTests();
        return box == null ? "" : box.getText();
    }
}
