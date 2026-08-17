package com.xamlo.core.engine.graphics.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.components.ICamera;
import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.components.gui.ScrollArea;
import com.xamlo.core.engine.graphics.components.gui.TextField;
import com.xamlo.core.engine.graphics.components.gui.Widget;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.engine.api.devices.EnumKeyboardButtons;
import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.device.events.CharacterInputEvent;
import com.xamlo.engine.device.events.KeyboardClickEvent;
import com.xamlo.engine.device.events.KeyboardHoldEvent;
import com.xamlo.engine.device.events.MouseClickEvent;
import com.xamlo.engine.device.events.MouseButtonPressEvent;
import com.xamlo.engine.device.events.MouseButtonReleaseEvent;
import com.xamlo.engine.device.events.MouseScrollEvent;
import com.xamlo.engine.device.events.MouseHoldEvent;
import com.xamlo.engine.device.events.MouseHoverEvent;

public class DefaultSceneControllerFocusTest {

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

        /** Рекурсивный hit-test: сначала дети (как в реальной сцене), потом сам элемент. */
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

    /** Камера, считающая вызовы move/rotate. */
    private static class CountingCamera implements ICamera {
        final AtomicInteger moves = new AtomicInteger();
        final AtomicInteger rotations = new AtomicInteger();

        @Override
        public Matrix4f getViewMatrix() {
            return new Matrix4f();
        }

        @Override
        public void rotate(Vector3f vector) {
            rotations.incrementAndGet();
        }

        @Override
        public void move(Vector3f vector) {
            moves.incrementAndGet();
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

    private DefaultSceneController newController(TestScene scene, CountingCamera camera) {
        DefaultSceneController controller = new DefaultSceneController();
        controller.setScene(scene);
        controller.setCamera(camera);
        return controller;
    }

    private TextField newField() {
        TextField field = new TextField();
        field.resize(new UIElementGeometry(10, 10, 200, 32));
        return field;
    }

    @Test
    public void clickOnFieldGivesItFocus() {
        TestScene scene = new TestScene();
        TextField field = newField();
        scene.add(field);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        controller.onMouseClienEvent(new MouseClickEvent(50, 20, EnumMouseButtons.MOUSE_BUTTON_1));

        assertSame(field, controller.getFocusedElement());
        assertTrue(field.isFocused());
    }

    @Test
    public void clickOnEmptySpaceClearsFocus() {
        TestScene scene = new TestScene();
        TextField field = newField();
        scene.add(field);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        controller.onMouseClienEvent(new MouseClickEvent(50, 20, EnumMouseButtons.MOUSE_BUTTON_1));
        assertTrue(field.isFocused());

        controller.onMouseClienEvent(new MouseClickEvent(500, 500, EnumMouseButtons.MOUSE_BUTTON_1));

        assertNull(controller.getFocusedElement());
        assertFalse(field.isFocused());
    }

    @Test
    public void clickOnChildFocusesNearestFocusableAncestor() {
        TestScene scene = new TestScene();
        TextField field = newField();
        Widget child = new Widget(field);
        child.resize(new UIElementGeometry(5, 5, 20, 20));
        scene.add(field);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        // Клик по координатам, где лежит только child (15,15)
        controller.onMouseClienEvent(new MouseClickEvent(15, 15, EnumMouseButtons.MOUSE_BUTTON_1));

        assertSame(field, controller.getFocusedElement(), "фокус получает ближайший фокусируемый предок");
        assertTrue(field.isFocused());
    }

    @Test
    public void typedCharacterGoesToFocusedField() {
        TestScene scene = new TestScene();
        TextField field = newField();
        scene.add(field);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        controller.onMouseClienEvent(new MouseClickEvent(50, 20, EnumMouseButtons.MOUSE_BUTTON_1));
        controller.onCharacterInputEvent(new CharacterInputEvent('H'));
        assertEquals("H", field.getText());
    }

    @Test
    public void typedCharacterIgnoredWithoutFocus() {
        TestScene scene = new TestScene();
        TextField field = newField();
        scene.add(field);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        controller.onCharacterInputEvent(new CharacterInputEvent('H'));
        assertEquals("", field.getText());
    }

    @Test
    public void keyboardEventGoesToFocusedField() {
        TestScene scene = new TestScene();
        TextField field = newField();
        scene.add(field);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        controller.onMouseClienEvent(new MouseClickEvent(50, 20, EnumMouseButtons.MOUSE_BUTTON_1));
        controller.onCharacterInputEvent(new CharacterInputEvent('a'));
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_BACKSPACE));
        assertEquals("", field.getText());
    }

