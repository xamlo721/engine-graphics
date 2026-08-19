package com.xamlo.core.engine.graphics.components;

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

import com.xamlo.engine.device.events.EnumDragPhase;
import com.xamlo.engine.device.events.MouseDragAndropEvent;
import com.xamlo.core.engine.graphics.api.components.ICamera;
import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.core.engine.graphics.api.gui.DragPayload;
import com.xamlo.core.engine.graphics.api.gui.IDragListener;
import com.xamlo.core.engine.graphics.api.gui.IDraggable;
import com.xamlo.core.engine.graphics.api.gui.IDropTarget;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.components.gui.PushButton;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.components.gui.Widget;
import com.xamlo.engine.api.devices.EnumMouseButtons;

/**
 * Типизированный payload жеста DnD и ghost-снимок (E4): доставка значения цели,
 * отклонение неподходящего таргета, опциональный снимок за курсором и сохранность
 * legacy-поведения без флага.
 */
public class DefaultSceneControllerDragTest {

    /** Сцена из плоского списка элементов, hit-test по containsPoint c исключением для DnD. */
    private static class TestScene implements IScene {
        private final List<IUIElement> elements = new ArrayList<>();

        boolean containsEl(IUIElement candidate) {
            return this.elements.contains(candidate);
        }

        int count() {
            return this.elements.size();
        }

        void add(IUIElement element) {
            this.elements.add(element);
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
            for (int i = this.elements.size() - 1; i >= 0; i--) {
                if (this.elements.get(i).containsPoint(xCoord, yCoord)) {
                    return this.elements.get(i);
                }
            }
            return null;
        }

        @Override
        public IUIElement findElementAt(float xCoord, float yCoord, Object excluded) {
            for (int i = this.elements.size() - 1; i >= 0; i--) {
                IUIElement el = this.elements.get(i);
                if (el == excluded || el.equals(excluded)) {
                    continue;
                }
                if (el.containsPoint(xCoord, yCoord)) {
                    return el;
                }
            }
            return null;
        }

