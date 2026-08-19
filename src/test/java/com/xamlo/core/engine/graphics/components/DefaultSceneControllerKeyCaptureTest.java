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
import java.util.concurrent.atomic.AtomicReference;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.components.ICamera;
import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.core.engine.graphics.api.gui.IKeyboardHandler;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.components.gui.PushButton;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.components.gui.Widget;
import com.xamlo.engine.api.devices.EnumKeyboardButtons;
import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.device.actions.ActionBinding;
import com.xamlo.engine.device.actions.ActionRegistry;
import com.xamlo.engine.device.events.CharacterInputEvent;
import com.xamlo.engine.device.events.KeyboardClickEvent;
import com.xamlo.engine.device.events.KeyboardHoldEvent;
import com.xamlo.engine.device.events.KeyboardReleaseEvent;
import com.xamlo.engine.device.events.MouseButtonPressEvent;
import com.xamlo.engine.device.events.MouseButtonReleaseEvent;
import com.xamlo.engine.device.events.MouseHoldEvent;

/**
 * Модальный захват следующей клавиши (E6) + сквозная интеграция с реестром действий (E7):
 * коммит точного кода, отмена ESC'ем в UNBOUND, подмена цели вторым arm, гашение UNKNOWN,
 * дебаунс ~20 мс по детерминированному часам и матрица приоритетов маршрутизации фокуса.
 */
public class DefaultSceneControllerKeyCaptureTest {

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

    /** Камера-заглушка с журналом движений. */
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
            moves.add(vector);
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

    /** Детерминированные часы: дебаунс-окно проверяется без реального времени. */
    private static class ClockController extends DefaultSceneController {
        long now = 1_000_000L; // достаточно далеко от нуля, чтобы стартовый штамп не подавлял ничего

        @Override
        protected long nowNanos() {
            return now;
        }
    }

    /** Фокусируемое поле, записывающее каждое доставленное нажатие (матрица приоритетов). */
    private static class RecordingField extends Widget implements IKeyboardHandler {
        final List<EnumKeyboardButtons> pressedKeys = new ArrayList<>();

        RecordingField(int x, int y) {
            resize(new UIElementGeometry(x, y, 200, 32));
            setFocusable(true);
        }

        @Override
        public void onKeyPressed(EnumKeyboardButtons key, boolean ctrl, boolean shift) {
            pressedKeys.add(key);
        }

        @Override
        public void onCharTyped(char c) {
        }
    }

    private PushButton countingButton(TestScene scene, AtomicInteger clicks) {
        PushButton button = new PushButton("B");
        button.resize(new UIElementGeometry(10, 10, 80, 32));
        button.setClickListener(element -> clicks.incrementAndGet());
        scene.add(button);
        return button;
    }

    // --- коммит и отмена ---------------------------------------------------------

