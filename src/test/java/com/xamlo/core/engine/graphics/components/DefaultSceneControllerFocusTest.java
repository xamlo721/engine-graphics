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
import com.xamlo.core.engine.graphics.components.gui.TextField;
import com.xamlo.core.engine.graphics.components.gui.Widget;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.engine.api.devices.EnumKeyboardButtons;
import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.device.events.CharacterInputEvent;
import com.xamlo.engine.device.events.KeyboardClickEvent;
import com.xamlo.engine.device.events.KeyboardHoldEvent;
import com.xamlo.engine.device.events.MouseClickEvent;

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

}