        @Override
        public List<IUIElement> getGuiElements() {
            return this.elements;
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
        final List<Vector3f> moves = new ArrayList<>();

        @Override
        public Matrix4f getViewMatrix() {
            return new Matrix4f();
        }

        @Override
        public void rotate(Vector3f vector) {
        }

        @Override
        public void move(Vector3f vector) {
            this.moves.add(vector);
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

    private DefaultSceneController newController(TestScene testScene) {
        DefaultSceneController controller = new DefaultSceneController();
        controller.setScene(testScene);
        controller.setCamera(new StubCamera());
        return controller;
    }

    private MouseDragAndropEvent drag(EnumDragPhase phase, float x, float y, float dx, float dy) {
        return new MouseDragAndropEvent(x, y, dx, dy, EnumMouseButtons.MOUSE_BUTTON_1, phase);
    }

    /** Таргет принимает payload только с ожидаемым значением. */
    private static final class FilteringTarget extends Widget implements IDraggable {
        private final String expectedToken;
        boolean dropActiveObserved = false;

        FilteringTarget(String token) {
            this.expectedToken = token;
        }

        @Override
        public boolean acceptsDrag(DragPayload payload) {
            if (payload == null || payload.getValue() == null) {
                return false;
            }
            return this.expectedToken.equals(payload.getValue().toString());
        }

        @Override
        public void setDropActive(boolean active) {
            super.setDropActive(active);
            if (active) {
                this.dropActiveObserved = true;
            }
        }
    }

    /** Источник со своим sentinel-снимком вместо стандартного клона. */
    private static final class SentinelSource extends PushButton {
        final IUIElement ghostMarker;

        SentinelSource(IUIElement marker) {
            super("drag source");
            this.ghostMarker = marker;
        }

        @Override
        public IUIElement createDragGhost() {
            return this.ghostMarker;
        }
    }

    // --- payload: доставка и отклонение -----------------------------------------

    @Test
    public void payloadIsDeliveredToAcceptingTargetAndRejectedElsewhere() {
        TestScene scene = new TestScene();
        PushButton source = new PushButton("src");
        source.resize(new UIElementGeometry(10, 10, 60, 30));
        FilteringTarget good = new FilteringTarget("token-A");
        good.resize(new UIElementGeometry(200, 10, 80, 40));
        FilteringTarget bad = new FilteringTarget("other-token");
        bad.resize(new UIElementGeometry(300, 10, 80, 40));
        DragPayload token = DragPayload.of("token-A");
        List<String> calls = new ArrayList<>();
        IDraggable api = source;
        api.setDragListener(new IDragListener() {
            @Override public void onDragStart(IDraggable d, float x, float y) { calls.add("start"); }
            @Override public void onDrag(IDraggable d, float x, float y, float dx, float dy) { calls.add("drag"); }
            @Override public void onDragEnterTarget(IDraggable d, IDropTarget t) { calls.add("enter:" + ((FilteringTarget) t).dropActiveObserved); }
            @Override public void onDragLeaveTarget(IDraggable d, IDropTarget t) { calls.add("leave"); }
            @Override public void onDrop(IDraggable d, IDropTarget t, float x, float y) {
                
                calls.add(d.getDragPayload() == token ? "drop:same-ref" : "drop:other");
            }
            @Override public void onDragEnd(IDraggable d, float x, float y) { calls.add("end"); }
        });
        scene.add(source);
        scene.add(good);
        scene.add(bad);
        DefaultSceneController controller = newController(scene);

        // Жест 1: зашли на подходящий таргет, ушли к отклоняющему — дропа нет.
        api.setDragPayload(token);
        assertFalse(bad.acceptsDrag(token), "таргет с чужим токеном не принимает payload");
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.START, 40f, 25f, 0f, 0f));
        assertTrue(calls.contains("start"), "жест стартовал: " + calls);
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.DRAG, 240f, 30f, 1f, 1f));
        assertTrue(good.dropActiveObserved && good.isDropActive(), "принимающий таргет подсвечен во время жеста");
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.DRAG, 340f, 30f, 1f, 1f));
        assertTrue(calls.contains("leave"), "покинули подходящий таргет при уходе к неподходящему: " + calls);
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.END, 340f, 30f, 1f, 1f));
        assertTrue(calls.stream().anyMatch(s -> s.equals("end")), "неподходящий таргет завершает жест сбросом: " + calls);
        assertFalse(calls.stream().anyMatch(s -> s.startsWith("drop:")), "дропа на отклонённом таргете нет: " + calls);

        // Жест 2: завершение над принимающим — onDrop получает ту же ссылку payload'а.
        calls.clear();
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.START, 40f, 25f, 0f, 0f));
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.DRAG, 240f, 30f, 1f, 1f));
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.END, 240f, 30f, 1f, 1f));
        assertTrue(calls.contains("drop:same-ref"), "onDrop получил исходный объект payload'а: " + calls);
        assertSame(token, api.getDragPayload());
    }

    @Test
    public void ghostSnapshotAttachedOnlyWhenOptedIn() {
        TestScene scene = new TestScene();
        Widget marker = new Widget();
        SentinelSource source = new SentinelSource(marker);
        source.resize(new UIElementGeometry(10, 10, 60, 30));
        List<String> calls = new ArrayList<>();
        IDraggable api = source;
        api.setDragListener(new IDragListener() {
            @Override public void onDragStart(IDraggable d, float x, float y) { calls.add("start"); }
            @Override public void onDrag(IDraggable d, float x, float y, float dx, float dy) { calls.add("drag"); }
            @Override public void onDragEnterTarget(IDraggable d, IDropTarget t) {}
            @Override public void onDragLeaveTarget(IDraggable d, IDropTarget t) {}
            @Override public void onDrop(IDraggable d, IDropTarget t, float x, float y) {}
            @Override public void onDragEnd(IDraggable d, float x, float y) { calls.add("end"); }
        });
        scene.add(source);
        DefaultSceneController controller = newController(scene);

        // Без флага: legacy-жест работает, снимка нет.
        assertFalse(api.isGhostPreviewEnabled(), "по умолчанию ghost выключен");
        int before = scene.count();
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.START, 40f, 25f, 0f, 0f));
        assertEquals(before, scene.count(), "без опции снимок не создаётся");
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.END, 60f, 30f, 1f, 1f));
        assertTrue(calls.containsAll(java.util.List.of("start", "end")), "legacy-колбэки без флага сохраняются: " + calls);

        // С флагом: sentinel присоединяется на START в точке захвата и следует за курсором.
        api.setGhostPreviewEnabled(true);
        calls.clear();
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.START, 40f, 25f, 0f, 0f));
        assertTrue(scene.containsEl(marker), "снимок в корневом слое сцены во время жеста");
        float startX = marker.getAbsX();
        float startY = marker.getAbsY();
        assertEquals(10f, startX, 1e-3f, "позиция снимка = курсор минус оффсет захвата (x)");
        assertEquals(10f, startY, 1e-3f, "позиция снимка = курсор минус оффсет захвата (y)");
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.DRAG, 70f, 60f, 5f, 5f));
        assertEquals(startX + 30f, marker.getAbsX(), 1e-3f, "снимок сместился вместе с курсором по X");
        assertEquals(startY + 35f, marker.getAbsY(), 1e-3f, "снимок сместился вместе с курсором по Y");
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.END, 70f, 60f, 5f, 5f));
        assertFalse(scene.containsEl(marker), "снимок снят после END");
    }

    @Test
    public void payloadSurvivesGestureEndForLazyReadback() {
        TestScene scene = new TestScene();
        PushButton source = new PushButton("src2");
        source.resize(new UIElementGeometry(10, 10, 60, 30));
        IDraggable api = source;
        assertNull(api.getDragPayload(), "payload не задан");
        DragPayload token = DragPayload.of("lazy");
        api.setDragListener(new IDragListener() {
            @Override public void onDragStart(IDraggable d, float x, float y) {}
            @Override public void onDrag(IDraggable d, float x, float y, float dx, float dy) {}
            @Override public void onDragEnterTarget(IDraggable d, IDropTarget t) {}
            @Override public void onDragLeaveTarget(IDraggable d, IDropTarget t) {}
            @Override public void onDrop(IDraggable d, IDropTarget t, float x, float y) {}
            @Override public void onDragEnd(IDraggable d, float x, float y) {}
        });
        api.setDragPayload(token);
        scene.add(source);
        DefaultSceneController controller = newController(scene);

        // Payload читается лениво после завершения жеста и очищается явным null.
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.START, 40f, 25f, 0f, 0f));
        controller.onMouseDragAndDropEvent(drag(EnumDragPhase.END, 60f, 30f, 1f, 1f));
        assertSame(token, api.getDragPayload(), "payload живёт на элементе после END");
        api.setDragPayload(null);
        assertNull(api.getDragPayload(), "явная очистка payload'а работает");
    }
}
