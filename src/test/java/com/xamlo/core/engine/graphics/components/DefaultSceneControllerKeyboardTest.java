package com.xamlo.core.engine.graphics.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
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
import com.xamlo.core.engine.graphics.components.gui.CheckBox;
import com.xamlo.core.engine.graphics.components.gui.ComboBox;
import com.xamlo.core.engine.graphics.components.gui.Label;
import com.xamlo.core.engine.graphics.components.gui.PushButton;
import com.xamlo.core.engine.graphics.components.gui.RadioButton;
import com.xamlo.core.engine.graphics.components.gui.TextField;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.components.gui.Widget;
import com.xamlo.engine.api.devices.EnumKeyboardButtons;
import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.api.devices.IKeyboard;
import com.xamlo.engine.device.events.CharacterInputEvent;
import com.xamlo.engine.device.events.KeyboardClickEvent;
import com.xamlo.engine.device.events.MouseClickEvent;

/**
 * Клавиатурная навигация и активация (E1): Tab/Shift+Tab циклично ходят по
 * фокусируемым элементам в z-порядке с обходом на концах, Enter/NumpadEnter/Space
 * активируют элемент в фокусе ровно один раз за нажатие, ESC снимает фокус.
 */
public class DefaultSceneControllerKeyboardTest {

    /** Сцена из плоского списка элементов, hit-test по containsPoint. */
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
                if (elements.get(i).containsPoint(xCoord, yCoord)) {
                    return elements.get(i);
                }
            }
            return null;
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

    /** Клавиатура-заглушка: нажимаемый набор клавиш задаётся явно (для Shift+Tab). */
    private static class ConfigurableKeyboard implements IKeyboard {
        final Set<EnumKeyboardButtons> held = new HashSet<>();

        boolean hold(EnumKeyboardButtons key) {
            return held.add(key);
        }

        @Override
        public boolean isKeyPushed(EnumKeyboardButtons key) {
            return false;
        }

        @Override
        public boolean isKeyReleased(EnumKeyboardButtons key) {
            return false;
        }

        @Override
        public boolean isKeyHold(EnumKeyboardButtons key) {
            return held.contains(key);
        }

        @Override
        public Set<EnumKeyboardButtons> getPushedKeys() {
            return Set.of();
        }

        @Override
        public Set<EnumKeyboardButtons> getKeysHolding() {
            return held;
        }

        @Override
        public Set<EnumKeyboardButtons> getReleasedKeys() {
            return Set.of();
        }

        @Override
        public List<Character> getCharsTyped() {
            return List.of();
        }
    }

    private DefaultSceneController newController(TestScene scene, IKeyboard keyboard) {
        DefaultSceneController controller = new DefaultSceneController();
        controller.setScene(scene);
        controller.setCamera(new StubCamera());
        if (keyboard != null) {
            controller.setKeyboard(keyboard);
        }
        return controller;
    }

    /** Кнопка с геометрией и счётчиком кликов. */
    private PushButton newCountingButton(int x, int y, AtomicInteger clicks) {
        PushButton button = new PushButton("B");
        button.resize(new UIElementGeometry(x, y, 80, 32));
        button.setClickListener(element -> clicks.incrementAndGet());
        return button;
    }

    private void pressKey(DefaultSceneController controller, EnumKeyboardButtons key) {
        controller.onKeyboardClienEvent(new KeyboardClickEvent(key));
    }

    // --- Tab / Shift+Tab ------------------------------------------------------

    @Test
    public void tabCyclesInEffectiveZOrderAndWrapsAround() {
        TestScene scene = new TestScene();
        // Порядок регистрации не совпадает с z-порядком: цикл идёт по эффективному z.
        PushButton high = new PushButton("high");
        high.resize(new UIElementGeometry(10, 10, 80, 32));
        high.setZIndex(5);
        PushButton low = new PushButton("low");
        low.resize(new UIElementGeometry(10, 60, 80, 32));
        low.setZIndex(1);
        PushButton mid = new PushButton("mid");
        mid.resize(new UIElementGeometry(10, 110, 80, 32));
        mid.setZIndex(3);
        scene.add(high);
        scene.add(low);
        scene.add(mid);
        DefaultSceneController controller = newController(scene, null);

        pressKey(controller, EnumKeyboardButtons.KEY_TAB);
        assertSame(low, controller.getFocusedElement(), "первый в цикле — минимальный z");

        pressKey(controller, EnumKeyboardButtons.KEY_TAB);
        assertSame(mid, controller.getFocusedElement());

        pressKey(controller, EnumKeyboardButtons.KEY_TAB);
        assertSame(high, controller.getFocusedElement(), "максимальный z замыкает цикл");

        pressKey(controller, EnumKeyboardButtons.KEY_TAB);
        assertSame(low, controller.getFocusedElement(), "обход заворачивается к началу");
    }

    @Test
    public void shiftTabGoesBackwardsAndStartsFromLast() {
        TestScene scene = new TestScene();
        PushButton high = new PushButton("h");
        high.resize(new UIElementGeometry(10, 10, 80, 32));
        high.setZIndex(5);
        PushButton low = new PushButton("l");
        low.resize(new UIElementGeometry(10, 60, 80, 32));
        low.setZIndex(1);
        scene.add(high);
        scene.add(low);
        ConfigurableKeyboard keyboard = new ConfigurableKeyboard();
        DefaultSceneController controller = newController(scene, keyboard);

        // Без начального фокуса Shift+Tab стартует с последнего элемента.
        keyboard.hold(EnumKeyboardButtons.KEY_LEFT_SHIFT);
        pressKey(controller, EnumKeyboardButtons.KEY_TAB);
        assertSame(high, controller.getFocusedElement(), "Shift+Tab без фокуса берёт последний элемент");

        // Дальше против z-порядка: high → low и обход назад к high.
        pressKey(controller, EnumKeyboardButtons.KEY_TAB);
        assertSame(low, controller.getFocusedElement());
        pressKey(controller, EnumKeyboardButtons.KEY_TAB);
        assertSame(high, controller.getFocusedElement(), "обход против часовой заворачивается на начало");
    }

    @Test
    public void tabSkipsNonFocusableHiddenAndDisabledElements() {
        TestScene scene = new TestScene();
        PushButton disabled = newCountingButton(10, 10, new AtomicInteger());
        disabled.setEnabled(false);
        PushButton hidden = newCountingButton(10, 60, new AtomicInteger());
        hidden.setVisible(false);
        Widget plain = new Widget();
        plain.resize(new UIElementGeometry(10, 110, 80, 32)); // по умолчанию не фокусируемый
        Label label = new Label("L");
        label.setFocusable(true);
        label.resize(new UIElementGeometry(10, 160, 80, 32));
        PushButton enabled = newCountingButton(10, 210, new AtomicInteger());
        scene.add(disabled);
        scene.add(hidden);
        scene.add(plain);
        scene.add(label);
        scene.add(enabled);
        DefaultSceneController controller = newController(scene, null);

        List<IUIElement> visited = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            pressKey(controller, EnumKeyboardButtons.KEY_TAB);
            visited.add(controller.getFocusedElement());
        }
        assertEquals(List.of(label, enabled, label, enabled), visited,
                "в цикле только видимые включённые фокусируемые элементы, с обходом на концах");
    }

    @Test
    public void tabTraversesNestedChildrenNotInRootRegistry() {
        TestScene scene = new TestScene();
        Widget container = new Widget();
        container.setZIndex(7); // контейнер поднимает потомков своим z-индексом
        container.resize(new UIElementGeometry(50, 50, 200, 100));
        PushButton inner = newCountingButton(10, 10, new AtomicInteger());
        inner.setParent(container);
        container.addChild(inner);
        PushButton outer = newCountingButton(300, 300, new AtomicInteger());
        outer.setZIndex(2);
        // В реестре сцены — только корневой контейнер и внешняя кнопка.
        scene.add(container);
        scene.add(outer);
        DefaultSceneController controller = newController(scene, null);

        pressKey(controller, EnumKeyboardButtons.KEY_TAB);
        assertSame(outer, controller.getFocusedElement(), "сначала элемент с меньшим эффективным z");
        pressKey(controller, EnumKeyboardButtons.KEY_TAB);
        assertSame(inner, controller.getFocusedElement(), "вложенный ребёнок найден обходом дерева");
    }

    @Test
    public void clickOnEmptySpaceStillClearsFocus() {
        TestScene scene = new TestScene();
        PushButton button = newCountingButton(10, 10, new AtomicInteger());
        scene.add(button);
        DefaultSceneController controller = newController(scene, null);

        controller.onMouseClienEvent(new MouseClickEvent(50, 26, EnumMouseButtons.MOUSE_BUTTON_1));
        assertTrue(button.isFocused(), "клик ставит фокус на интерактивную кнопку");

        controller.onMouseClienEvent(new MouseClickEvent(900, 700, EnumMouseButtons.MOUSE_BUTTON_1));
        assertNull(controller.getFocusedElement());
        assertFalse(button.isFocused());
    }

    // --- Активация Enter / NumpadEnter / Space ---------------------------------

    /** Нажатие клавиши активации и проверка, что счётчик кликов вырос ровно на единицу. */
    private static int pressAndMeasure(DefaultSceneController controller, AtomicInteger clicks, EnumKeyboardButtons key) {
        int before = clicks.get();
        controller.onKeyboardClienEvent(new KeyboardClickEvent(key));
        return clicks.get() - before;
    }

    @Test
    public void enterSpaceNumpadEnterEachFireListenerExactlyOncePerPress() {
        TestScene scene = new TestScene();
        AtomicInteger clicks = new AtomicInteger();
        PushButton button = newCountingButton(10, 10, clicks);
        scene.add(button);
        DefaultSceneController controller = newController(scene, null);
        controller.setFocusedElement(button);

        assertEquals(1, pressAndMeasure(controller, clicks, EnumKeyboardButtons.KEY_ENTER), "одно нажатие Enter — один клик");
        assertEquals(1, pressAndMeasure(controller, clicks, EnumKeyboardButtons.KEY_SPACE), "Space активирует так же");
        assertEquals(1, pressAndMeasure(controller, clicks, EnumKeyboardButtons.KEY_KP_ENTER), "NumpadEnter тоже работает");
        assertEquals(3, clicks.get(), "всего три активации за три разных клавиши");
    }

    @Test
    public void repeatedSameActivationKeyFiresOncePerEventNotTwice() {
        TestScene scene = new TestScene();
        AtomicInteger clicks = new AtomicInteger();
        PushButton button = newCountingButton(10, 10, clicks);
        scene.add(button);
        DefaultSceneController controller = newController(scene, null);
        controller.setFocusedElement(button);

        for (int i = 0; i < 5; i++) {
            pressKey(controller, EnumKeyboardButtons.KEY_ENTER);
        }
        assertEquals(5, clicks.get(), "каждое событие клика клавиатуры даёт ровно одно срабатывание");
    }

    @Test
    public void activationWithoutFocusDoesNothing() {
        TestScene scene = new TestScene();
        AtomicInteger clicks = new AtomicInteger();
        PushButton button = newCountingButton(10, 10, clicks);
        scene.add(button);
        DefaultSceneController controller = newController(scene, null);

        pressKey(controller, EnumKeyboardButtons.KEY_ENTER);
        pressKey(controller, EnumKeyboardButtons.KEY_SPACE);
        assertEquals(0, clicks.get());
        assertNull(controller.getFocusedElement());
    }

    @Test
    public void escapeClearsFocusAndStopsActivation() {
        TestScene scene = new TestScene();
        AtomicInteger clicks = new AtomicInteger();
        PushButton button = newCountingButton(10, 10, clicks);
        scene.add(button);
        DefaultSceneController controller = newController(scene, null);
        controller.setFocusedElement(button);

        pressKey(controller, EnumKeyboardButtons.KEY_ESCAPE);
        assertNull(controller.getFocusedElement(), "ESC снимает фокус как и раньше");
        assertFalse(button.isFocused());

        pressKey(controller, EnumKeyboardButtons.KEY_ENTER);
        assertEquals(0, clicks.get(), "после снятия фокуса Enter ничего не активирует");
    }

    @Test
    public void disabledWidgetIgnoresKeyboardActivationEvenWhenFocused() {
        TestScene scene = new TestScene();
        AtomicInteger clicks = new AtomicInteger();
        PushButton button = newCountingButton(10, 10, clicks);
        button.setEnabled(false);
        scene.add(button);
        DefaultSceneController controller = newController(scene, null);
        controller.setFocusedElement(button); // программный фокус на выключенном элементе

        pressKey(controller, EnumKeyboardButtons.KEY_ENTER);
        pressKey(controller, EnumKeyboardButtons.KEY_SPACE);
        assertEquals(0, clicks.get(), "выключенная кнопка активацией не реагирует");
    }

    /** Обычный (не Label) виджет без activate-реализации: клавиши активации его не ломают. */
    @Test
    public void activationKeysAreHarmlessForPlainFocusableWidgets() {
        TestScene scene = new TestScene();
        Widget plain = new Widget();
        plain.setFocusable(true);
        plain.resize(new UIElementGeometry(10, 10, 80, 32));
        scene.add(plain);
        DefaultSceneController controller = newController(scene, null);
        controller.setFocusedElement(plain);

        pressKey(controller, EnumKeyboardButtons.KEY_SPACE);
        pressKey(controller, EnumKeyboardButtons.KEY_KP_ENTER);

        assertSame(plain, controller.getFocusedElement());
        assertTrue(plain.isFocused(), "фокус сохранён, исключений нет");
    }

    // --- Активация конкретных интерактивных элементов --------------------------

    @Test
    public void checkboxTogglesViaEnterAndFiresUserListenerOncePerPress() {
        TestScene scene = new TestScene();
        CheckBox box = new CheckBox("Option");
        box.resize(new UIElementGeometry(10, 10, 160, 40));
        AtomicInteger userClicks = new AtomicInteger();
        box.setClickListener(element -> userClicks.incrementAndGet());
        scene.add(box);
        DefaultSceneController controller = newController(scene, null);
        controller.setFocusedElement(box);

        assertFalse(box.isChecked());
        pressKey(controller, EnumKeyboardButtons.KEY_ENTER);
        assertTrue(box.isChecked(), "Enter переключает чекбокс");
        assertEquals(1, userClicks.get(), "внешний слушатель сработал один раз");

        pressKey(controller, EnumKeyboardButtons.KEY_SPACE);
        assertFalse(box.isChecked(), "Space тоже переключает обратно");
        assertEquals(2, userClicks.get());
    }

    @Test
    public void radioButtonSelectsViaEnterAndUnchecksGroupMate() {
        TestScene scene = new TestScene();
        String group = "keyboard-test-group";
        RadioButton first = new RadioButton("First", group);
        first.resize(new UIElementGeometry(10, 10, 160, 32));
        RadioButton second = new RadioButton("Second", group);
        second.resize(new UIElementGeometry(10, 52, 160, 32));
        AtomicInteger secondUserClicks = new AtomicInteger();
        second.setClickListener(element -> secondUserClicks.incrementAndGet());
        scene.add(first);
        scene.add(second);
        DefaultSceneController controller = newController(scene, null);
        controller.setFocusedElement(second);

        pressKey(controller, EnumKeyboardButtons.KEY_ENTER);

        assertTrue(second.isChecked(), "фокусированный элемент выбран активацией");
        assertFalse(first.isChecked(), "остальные в группе сняты отметкой");
        assertEquals(1, secondUserClicks.get());

        // Клик по первому через контроллер возвращает выбор ему — группа согласована.
        // Клик в прямоугольник первого (y∈[10..42]); y=68 ложился бы во второй ряд.
        controller.onMouseClienEvent(new MouseClickEvent(40, 26, EnumMouseButtons.MOUSE_BUTTON_1));
        assertTrue(first.isChecked());
        assertFalse(second.isChecked());
    }

    @Test
    public void comboboxTogglesOpenCloseViaEnter() {
        TestScene scene = new TestScene();
        ComboBox box = new ComboBox("Pick one");
        box.addItem("alpha");
        box.addItem("beta");
        box.resize(new UIElementGeometry(10, 10, 200, 36));
        scene.add(box);
        DefaultSceneController controller = newController(scene, null);
        controller.setFocusedElement(box);

        assertFalse(box.isOpen());
        pressKey(controller, EnumKeyboardButtons.KEY_ENTER);
        assertTrue(box.isOpen(), "Enter раскрывает панель вариантов");

        pressKey(controller, EnumKeyboardButtons.KEY_SPACE);
        assertFalse(box.isOpen(), "Space закрывает открытую панель");

        pressKey(controller, EnumKeyboardButtons.KEY_KP_ENTER);
        assertTrue(box.isOpen(), "повторная активация снова открывает");
    }

    // --- Ввод символов не пострадал -------------------------------------------

    @Test
    public void characterInputStillGoesToFocusedFieldAfterRoutingChanges() {
        TextField field = new TextField();
        field.resize(new UIElementGeometry(10, 10, 200, 32));
        TestScene scene = new TestScene();
        scene.add(field);
        DefaultSceneController controller = newController(scene, null);
        controller.onMouseClienEvent(new MouseClickEvent(50, 26, EnumMouseButtons.MOUSE_BUTTON_1));

        controller.onCharacterInputEvent(new CharacterInputEvent('a'));
        assertEquals("a", field.getText(), "символьный ввод доходит до сфокусированного поля");
    }

}