    @Test
    public void armThenPressCommitsExactCodeAndDisarms() {
        TestScene scene = new TestScene();
        ClockController controller = new ClockController();
        controller.setScene(scene);
        AtomicReference<Integer> committed = new AtomicReference<>(-999);

        assertFalse(controller.isKeyCaptureArmed(), "по умолчанию никто не вооружён");
        controller.armKeyCapture(committed::set);
        assertTrue(controller.isKeyCaptureArmed());
        assertEquals(-999, (int) committed.get(), "до нажатия ничего не закоммичено");

        int expected = ActionBinding.keyCodeFor(EnumKeyboardButtons.KEY_F5);
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_F5));

        assertEquals(expected, (int) committed.get(), "коммитит ровно код нажатой клавиши");
        assertFalse(controller.isKeyCaptureArmed(), "после коммита захват завершён");
        assertNull(controller.getKeyCaptureTarget());
    }

    @Test
    public void escapeWhileArmedCommitsUnboundAndSupersedesFocusClearing() {
        TestScene scene = new TestScene();
        RecordingField field = new RecordingField(10, 60);
        scene.add(field);
        ClockController controller = new ClockController();
        controller.setScene(scene);
        controller.setFocusedElement(field);
        AtomicReference<Integer> committed = new AtomicReference<>();

        // Пока цель НЕ вооружена — ESC снимает фокус как и исторически.
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_ESCAPE));
        assertNull(controller.getFocusedElement(), "ESC без захвата снимает фокус по-старому");

        controller.setFocusedElement(field);
        controller.armKeyCapture(committed::set);

        // Вооружённый захват перехватывает ESC: фокус не трогаем, коммитим UNBOUND_CODE.
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_ESCAPE));
        assertEquals(ActionBinding.UNBOUND_CODE, (int) committed.get(), "отмена через ESC -> код 0");
        assertSame(field, controller.getFocusedElement(), "пока захват активен, ESC НЕ снимает UI-фокус");
        assertFalse(controller.isKeyCaptureArmed());
    }

    @Test
    public void unknownKeysAreSwallowedWithoutDisarming() {
        TestScene scene = new TestScene();
        ClockController controller = new ClockController();
        controller.setScene(scene);
        AtomicReference<Integer> committed = new AtomicReference<>();
        controller.armKeyCapture(committed::set);

        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_UNKNOWN));
        assertTrue(controller.isKeyCaptureArmed(), "непредставимый код гасится без развооружения");
        assertNull(committed.get(), "ничего не закоммичено");

        int expected = ActionBinding.keyCodeFor(EnumKeyboardButtons.KEY_F6);
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_F6));
        assertEquals(expected, (int) committed.get(), "следующая настоящая клавиша коммитит свой код");
        assertFalse(controller.isKeyCaptureArmed());
    }

    @Test
    public void secondArmReplacesFirstWithoutCommittingToOldTarget() {
        TestScene scene = new TestScene();
        ClockController controller = new ClockController();
        controller.setScene(scene);
        AtomicReference<Integer> first = new AtomicReference<>();
        AtomicReference<Integer> second = new AtomicReference<>();

        controller.armKeyCapture(first::set);
        controller.armKeyCapture(second::set); // подмена до любого нажатия
        assertSame(second.get(), null);

        int expected = ActionBinding.keyCodeFor(EnumKeyboardButtons.KEY_A);
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_A));
        assertNull(first.get(), "прежняя цель ничего не получила");
        assertEquals(expected, (int) second.get(), "нажалась новая цель");
        assertFalse(controller.isKeyCaptureArmed());
    }

    @Test
    public void explicitCancelCommitsNothingAndDebounceWindowAppliesThenExpires() {
        TestScene scene = new TestScene();
        PushButton low = countingButton(scene, new AtomicInteger());
        PushButton high = new PushButton("H");
        high.resize(new UIElementGeometry(10, 60, 80, 32));
        high.setZIndex(5);
        scene.add(high);
        RecordingField field = new RecordingField(10, 110);
        scene.add(field);
        ClockController controller = new ClockController();
        controller.setScene(scene);
        AtomicReference<Integer> committed = new AtomicReference<>();

        long t0 = controller.now;
        controller.armKeyCapture(committed::set);
        controller.cancelKeyCapture(); // явная отмена без коммита
        assertNull(committed.get(), "cancel ничему не закоммитил");
        assertFalse(controller.isKeyCaptureArmed());

        // В дебаунс-окне клавиатура сцены глушится: ни Tab, ни доставка фокусу.
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_TAB));
        assertNull(controller.getFocusedElement(), "Tab внутри окна подавлен");
        assertTrue(field.pressedKeys.isEmpty(), "фокусированный элемент ничего не получил в окне");

        controller.now = t0 + DefaultSceneController.KEY_CAPTURE_SUPPRESSION_NANOS - 1L;
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_A));
        assertTrue(field.pressedKeys.isEmpty(), "за наносекунду до края окна всё ещё подавлено");

        // Граница включительно: окно истекло — маршрутизация вернулась.
        controller.now = t0 + DefaultSceneController.KEY_CAPTURE_SUPPRESSION_NANOS;
        controller.setFocusedElement(field);
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_B));
        assertEquals(List.of(EnumKeyboardButtons.KEY_B), field.pressedKeys, "после окна клавиши снова доставляются");
    }

    @Test
    public void committingPressDoesNotTriggerGlobalShortcutsInTheSamePass() {
        TestScene scene = new TestScene();
        AtomicInteger clicks = new AtomicInteger();
        PushButton button = countingButton(scene, clicks);
        ClockController controller = new ClockController();
        controller.setScene(scene);
        AtomicReference<Integer> committed = new AtomicReference<>();

        // Контроль: без захвата Enter активирует сфокусированную кнопку.
        controller.setFocusedElement(button);
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_ENTER));
        assertEquals(1, (int) clicks.get());

        controller.armKeyCapture(committed::set);
        int expected = ActionBinding.keyCodeFor(EnumKeyboardButtons.KEY_ENTER);
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_ENTER));

        assertEquals(expected, (int) committed.get(), "Enter закоммичен как привязка...");
        assertEquals(1, (int) clicks.get(), "...и при этом НЕ активировал фокус в том же проходе");
    }

    @Test
    public void debounceWindowAlsoSuppressesHoldDrivenCameraMovement() {
        TestScene scene = new TestScene();
        StubCamera camera = new StubCamera();
        ClockController controller = new ClockController();
        controller.setScene(scene);
        controller.setCamera(camera);
        AtomicReference<Integer> committed = new AtomicReference<>();

        long t0 = controller.now;
        Set<EnumKeyboardButtons> emptyHolds = Set.of();
        controller.onKeyboardHoldEvent(new KeyboardHoldEvent(emptyHolds)); // контрольный кадр до захвата
        assertTrue(camera.moves.isEmpty());

        controller.armKeyCapture(committed::set);
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_F9)); // коммит
        assertFalse(controller.isKeyCaptureArmed());

        Set<EnumKeyboardButtons> holdingW = Set.of(EnumKeyboardButtons.KEY_W);
        controller.onKeyboardHoldEvent(new KeyboardHoldEvent(holdingW));
        assertTrue(camera.moves.isEmpty(), "hold-управление камеры подавлено внутри окна после коммита");

        controller.now = t0 + DefaultSceneController.KEY_CAPTURE_SUPPRESSION_NANOS;
        controller.onKeyboardHoldEvent(new KeyboardHoldEvent(holdingW));
        assertEquals(1, camera.moves.size(), "после истечения окна hold снова двигает камеру");
    }

    /** Поле с записью набранных символов (для проверки прохода CharacterInputEvent). */
    private static class CharRecordingField extends Widget implements IKeyboardHandler {
        final List<Character> chars = new ArrayList<>();

        CharRecordingField(int x, int y) {
            resize(new UIElementGeometry(x, y, 200, 32));
            setFocusable(true);
        }

        @Override
        public void onKeyPressed(EnumKeyboardButtons key, boolean ctrl, boolean shift) {
        }

        @Override
        public void onCharTyped(char c) {
            chars.add(c);
        }
    }

    @Test
    public void characterInputPassesThroughEvenWhileArmedAndSuppressed() {
        TestScene scene = new TestScene();
        ClockController controller = new ClockController();
        controller.setScene(scene);
        AtomicReference<Integer> committed = new AtomicReference<>();

        CharRecordingField texty = new CharRecordingField(10, 60);
        scene.add(texty);
        controller.setFocusedElement(texty);
        controller.armKeyCapture(committed::set);

        controller.onCharacterInputEvent(new CharacterInputEvent('a'));
        assertEquals(List.of(Character.valueOf('a')), texty.chars, "символьный ввод не является шорткатом и проходит всегда");

        long t0 = controller.now;
        controller.cancelKeyCapture();
        controller.onCharacterInputEvent(new CharacterInputEvent('b'));
        assertTrue(texty.chars.contains('b'), "проходит и в дебаунс-окне после отмены");
    }

    // --- матрица приоритетов маршрутизации ----------------------------------------

    /** Матрица: (а) ничто не вооружено -> фокус получает клавиши как раньше. */
    @Test
    public void precedenceIdleFocusStillReceivesKeysWhenNothingArmed() {
        TestScene scene = new TestScene();
        RecordingField field = new RecordingField(10, 10);
        scene.add(field);
        ClockController controller = new ClockController();
        controller.setScene(scene);
        controller.setFocusedElement(field);

        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_C));
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_D));
        assertEquals(List.of(EnumKeyboardButtons.KEY_C, EnumKeyboardButtons.KEY_D), field.pressedKeys,
                "историческая доставка элементу в фокусе сохранена, когда захват не активен");
    }

    /** Матрица: (б) цель вооружена -> сфокусированный элемент событие НЕ видит. */
    @Test
    public void precedenceArmedTargetConsumsKeyFromFocusedElement() {
        TestScene scene = new TestScene();
        RecordingField field = new RecordingField(10, 10);
        scene.add(field);
        ClockController controller = new ClockController();
        controller.setScene(scene);
        controller.setFocusedElement(field);
        AtomicReference<Integer> committed = new AtomicReference<>();
        controller.armKeyCapture(committed::set);

        int expected = ActionBinding.keyCodeFor(EnumKeyboardButtons.KEY_E);
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_E));
        assertTrue(field.pressedKeys.isEmpty(), "во время захвата клавиша до элемента в фокусе не доходит");
        assertEquals(expected, (int) committed.get());
    }

    /** Матрица: (в) дебаунс-окно после завершения -> фокус глушится; (г) после окна — снова получает. */
    @Test
    public void precedenceSuppressionWindowThenResumeDeliversAgain() {
        TestScene scene = new TestScene();
        RecordingField field = new RecordingField(10, 10);
        scene.add(field);
        ClockController controller = new ClockController();
        controller.setScene(scene);
        controller.setFocusedElement(field);
        AtomicReference<Integer> committed = new AtomicReference<>();
        long t0 = controller.now;

        controller.armKeyCapture(committed::set);
        controller.cancelKeyCapture(); // окно подавления открыто без коммита

        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_F));
        assertTrue(field.pressedKeys.isEmpty(), "(в) внутри окна элемент в фокусе ничего не видит");

        controller.now = t0 + DefaultSceneController.KEY_CAPTURE_SUPPRESSION_NANOS + 5L;
        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_G));
        assertEquals(List.of(EnumKeyboardButtons.KEY_G), field.pressedKeys, "(г) после истечения окна доставка возобновлена");
    }

    // --- интеграция с реестром действий (E7) через тот же поток событий -------------

    @Test
    public void actionRegistryRidesTheSameKeyEventStream() {
        TestScene scene = new TestScene();
        StubCamera camera = new StubCamera();
        ClockController controller = new ClockController();
        controller.setScene(scene);
        controller.setCamera(camera);

        ActionRegistry registry = new ActionRegistry();
        int walkCode = ActionBinding.keyCodeFor(EnumKeyboardButtons.KEY_W);
        ActionBinding walk = new ActionBinding(registry, "walk", "Ходьба", walkCode);
        ActionBinding pick = new ActionBinding(registry, "pick", "Взять", ActionBinding.keyCodeForMouseButton(0));
        controller.setActionRegistry(registry);

        assertTrue(controller.isKeyCodeTaken(walkCode), "конфликт-запрос делегирует в подключённый реестр");
        assertFalse(controller.isKeyCodeTaken(ActionBinding.UNBOUND_CODE));

        Set<EnumKeyboardButtons> emptyHolds = Set.of();
        controller.onKeyboardHoldEvent(new KeyboardHoldEvent(emptyHolds));
        assertFalse(walk.getIsKeyPressed());

        controller.onKeyboardClienEvent(new KeyboardClickEvent(EnumKeyboardButtons.KEY_W));
        assertTrue(walk.isPressed(), "edge нажатия дошёл через обычный поток событий контроллера");
        assertFalse(walk.isPressed(), "и ровно один раз");
        assertTrue(walk.getIsKeyPressed());

        controller.onKeyboardHoldEvent(new KeyboardHoldEvent(Set.of(EnumKeyboardButtons.KEY_W)));
        assertTrue(walk.getIsKeyPressed(), "снимок удержания актуализировал поллинг");

        controller.onKeyboardReleaseEvent(new KeyboardReleaseEvent(EnumKeyboardButtons.KEY_W));
        assertFalse(walk.getIsKeyPressed(), "отпускание сбросило состояние");
    }

    @Test
    public void mouseBoundActionsAreFedFromMouseEventsAndArmUnpressesRegistry() {
        TestScene scene = new TestScene();
        ClockController controller = new ClockController();
        controller.setScene(scene);

        ActionRegistry registry = new ActionRegistry();
        int leftCode = ActionBinding.keyCodeForMouseButton(0); // -100
        ActionBinding pick = new ActionBinding(registry, "pick", "Взять предмет", leftCode);
        controller.setActionRegistry(registry);

        controller.onMouseButtonPressEvent(new MouseButtonPressEvent(5f, 6f, EnumMouseButtons.MOUSE_BUTTON_1));
        assertTrue(pick.isPressed(), "edge мышиной кнопки прошёл через тот же контроллер");
        assertTrue(pick.getIsKeyPressed());

        List<EnumMouseButtons> holdingLeft = List.of(EnumMouseButtons.MOUSE_BUTTON_1);
        controller.onMouseHoldEvent(new MouseHoldEvent(holdingLeft));
        assertTrue(pick.getIsKeyPressed(), "снимок удержания мыши подтверждает состояние");

        AtomicReference<Integer> committed = new AtomicReference<>();
        controller.armKeyCapture(committed::set);
        assertFalse(pick.getIsKeyPressed(), "вооружение захвата вызвало unPressAllKeys в реестре");
        assertFalse(pick.isPressed());

        controller.onMouseButtonReleaseEvent(new MouseButtonReleaseEvent(5f, 6f, EnumMouseButtons.MOUSE_BUTTON_1));
        assertFalse(pick.getIsKeyPressed());
    }

    @Test
    public void isKeyCodeTakenWithoutAttachedRegistryIsAlwaysFalse() {
        ClockController controller = new ClockController();
        TestScene scene = new TestScene();
        controller.setScene(scene);
        assertFalse(controller.isKeyCodeTaken(ActionBinding.keyCodeFor(EnumKeyboardButtons.KEY_F2)));
        assertFalse(controller.isKeyCodeTaken(-100), "без подключённого реестра конфликтов быть не может");
    }

}