    @Test
    public void escapeClearsFocus() {
        TestScene scene = new TestScene();
        TextField field = newField();
        scene.add(field);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        controller.onMouseClienEvent(new MouseClickEvent(50, 20, EnumMouseButtons.MOUSE_BUTTON_1));
        assertTrue(field.isFocused());

        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_ESCAPE));

        assertNull(controller.getFocusedElement());
        assertFalse(field.isFocused());
    }

    @Test
    public void holdEventTicksFocusedElementAndDoesNotMoveCamera() {
        TestScene scene = new TestScene();
        TextField field = newField();
        scene.add(field);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        controller.onMouseClienEvent(new MouseClickEvent(50, 20, EnumMouseButtons.MOUSE_BUTTON_1));
        controller.onKeyboardHoldEvent(new KeyboardHoldEvent(Set.of(EnumKeyboardButtons.KEY_W)));

        assertEquals(0, camera.moves.get(), "ввод текста не двигает камеру");
    }

    @Test
    public void holdEventMovesCameraWithoutFocus() {
        TestScene scene = new TestScene();
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        controller.onKeyboardHoldEvent(new KeyboardHoldEvent(Set.of(EnumKeyboardButtons.KEY_W)));

        assertEquals(1, camera.moves.get());
    }

    @Test
    public void focusMovesBetweenFieldsOnSwitchClick() {
        TestScene scene = new TestScene();
        TextField first = newField();
        TextField second = new TextField();
        second.resize(new UIElementGeometry(10, 60, 200, 32));
        scene.add(first);
        scene.add(second);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        controller.onMouseClienEvent(new MouseClickEvent(50, 20, EnumMouseButtons.MOUSE_BUTTON_1));
        assertTrue(first.isFocused());
        assertFalse(second.isFocused());

        controller.onMouseClienEvent(new MouseClickEvent(50, 70, EnumMouseButtons.MOUSE_BUTTON_1));
        assertFalse(first.isFocused(), "старое поле теряет фокус");
        assertTrue(second.isFocused());
        assertSame(second, controller.getFocusedElement());
    }

    // --- выделение перетаскиванием -----------------------------------------

    @Test
    public void pressOnFieldStartsSelectionAndFocuses() {
        TestScene scene = new TestScene();
        TextField field = newField();
        scene.add(field);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        controller.onMouseButtonPressEvent(new MouseButtonPressEvent(50, 20, EnumMouseButtons.MOUSE_BUTTON_1));

        assertSame(field, controller.getFocusedElement(), "поле получает фокус при старте выделения");
        assertTrue(field.isFocused());
    }

    @Test
    public void dragWhileHeldExtendsSelectionUntilRelease() {
        TestScene scene = new TestScene();
        TextField field = new TextField("Hello");
        field.resize(new UIElementGeometry(10, 10, 200, 32));
        scene.add(field);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        // ЛКМ зажата (hold), затем press на поле
        controller.onMouseHoldEvent(new MouseHoldEvent(List.of(EnumMouseButtons.MOUSE_BUTTON_1)));
        controller.onMouseButtonPressEvent(new MouseButtonPressEvent(20, 20, EnumMouseButtons.MOUSE_BUTTON_1));
        assertTrue(field.isFocused());

        // Движение курсора при зажатой ЛКМ → drag растягивает выделение
        controller.onMouseHoverEvent(new MouseHoverEvent(60, 20, 40, 0));
        assertTrue(field.getSelectionStart() >= 0, "drag создал выделение");

        // Отпускание завершает жест
        controller.onMouseButtonReleaseEvent(new MouseButtonReleaseEvent(60, 20, EnumMouseButtons.MOUSE_BUTTON_1));
        controller.onMouseHoldEvent(new MouseHoldEvent(List.of()));

        // Дальнейшее движение без зажатой кнопки не должно менять выделение
        int startAfterRelease = field.getSelectionStart();
        int endAfterRelease = field.getSelectionEnd();
        controller.onMouseHoverEvent(new MouseHoverEvent(150, 20, 90, 0));
        assertEquals(startAfterRelease, field.getSelectionStart());
        assertEquals(endAfterRelease, field.getSelectionEnd());
    }

    // --- Прокрутка колесом (ScrollArea) -------------------------------------

    @Test
    public void wheelOverContentRoutesToNearestScrollAncestor() {
        TestScene scene = new TestScene();
        ScrollArea area = new ScrollArea();
        area.resize(new UIElementGeometry(100, 100, 200, 100));
        area.setContentSize(400, 300); // maxY = 200
        area.setScroll(0f, 200f);      // строка с локальным y=250 видна на abs y≈150
        Widget row = new Widget(area.getContent());
        row.resize(new UIElementGeometry(5, 250, 80, 20));
        scene.add(area);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        // Курсор над видимой частью строки: событие уходит ближайшему предку-таргету.
        controller.onMouseScrollEvent(new MouseScrollEvent(120f, 160f, 0f, 1f));

        assertEquals(160f, area.getScrollY(), 1e-6f, "колесо вверх уменьшило прокрутку");
    }

    @Test
    public void nestedWheelTargetsRouteToNearestOnly() {
        TestScene scene = new TestScene();
        ScrollArea outer = new ScrollArea();
        outer.resize(new UIElementGeometry(0, 0, 300, 200));
        outer.setContentSize(600, 400);
        ScrollArea inner = new ScrollArea(outer.getContent());
        inner.resize(new UIElementGeometry(20, 20, 100, 80));
        inner.setContentSize(400, 300); // maxY = 220
        inner.setScroll(0f, 100f);
        scene.add(outer);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        // Точка внутри внутреннего (и внешнего) контейнера: двигается только ближайший.
        controller.onMouseScrollEvent(new MouseScrollEvent(60f, 50f, 0f, -1f));

        assertEquals(140f, inner.getScrollY(), 1e-6f, "колесо вниз увеличивало прокрутку");
        assertEquals(0f, outer.getScrollY(), 1e-6f, "внешний таргет не затронут");
    }

    @Test
    public void wheelOverEmptySpaceDoesNothing() {
        TestScene scene = new TestScene();
        ScrollArea area = new ScrollArea();
        area.resize(new UIElementGeometry(500, 500, 200, 100));
        area.setContentSize(400, 300);
        area.setScroll(0f, 120f);
        scene.add(area);
        CountingCamera camera = new CountingCamera();
        DefaultSceneController controller = newController(scene, camera);

        controller.onMouseScrollEvent(new MouseScrollEvent(10f, 10f, 0f, 1f));

        assertEquals(120f, area.getScrollY(), 1e-6f, "свободное пространство без таргета");
    }

    // --- Клип hit-test: вышедшее за край содержимое недоступно ---------------

    private ScrollArea clippedAreaWithRow(int contentHeight, int rowLocalY) {
        ScrollArea area = new ScrollArea();
        area.resize(new UIElementGeometry(100, 100, 200, 100));
        area.setContentSize(200, contentHeight);
        Widget row = new Widget(area.getContent());
        row.resize(new UIElementGeometry(10, rowLocalY, 60, 20));
        return area;
    }

    @Test
    public void scrolledOutContentIsNotAHitTarget() {
        TestScene scene = new TestScene();
        ScrollArea area = clippedAreaWithRow(400, 350); // при scroll=0 строка далеко за нижним краем
        scene.add(area);

        // Точка внутри самой строки (abs y≈450), но за границей клипающей области.
        assertNull(scene.findElementAt(140f, 460f), "вышедшая за край строка не ловит курсор");
    }

    @Test
    public void scrolledInContentBecomesHitTarget() {
        TestScene scene = new TestScene();
        ScrollArea area = clippedAreaWithRow(400, 280);
        scene.add(area);
        area.setScroll(0f, 200f); // abs строки ≈ [180..200] — внутри видимой полосы

        IUIElement hit = scene.findElementAt(140f, 190f);
        assertSame(area.getContent().getChildElements().get(0), hit, "прокрученная в кадр строка доступна");
    }

}
