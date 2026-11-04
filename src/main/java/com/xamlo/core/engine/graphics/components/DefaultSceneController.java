package com.xamlo.core.engine.graphics.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import org.joml.Vector3f;

import com.xamlo.core.engine.graphics.api.components.ICamera;
import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.components.ISceneController;
import com.xamlo.core.engine.graphics.api.gui.IActivatable;
import com.xamlo.core.engine.graphics.api.gui.DragPayload;
import com.xamlo.core.engine.graphics.api.gui.IDragListener;
import com.xamlo.core.engine.graphics.api.gui.IDraggable;
import com.xamlo.core.engine.graphics.api.gui.IClickable;
import com.xamlo.core.engine.graphics.api.gui.IDropTarget;
import com.xamlo.core.engine.graphics.api.gui.IFrameTickable;
import com.xamlo.core.engine.graphics.api.gui.IFocusable;
import com.xamlo.core.engine.graphics.api.gui.IHoverable;
import com.xamlo.core.engine.graphics.api.gui.IKeyboardHandler;
import com.xamlo.core.engine.graphics.api.gui.IPointer;
import com.xamlo.core.engine.graphics.api.gui.IPointerListener;
import com.xamlo.core.engine.graphics.api.gui.ITextSelectionHandler;
import com.xamlo.core.engine.graphics.api.gui.ITooltipSupport;
import com.xamlo.core.engine.graphics.api.gui.IVisible;
import com.xamlo.core.engine.graphics.api.gui.IWheelTarget;
import com.xamlo.core.engine.graphics.api.gui.TextLineSpec;
import com.xamlo.core.engine.graphics.api.gui.ZOrder;

import com.xamlo.core.engine.graphics.components.gui.Border;
import com.xamlo.core.engine.graphics.components.gui.Color;
import com.xamlo.core.engine.graphics.components.gui.EnumAlignment;
import com.xamlo.core.engine.graphics.components.gui.Label;
import com.xamlo.core.engine.graphics.components.gui.TextMetrics;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.font.ApplicationFont;
import com.xamlo.core.engine.graphics.font.UnicodeGlyphFont;
import com.xamlo.core.engine.graphics.fontsystem.FontSystem;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;

import com.xamlo.engine.api.devices.EnumKeyboardButtons;
import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.api.devices.IKeyboard;
import com.xamlo.engine.api.devices.IMouse;
import com.xamlo.engine.device.actions.ActionBinding;
import com.xamlo.engine.device.actions.ActionRegistry;
import com.xamlo.engine.device.actions.IKeyCaptureTarget;
import com.xamlo.engine.device.events.CharacterInputEvent;
import com.xamlo.engine.device.events.KeyboardClickEvent;
import com.xamlo.engine.device.events.KeyboardHoldEvent;
import com.xamlo.engine.device.events.KeyboardReleaseEvent;
import com.xamlo.engine.device.events.MouseButtonPressEvent;
import com.xamlo.engine.device.events.MouseButtonReleaseEvent;
import com.xamlo.engine.device.events.MouseClickEvent;
import com.xamlo.engine.device.events.MouseDragAndropEvent;
import com.xamlo.engine.device.events.MouseHoldEvent;
import com.xamlo.engine.device.events.MouseHoverEvent;
import com.xamlo.engine.device.events.MouseScrollEvent;
import com.xamlo.engine.device.events.WindowResizeEvent;
import com.xamlo.core.engine.graphics.components.gui.ClipContexts;
import com.xamlo.core.engine.graphics.viewport.LogicalViewport;
import com.xamlo.core.engine.graphics.viewport.ViewportSettings;

import net.lenni0451.asmevents.event.EventTarget;

public class DefaultSceneController implements ISceneController {

	private ICamera camera;
	/** When false the controller still routes keys/actions but never moves the camera (hosts that own the camera elsewhere). */
	private boolean cameraControlEnabled = true;
	@SuppressWarnings("unused")
	private IMouse mouse;
	@SuppressWarnings("unused")
	private IKeyboard keyboard;
	private IScene scene;
	
	//this flag allow to execute mouse click&hover event like a UI actions
	private boolean inHUDMode;
	
	/** Удерживается ли вторая кнопка мыши — берётся из событий, а не из устройств. */
	private boolean hudMouseButtonHeld;
	
	// Состояние активного жеста drag-and-drop (живёт только на диспетчерском потоке)
	private IDraggable draggingElement;
	private IDropTarget dragTarget;
	
	// «Призрак» перетаскиваемого элемента (E4): визуальный снимок за курсором.
	// Оффсеты — от точки захвата до левого верхнего угла элемента в момент START.
	private IUIElement dragGhost;
	private IScene dragGhostScene;
	
	private float dragGrabOffsetX;
	private float dragGrabOffsetY;
	
	// Элемент, принимающий клавиатурный ввод (устанавливается по клику / ESC снимает)
	private IUIElement focusedElement;
	
	// Реестр ремаппабельных действий (E7): необязательный хук в этот же поток событий.
	private ActionRegistry actionRegistry;
	
	// Состояние модального захвата следующей клавиши (E6): вооружённая цель и штамп
	// дебаунса после коммита/отмены — чтобы само событие не запустило глобальные шорткаты.
	private IKeyCaptureTarget keyCaptureTarget;
	private long keyCaptureSuppressionUntilNanos = 0L;
	protected static final long KEY_CAPTURE_SUPPRESSION_NANOS = 20_000_000L; // ~20 мс
	
	// Жест выделения перетаскиванием (живёт на диспетчерском потоке)
	private IUIElement selectingElement;
	
	// Зажата ли левая кнопка мыши — из MouseHoldEvent (для drag-фазы выделения)
	private boolean leftButtonHeld;
	
	// Состояние всплывающей подсказки (живёт на диспетчерском потоке).
	private Label tooltipBox;
	private IScene tooltipBoxScene;
	private IUIElement tooltipOwner;
	private long hoverStartNanos = -1L;
	private float lastCursorX;
	private float lastCursorY;
	private boolean hasLastCursor = false;
	protected long tooltipDelayMillis = 600L;

	// --- Логическое пространство GUI и resize окна (E9) -------------------------

	/** Заданная приложением логическая сетка UI; null — legacy-режим с историческими константами. */
	private volatile LogicalViewport logicalViewport = null;

	// Последний известный физический размер окна из WindowResizeEvent; до первого события —
	// legacy-значения, совпадающие со стартовым состоянием рендер-проходов.
	private volatile int lastPhysicalWidth = ClipContexts.WINDOW_WIDTH;
	private volatile int lastPhysicalHeight = ClipContexts.WINDOW_HEIGHT;

	private static final float movAmt = 0.0011f;

	public DefaultSceneController() {
		this.inHUDMode = false;
	}
	
	/**
	 * Распределение нажатия клавиши по приоритетам:
	 * <ol>
	 *  <li>E7-реестр получает edge независимо от всего остального — это сквозной механизм;</li>
	 *  <li>E6-захват в вооружённом состоянии ПОГЛОЩАЕТ событие целиком: ESC коммитит
	 *      UNBOUND_CODE (отмена), любая другая известная клавиша — свой код, UNKNOWN
	 *      гасится без развооружения. Ничьему другому обработчику это событие не видо;
	 *      в частности, ESC здесь НЕ снимает UI-фокус, пока захват активен.</li>
	 *  <li>В дебаунс-окне после только что завершившегося захвата (~20 мс) клавиатурные
	 *      сцены шорткаты (Tab/активация/ESC-clear/доставка фокусу и hold-движение камеры)
	 *      подавлены, чтобы само коммитящее событие их не запустило. Символьный ввод
	 *      (CharacterInputEvent) при этом проходит всегда — он не является шорткатом.</li>
	 *  <li>Иначе — историческое поведение без изменений.</li>
	 * </ol>
	 */
	@Override
	@EventTarget(noParamEvents = KeyboardClickEvent.class)
	public void onKeyboardClienEvent(final KeyboardClickEvent event) {

        final EnumKeyboardButtons key = event.getButton();

        // E7: edge уходит реестру из того же потока событий, без параллельного канала.
        if (actionRegistry != null) {
            actionRegistry.noteKeyPressed(ActionBinding.keyCodeFor(key));
        }

        // E6: вооружённая цель забирает нажатие целиком.
        IKeyCaptureTarget armedTarget = this.keyCaptureTarget;
        if (armedTarget != null) {
            handleArmedKeyPress(armedTarget, key);
            return;
        }

        if (!isInKeyCaptureSuppression(nowNanos())) {
            boolean ctrl  = isModifierHeld(EnumKeyboardButtons.KEY_LEFT_CONTROL, EnumKeyboardButtons.KEY_RIGHT_CONTROL);
            boolean shift = isModifierHeld(EnumKeyboardButtons.KEY_LEFT_SHIFT, EnumKeyboardButtons.KEY_RIGHT_SHIFT);

            // ESC снимает фокус с элемента ввода
            if (key == EnumKeyboardButtons.KEY_ESCAPE) {
                clearFocus();
            } else if (key == EnumKeyboardButtons.KEY_TAB && !ctrl && this.scene != null) {
                // Tab / Shift+Tab циклично ходят по фокусируемым элементам сцены;
                // Ctrl+TAB не перехватывается и уходит обработчику как обычная клавиша.
                cycleTabFocus(shift);
            } else if (!ctrl && isActivationKey(key)) {
                // Enter/NumpadEnter/Space активируют элемент в фокусе (IActivatable).
                activateFocusedIfPossible();
            }

            // Клавиша уходит элементу в фокусе (текстовое поле и т.п.)
            if (focusedElement instanceof IKeyboardHandler handler) {
                handler.onKeyPressed(key, ctrl, shift);
            }
        }
    }

	/**
	 * Нажатие при вооружённом захвате: UNKNOWN гасится без развооружения (такой код нельзя
	 * ни привязать, ни показать), ESC коммитит UNBOUND_CODE как отмену, остальное — свой код.
	 */
	private void handleArmedKeyPress(IKeyCaptureTarget target, EnumKeyboardButtons key) {
		if (key == EnumKeyboardButtons.KEY_UNKNOWN) {
			return;
		}
		int committedCode = (key == EnumKeyboardButtons.KEY_ESCAPE) ? ActionBinding.UNBOUND_CODE : ActionBinding.keyCodeFor(key);
		this.keyCaptureTarget = null;
		stampKeyCaptureSuppression();
		target.onKeyCommitted(committedCode);
	}

	/** Штамп дебаунс-окна после завершения захвата (коммит/отмена явная или по клавише). */
	private void stampKeyCaptureSuppression() {
		keyCaptureSuppressionUntilNanos = nowNanos() + KEY_CAPTURE_SUPPRESSION_NANOS;
	}

	private boolean isInKeyCaptureSuppression(long nowNanosValue) {
		return nowNanosValue < keyCaptureSuppressionUntilNanos;
	}

	/** Клавиши «нажать» для активации сфокусированного элемента. */
	private static boolean isActivationKey(EnumKeyboardButtons key) {
        return key == EnumKeyboardButtons.KEY_ENTER
                || key == EnumKeyboardButtons.KEY_KP_ENTER
                || key == EnumKeyboardButtons.KEY_SPACE;
    }

	/** Элемент может принимать взаимодействие: видим и не выключен (если такие флаги есть). */
	private static boolean interactionAllowed(IUIElement element) {
        if (element instanceof IVisible visible && !visible.isVisible()) {
            return false;
        }
        if (element instanceof IActivatable activatable && !activatable.isEnabled()) {
            return false;
        }
        return true;
    }

	/** Активирует элемент в фокусе, если он поддерживает активацию и доступен. */
	private void activateFocusedIfPossible() {
        IUIElement target = this.focusedElement;
        if (!(target instanceof IActivatable activatable)) {
            return;
        }
        if (!interactionAllowed(target)) {
            return;
        }
        activatable.activate();
    }

	/**
	 * Циклически переставляет клавиатурный фокус по фокусируемым элементам сцены:
	 * Tab — по порядку списка, Shift+Tab — против него, на концах обход заворачивается.
	 */
	private void cycleTabFocus(boolean backwards) {
        List<IUIElement> candidates = collectFocusCycleCandidates();
        int count = candidates.size();
        if (count == 0) {
            return;
        }
        int current = -1;
        for (int i = 0; i < count; i++) {
            if (candidates.get(i) == focusedElement) {
                current = i;
                break;
            }
        }
        // Фокуса ещё нет или текущий элемент выбыл из цикла: Tab стартует с первого,
        // Shift+Tab — с последнего.
        int next = current < 0 ? (backwards ? count - 1 : 0)
                              : ((current + (backwards ? -1 : 1) + count) % count);
        setFocusedElement(candidates.get(next));
    }

	/**
	 * Кандидаты для tab-цикла: все элементы реестра сцены и их потомки (с дедупликацией),
	 * отфильтрованные по «фокусируем + видим + не выключен», отсортированные по эффективному
	 * z-индексу ({@link ZOrder}). Стабильная сортировка сохраняет порядок обхода дерева при
	 * равных индексах — тот же порядок, что у отрисовки элементов со слоями одного уровня.
	 */
	private List<IUIElement> collectFocusCycleCandidates() {
        Set<IUIElement> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<IUIElement> found = new ArrayList<>();
        for (IUIElement root : this.scene.getGuiElements()) {
            visitForFocus(root, seen, found);
        }
        return ZOrder.sorted(found);
    }

	private void visitForFocus(IUIElement element, Set<IUIElement> seen, List<IUIElement> out) {
        if (!seen.add(element)) {
            return;
        }
        if (element instanceof IFocusable focusable && focusable.isFocusable() && interactionAllowed(element)) {
            out.add(element);
        }
        for (IUIElement child : element.getChildElements()) {
            visitForFocus(child, seen, out);
        }
    }

	/**
	 * Набран символ (включая Unicode) — уходит элементу в фокусе.
	 */
	@EventTarget(noParamEvents = CharacterInputEvent.class)
	public void onCharacterInputEvent(final CharacterInputEvent event) {
        if (focusedElement instanceof IKeyboardHandler handler) {
            handler.onCharTyped(event.getCharacter());
        }
    }

	/** Зажата ли какая-то из модификаторов (Ctrl/Shift) прямо сейчас. */
	private boolean isModifierHeld(EnumKeyboardButtons left, EnumKeyboardButtons right) {
        if (keyboard == null) {
            return false;
        }
        return keyboard.isKeyHold(left) || keyboard.isKeyHold(right);
    }
	
	@Override
	@EventTarget(noParamEvents = KeyboardReleaseEvent.class)
	public void onKeyboardReleaseEvent(final KeyboardReleaseEvent event) {
        // Edge-событие «отпущена» — E7-реестр тоже получает его из того же потока.
        if (actionRegistry != null) {
            actionRegistry.noteKeyReleased(ActionBinding.keyCodeFor(event.getButton()));
        }
    }
	
	@Override
	@EventTarget(noParamEvents = KeyboardHoldEvent.class)
	public void onKeyboardHoldEvent(final KeyboardHoldEvent event) {

		// E7: полный снимок зажатых клавиш актуализирует getIsKeyPressed() реестра
		// независимо от UI-фокуса (до раннего return ниже).
		if (actionRegistry != null) {
			Set<Integer> heldCodes = new HashSet<>(event.getHeldKeys().size());
			for (EnumKeyboardButtons key : event.getHeldKeys()) {
				heldCodes.add(ActionBinding.keyCodeFor(key));
			}
			actionRegistry.refreshHeldState(heldCodes);
		}

		// Пока элемент ввода в фокусе, клавиатура «его»: тикаем его (мигание caret)
		// и не двигаем камерой, чтобы набор текста не управлял миром.
		if (focusedElement != null) {
			if (focusedElement instanceof IFrameTickable tickable) {
				tickable.onFrame();
			}
			return;
		}

		// Дебаунс после завершения захвата (E6): hold-управление подавлено вместе с кликами.
		if (isInKeyCaptureSuppression(nowNanos())) {
			return;
		}

		if (!cameraControlEnabled) {
			return;
		}

		for (EnumKeyboardButtons heldKey : event.getHeldKeys()) {
			switch (heldKey) {
				case KEY_W: camera.move(new Vector3f(0.0f, 0.0f, -movAmt)); break;
				case KEY_S: camera.move(new Vector3f(0.0f, 0.0f, movAmt)); break;
				case KEY_A: camera.move(new Vector3f(-movAmt, 0.0f, 0.0f)); break;
				case KEY_D: camera.move(new Vector3f(movAmt, 0.0f, 0.0f)); break;
				case KEY_SPACE: camera.move(new Vector3f(0.0f, movAmt, 0.0f)); break;
				case KEY_LEFT_SHIFT: camera.move(new Vector3f(0.0f, -movAmt, 0.0f)); break;
				case KEY_Q: camera.rotate(new Vector3f(0.0f, movAmt * 40, 0.0f)); break;
				case KEY_E: camera.rotate(new Vector3f(0.0f, -movAmt * 40, 0.0f)); break;
				default: break;
			}
		}
	}

	@Override
	@EventTarget(noParamEvents = MouseClickEvent.class)
	public void onMouseClienEvent(final MouseClickEvent event) {
		// E9: курсор приходит физическими пикселями — один раз переводим в пространство активной сетки.
		float ux = this.toDesignX(event.getxPos());
		float uy = this.toDesignY(event.getyPos());

		switch (event.getButton()) {
			
		case MOUSE_BUTTON_1: {
			
			IUIElement element = this.scene.findElementAt(ux, uy);

			if (element == null) {
				clearFocus();
				break;
			}

			// Клик по фокусируемому элементу (или его потомку) ставит фокус,
			// клик мимо — снимает.
			focusNearestFocusable(element);

			if (element instanceof IClickable clickable) {
				com.xamlo.core.engine.graphics.api.gui.IClickListener click = clickable.getClickListener();
				if (click != null) {
					click.onClicked(clickable);
				}
			}

			if (element instanceof IPointer pointer) {
				IPointerListener listener = pointer.getPointerListener();
				if (listener != null) {
					listener.onPointer(element, ux, uy);
				}
			}
			
			break;
		}
			case MOUSE_BUTTON_2: {
				break;
			}
			case MOUSE_BUTTON_3: {
				break;
			}
			case MOUSE_BUTTON_4: {
				this.inHUDMode = true;
				break;
			}
			case MOUSE_BUTTON_5: {
				this.inHUDMode = false;
				break;
			}
			case MOUSE_BUTTON_6: {
				break;
			}
			case MOUSE_BUTTON_7: {
				break;
			}
			default: {
				throw new IllegalArgumentException("Unexpected value: " + event.getButton());
			}
		}
		noteCursorPosition(ux, uy);
	}

	@Override
	@EventTarget(noParamEvents = MouseButtonPressEvent.class)
	public void onMouseButtonPressEvent(final MouseButtonPressEvent event) {
		// E9: курсор приходит физическими пикселями — один раз переводим в пространство активной сетки.
		float ux = this.toDesignX(event.getxPos());
		float uy = this.toDesignY(event.getyPos());
		noteActionButtonEdge(event.getButton(), true);
		if (event.getButton() != EnumMouseButtons.MOUSE_BUTTON_1) {
			return;
		}
		// Старт выделения: ближайший к курсору элемент, поддерживающий выделение.
		IUIElement element = this.scene.findElementAt(ux, uy);
		IUIElement target = nearestTextSelectionHandler(element);
		this.selectingElement = target;
		if (target != null) {
			// Поле, где начинают выделять, получает и клавиатурный фокус
			// (drag без клика не прогоняет focusNearestFocusable из click-обработчика).
			focusNearestFocusable(target);
			((ITextSelectionHandler) target).onSelectionStart(ux, uy);
		}
		noteCursorPosition(ux, uy);
	}

	@Override
	@EventTarget(noParamEvents = MouseButtonReleaseEvent.class)
	public void onMouseButtonReleaseEvent(final MouseButtonReleaseEvent event) {
		// E9: курсор приходит физическими пикселями — один раз переводим в пространство активной сетки.
		float ux = this.toDesignX(event.getxPos());
		float uy = this.toDesignY(event.getyPos());
		noteActionButtonEdge(event.getButton(), false);
		if (event.getButton() == EnumMouseButtons.MOUSE_BUTTON_1 && selectingElement != null) {
			((ITextSelectionHandler) selectingElement).onSelectionEnd(ux, uy);
			selectingElement = null;
		}
		noteCursorPosition(ux, uy);
	}

	/** Прокрутка колеса: дельта уходит ближайшему предку-таргету под курсором. */
	@Override
	@EventTarget(noParamEvents = MouseScrollEvent.class)
	public void onMouseScrollEvent(final MouseScrollEvent event) {
		// E9: курсор приходит физическими пикселями — один раз переводим в пространство активной сетки.
		float ux = this.toDesignX(event.getxPos());
		float uy = this.toDesignY(event.getyPos());
		IUIElement element = this.scene.findElementAt(ux, uy);
		for (IUIElement candidate = element; candidate != null; candidate = candidate.hasParent() ? candidate.getParent() : null) {
			if (candidate instanceof IWheelTarget target) {
				target.onScrolled(event.getDeltaX(), event.getDeltaY());
				return;
			}
		}
	}
	
	/** Edge кнопки мыши для E7-реестра с отрицательными кодами (-100-index). */
	private void noteActionButtonEdge(EnumMouseButtons button, boolean pressed) {
		if (actionRegistry == null || button.keyID < 0) { // UNKOWN имеет keyID -1
			return;
		}
		int code = ActionBinding.keyCodeForMouseButton(button.keyID);
		if (pressed) {
			actionRegistry.noteKeyPressed(code);
		} else {
			actionRegistry.noteKeyReleased(code);
		}
	}

	@Override
	@EventTarget(noParamEvents = MouseHoldEvent.class)
	public void onMouseHoldEvent(final MouseHoldEvent event) {

		tickTooltip();
		hudMouseButtonHeld = event.getHeldButtons().contains(EnumMouseButtons.MOUSE_BUTTON_2);
		leftButtonHeld = event.getHeldButtons().contains(EnumMouseButtons.MOUSE_BUTTON_1);

		// E7: снимок зажатых кнопок мыши — отдельный домен от клавиатурного.
		if (actionRegistry != null) {
			Set<Integer> heldCodes = new HashSet<>();
			for (EnumMouseButtons btn : event.getHeldButtons()) {
				if (btn.keyID >= 0) {
					heldCodes.add(ActionBinding.keyCodeForMouseButton(btn.keyID));
				}
			}
			actionRegistry.refreshMouseButtonsHeld(heldCodes);
		}
	}
	
	@Override
	@EventTarget(noParamEvents = MouseHoverEvent.class)
	public void onMouseHoverEvent(MouseHoverEvent event) {
		// E9: курсор приходит физическими пикселями — один раз переводим в пространство активной сетки.
		float ux = this.toDesignX(event.getXCoord());
		float uy = this.toDesignY(event.getYCoord());

		// Идёт выделение перетаскиванием — растягиваем его до курсора.
		if (selectingElement != null && leftButtonHeld) {
			((ITextSelectionHandler) selectingElement).onSelectionDrag(ux, uy);
			return;
		}

		if (cameraControlEnabled && hudMouseButtonHeld && this.inHUDMode) {
			camera.rotate(new Vector3f( event.getDy() * movAmt * 10, event.getDx() * movAmt * 10, 0.0f));
			return;
		}
		
		
		syncPointer(ux, uy);
	}

	/** Пересчитывает ховер/подсказку для виджетов, перестроенных под неподвижным курсором. */
	public void refreshPointerState() {
		if (!this.hasLastCursor) return;
		this.syncPointer(this.lastCursorX, this.lastCursorY);
	}

	private void syncPointer(float ux, float uy) {
		for (IUIElement otherElement : scene.getGuiElements()) {
			if (otherElement instanceof IHoverable) {
				((IHoverable)otherElement).setHovered(false);
			}
		}
		IUIElement element = this.scene.findElementAt(ux, uy);
		if (element != null && element instanceof IHoverable) {
			((IHoverable)element).setHovered(true);
		}
		updateFromCursor(ux, uy);
	}

	@Override
	@EventTarget(noParamEvents = MouseDragAndropEvent.class)
	public void onMouseDragAndDropEvent(final MouseDragAndropEvent event) {
		// E9: курсор приходит физическими пикселями — один раз переводим в пространство активной сетки.
		float ux = this.toDesignX(event.getxPos());
		float uy = this.toDesignY(event.getyPos());

		switch (event.getPhase()) {
			
			case START: {
				// Жест стал перетаскиванием — ищем элемент под курсором без исключений
				IUIElement found = this.scene.findElementAt(ux, uy);
				if (found instanceof IDraggable && ((IDraggable)found).getDragListener() != null) {
					this.draggingElement = (IDraggable)found;
					// E4: точка захвата запоминается ДО колбэка — слушатель может сдвинуть источник.
					dragGrabOffsetX = ux - found.getAbsX();
					dragGrabOffsetY = uy - found.getAbsY();
					IDragListener listener = draggingElement.getDragListener();
					listener.onDragStart(draggingElement, ux, uy);
					attachDragGhost(ux, uy);
				} else {
					this.draggingElement = null;
				}
				break;
			}

			case DRAG: {
				if (draggingElement == null) break;

				IDragListener listener = draggingElement.getDragListener();

				// Таргет под курсором: сам перетаскиваемый элемент из хит-теста исключаем,
				// а payload жеста таргет проверяет перед принятием (E4).
				IUIElement underCursor = this.scene.findElementAt(ux, uy, draggingElement);
				IDropTarget newTarget = acceptedDropCandidate(underCursor);
				
				if (newTarget != dragTarget) {
					if (dragTarget != null) {
						dragTarget.setDropActive(false);
						listener.onDragLeaveTarget(draggingElement, dragTarget);
					}
					if (newTarget != null) {
						newTarget.setDropActive(true);
						listener.onDragEnterTarget(draggingElement, newTarget);
					}
					dragTarget = newTarget;
				}
				
				listener.onDrag(draggingElement, ux, uy, event.getDeltaX(), event.getDeltaY());

				// E4: снимок следует за курсором с оффсетом точки захвата.
				repositionDragGhost(ux, uy);
				break;
			}

			case END: {
				if (draggingElement == null) break;

				IDragListener listener = draggingElement.getDragListener();

				if (dragTarget != null) {
					dragTarget.setDropActive(false);
					listener.onDrop(draggingElement, dragTarget, ux, uy);
				} else {
					IUIElement underCursor = this.scene.findElementAt(ux, uy, draggingElement);
					IDropTarget lastSecond = acceptedDropCandidate(underCursor);
					if (lastSecond != null) {
						lastSecond.setDropActive(false);
						listener.onDrop(draggingElement, lastSecond, ux, uy);
					} else {
						listener.onDragEnd(draggingElement, ux, uy);
					}
				}

				this.draggingElement = null;
				this.dragTarget = null;
				detachDragGhost();
				break;
			}
		}
	}

	// --- Типизированный payload и «призрак» перетаскивания (E4) ----------------------

	/** Корневой слой ghost-снимка: выше любых реальных элементов сцены, ниже кольца подсказок. */
	protected static final int DRAG_GHOST_Z_INDEX = Integer.MAX_VALUE / 4;

	/**
	 * Таргет под курсором, принимающий типизированные данные активного жеста (E4):
	 * кандидат должен быть IDropTarget И пройти свою проверку {@link IDropTarget#acceptsDrag}
	 * по payload'у источника. Отклонённый таргет для этого payload'а прозрачен — ни
	 * подсветки, ни enter/drop-доставки нет, жест завершится как сброс вне цели.
	 */
	private IDropTarget acceptedDropCandidate(IUIElement underCursor) {
		if (!(underCursor instanceof IDropTarget target)) {
			return null;
		}
		DragPayload payload = (draggingElement == null) ? null : draggingElement.getDragPayload();
		return target.acceptsDrag(payload) ? target : null;
	}

	/**
	 * Присоединяет визуальный снимок перетаскиваемого элемента к корню сцены (E4).
	 * Снимок создаётся только если элемент явно включил ghost-preview
	 * ({@link IDraggable#isGhostPreviewEnabled()}) и умеет себя клонировать
	 * ({@link IDraggable#createDragGhost()} не null); позиционируется в точке захвата
	 * поверх оригинала, дальше следует за курсором в DRAG-кадрах. Повторный вызов
	 * сначала отсоединяет прежний снимок — в сцене всегда ровно один на активный жест.
	 */
	private void attachDragGhost(float xCoord, float yCoord) {
		detachDragGhost();
		IDraggable source = this.draggingElement;
		if (source == null || !source.isGhostPreviewEnabled()) {
			return;
		}
		IUIElement ghost = source.createDragGhost();
		if (ghost == null || this.scene == null) {
			return;
		}
		this.dragGhostScene = this.scene;
		this.dragGhost = ghost;
		repositionDragGhost(xCoord, yCoord);
		ghost.setZIndex(DRAG_GHOST_Z_INDEX);
		if (ghost instanceof IVisible visible) {
			visible.setVisible(true);
		}
		List<IUIElement> roots = this.scene.getGuiElements();
		if (!roots.contains(ghost)) {
			roots.add(ghost);
		}
	}

	/** Снимок встаёт так, как если бы его схватили в точке старта жеста и вели за курсором. */
	private void repositionDragGhost(float xCoord, float yCoord) {
		if (this.dragGhost == null) {
			return;
		}
		this.dragGhost.setPosition((int)(xCoord - dragGrabOffsetX), (int)(yCoord - dragGrabOffsetY));
	}

	/** Убирает снимок из того списка сцены, куда он был добавлен (сцена могла смениться). */
	private void detachDragGhost() {
		if (this.dragGhost == null) {
			return;
		}
		if (dragGhostScene != null) {
			dragGhostScene.getGuiElements().remove(this.dragGhost);
		}
		this.dragGhost = null;
		this.dragGhostScene = null;
	}

	/** Тестовый хук: текущий ghost-снимок активного жеста (null — жест не идёт/выключен). */
	public IUIElement getDragGhostForTests() {
		return this.dragGhost;
	}

	/**
	 * Ставит фокус на ближайший фокусируемый предок элемента (включая сам элемент).
	 * Если фокус уже на этом элементе — ничего не делает.
	 */
	private void focusNearestFocusable(IUIElement element) {
		IUIElement candidate = element;
		while (candidate != null) {
			if (candidate instanceof IFocusable focusable && focusable.isFocusable()) {
				setFocusedElement(candidate);
				return;
			}
			candidate = candidate.hasParent() ? candidate.getParent() : null;
		}
		// Клик по нефокусируемому элементу фокус не меняет (снимает только пустота/ESC).
	}

	/**
	 * Ближайший предок элемента (включая сам), поддерживающий выделение мышью.
	 * Клик по внутреннему индикатору текстового поля выделяет само поле.
	 */
	private IUIElement nearestTextSelectionHandler(IUIElement element) {
		IUIElement candidate = element;
		while (candidate != null) {
			if (candidate instanceof ITextSelectionHandler) {
				return candidate;
			}
			candidate = candidate.hasParent() ? candidate.getParent() : null;
		}
		return null;
	}

	/** Программно устанавливает фокус (null — снять). */
	public void setFocusedElement(IUIElement element) {
		if (focusedElement == element) {
			return;
		}
		if (focusedElement instanceof IFocusable old) {
			old.setFocused(false);
		}
		focusedElement = element;
		if (focusedElement instanceof IFocusable now) {
			now.setFocused(true);
		}
	}

	/** Снимает фокус с текущего элемента. */
	public void clearFocus() {
		setFocusedElement(null);
	}

	public IUIElement getFocusedElement() {
		return focusedElement;
	}

	// --- Логическое пространство GUI и resize окна (E9) -------------------------

	/**
	 * Опционально задаёт логическую сетку UI приложения (opt-in на E9): пока она задана,
	 * ортоматрицы рендереров и клип-математика работают в её размерах вместо зашитых
	 * 1920×1080, а {@link #onWindowResizeEvent} дополнительно сообщает о каждом изменении
	 * физического размера окна через хук {@link #onViewportChanged}. {@code null} возвращает
	 * legacy-поведение побайтово как до введения вьюпорта. Состояние дублируется в глобальном
	 * {@link ViewportSettings}, которым читают рендеры и ClipContexts.
	 */
	public void setLogicalViewport(LogicalViewport viewport) {
	    if (viewport != null && viewport.equals(this.logicalViewport)) {
	        return;
	    }
	    this.logicalViewport = viewport;
	    ViewportSettings.setActive(viewport);
	    int logicalW = (viewport == null) ? ClipContexts.WINDOW_WIDTH : viewport.getLogicalWidth();
	    int logicalH = (viewport == null) ? ClipContexts.WINDOW_HEIGHT : viewport.getLogicalHeight();
	    onViewportChanged(logicalW, logicalH, lastPhysicalWidth, lastPhysicalHeight);
	}

	/** Активная логическая сетка или null — не задана (legacy-режим). */
	public LogicalViewport getLogicalViewport() {
	    return this.logicalViewport;
	}

	/** Физический X курсора → координата активной сетки; без заданной сетки — identity. */
	public float toDesignX(float physicalX) {
	    LogicalViewport vp = this.logicalViewport;
	    return (vp == null) ? physicalX : vp.toDesignX(physicalX, lastPhysicalWidth);
	}

	/** Физический Y курсора → координата активной сетки; без заданной сетки — identity. */
	public float toDesignY(float physicalY) {
	    LogicalViewport vp = this.logicalViewport;
	    return (vp == null) ? physicalY : vp.toDesignY(physicalY, lastPhysicalHeight);
	}

	/**
	 * Событие изменения размера окна: обновляет последний известный физический размер и при
	 * заданной сетке зовёт хук re-layout. Пока сетка не задана, обработчик лишь запоминает
	 * размеры — legacy-поведение рендера от физического размера не зависит.
	 */
	@EventTarget(noParamEvents = WindowResizeEvent.class)
	public void onWindowResizeEvent(final WindowResizeEvent event) {
	    int w = event.getWidth();
	    int h = event.getHeight();
	    if (w == lastPhysicalWidth && h == lastPhysicalHeight) {
	        return; // повторный вызов с прежним размером — события уже было ровно одно
	    }
	    lastPhysicalWidth = w;
	    lastPhysicalHeight = h;
	    LogicalViewport vp = this.logicalViewport;
	    if (vp != null) {
	        onViewportChanged(vp.getLogicalWidth(), vp.getLogicalHeight(), w, h);
	    }
	}

	/**
	 * Хук перераскладки активного контента: вызывается при установке/сбросе логической сетки
	 * ({@link #setLogicalViewport}) и на каждое изменение физического размера окна в активном
	 * режиме. По умолчанию ничего не делает — сцены переопределяют под свой re-layout.
	 */
	protected void onViewportChanged(int logicalWidth, int logicalHeight, int physicalWidth, int physicalHeight) {}

    // --- Ремаппабельные действия (E7) и модальный захват (E6) ---------------------

	/**
	 * Подключает реестр действий к клавиатурному маршрутированию этого контроллера:
	 * edge'и нажатий/отпусканий, снимки удержания клавиатуры и мыши подаются в него из
	 * тех же событий, что расходятся по UI. {@code null} отключает хук. Содержимое
	 * таблицы привязок — за приложением; движок не знает ни одного предопределённого действия.
	 */
	public void setActionRegistry(ActionRegistry registry) {
		this.actionRegistry = registry;
	}

	public ActionRegistry getActionRegistry() {
		return actionRegistry;
	}

	/**
	 * Вооружает захват следующей клавиши для {@code target}: следующее известное нажатие
	 * коммитит свой код в колбэк ровно один раз, ESC коммитит UNBOUND_CODE как отмену,
	 * UNKNOWN гасится без развооружения. Пока цель вооружена, сцены обработчики
	 * (включая снятие фокуса ESC'ем и доставку элементу в фокусе) событие не видят;
	 * после завершения коммита действует дебаунс ~20 мс против глобальных шорткатов.
	 * При вооружении вызывается unPressAllKeys() у подключённого реестра. Повторный вызов
	 * ПОДМЕНЯЕТ цель без каких-либо коммитов прежней. {@code null} эквивалентен cancelKeyCapture().
	 */
	public void armKeyCapture(IKeyCaptureTarget target) {
		if (target == null) {
			cancelKeyCapture();
			return;
		}
		this.keyCaptureTarget = target;
		if (actionRegistry != null) {
			actionRegistry.unPressAllKeys();
		}
	}

	/** Развооружает без какого-либо коммита и штампует дебаунс-окно. */
	public void cancelKeyCapture() {
		if (this.keyCaptureTarget == null) {
			return;
		}
		this.keyCaptureTarget = null;
		stampKeyCaptureSuppression();
	}

	public boolean isKeyCaptureArmed() {
		return keyCaptureTarget != null;
	}

	public IKeyCaptureTarget getKeyCaptureTarget() {
		return keyCaptureTarget;
	}

	/** Назначен ли код какому-то зарегистрированному действию — для подсветки дублей в UI. */
	public boolean isKeyCodeTaken(int keyCode) {
		return actionRegistry != null && actionRegistry.isKeyCodeTaken(keyCode);
	}

	@Override
	public void setCamera(ICamera camera) {
		this.camera = camera;
	}

	/**
	 * Enables/disables the controller's built-in keyboard/mouse camera movement. Applications that drive the camera
	 * from their own simulation (or a fixed viewpoint) disable it while keeping focus, tooltip and action routing.
	 */
	public void setCameraControlEnabled(boolean enabled) {
		this.cameraControlEnabled = enabled;
	}

	public boolean isCameraControlEnabled() {
		return cameraControlEnabled;
	}

	@Override
	public void setMouse(IMouse mouse) {
		this.mouse = mouse;
	}

	@Override
	public void setKeyboard(IKeyboard keyboard) {
		this.keyboard = keyboard;
	}

	@Override
	public void setScene(IScene scene) {
		this.scene = scene;
	}

	// --- Всплывающие подсказки -------------------------------------------------

	/** Подсказка не перехватывает клики; многострочный текст (\n) рисуется блоком строк. */
	private static final class TooltipLabel extends Label {

		private List<TextLineSpec> hintLines;

		private TooltipLabel(String text) {
			super(text);
		}

		@Override
		public boolean containsPoint(float x, float y) {
			return false;
		}

		/** Многострочная подсказка распадается на стилизованные строки блока. */
		void refreshStyling() {
			String t = getText();
			if (t.indexOf('\n') < 0) {
				this.hintLines = null;
				return;
			}
			List<TextLineSpec> lines = new ArrayList<>(t.split("\n").length);
			for (String part : t.split("\n")) {
				lines.add(new TextLineSpec(part));
			}
			this.hintLines = lines;
		}

		@Override
		public List<TextLineSpec> getStyledLines() {
			return this.hintLines == null ? Collections.emptyList() : this.hintLines;
		}
	}

	protected long nowNanos() {
		return System.nanoTime();
	}

	/** Запоминаем координаты курсора (для тика таймера без движения мыши). */
	private void noteCursorPosition(float x, float y) {
		this.lastCursorX = x;
		this.lastCursorY = y;
		this.hasLastCursor = true;
	}

	/** Свежие координаты + hit-test: обновляем цель и таймер подсказки. */
	private void updateFromCursor(float cx, float cy) {
		noteCursorPosition(cx, cy);
		IUIElement under = this.scene != null ? this.scene.findElementAt(cx, cy) : null;
		applyTooltipTarget(under);
	}

	/** Смена «владельца» подсказки под курсором или довод таймера показа. */
	private void applyTooltipTarget(IUIElement under) {
		ITooltipSupport ownerNode = nearestToolTipOwner(under);
		String text = ownerNode == null ? "" : ownerNode.getToolTipText();
		if (text.isEmpty()) {
			hideTooltipNow();
			return;
		}
		boolean targetChanged = !(ownerNode == tooltipOwner);
		boolean textStale = tooltipBox == null || !tooltipBox.getText().equals(text);
		if (targetChanged || textStale) {
			ensureTooltipBox(text);
			if (targetChanged) {
				// Пере-задержка при смене цели — как в MC.
				tooltipOwner = (IUIElement) ownerNode;
				hoverStartNanos = nowNanos();
				tooltipBox.setVisible(false);
				return;
			}
		}
		maybeShowTooltip();
	}

	private void maybeShowTooltip() {
		if (tooltipBox == null || tooltipBox.isVisible()) {
			return;
		}
		long waitedMillis = (nowNanos() - hoverStartNanos) / 1_000_000L;
		if (waitedMillis < tooltipDelayMillis) {
			return;
		}
		positionAndShowTooltip();
	}

	/** Каждый кадр из MouseHoldEvent: таймер доходит даже без движения мыши. */
	protected void tickTooltip() {
		if (hasLastCursor && (tooltipOwner != null || tooltipBox != null)) {
			updateFromCursor(lastCursorX, lastCursorY);
		}
	}

	/** Ближайший предок под курсором с непустой подсказкой. */
	private ITooltipSupport nearestToolTipOwner(IUIElement element) {
		for (IUIElement node = element; node != null; node = node.hasParent() ? node.getParent() : null) {
			if (node instanceof ITooltipSupport support && !support.getToolTipText().isEmpty()) {
				return support;
			}
		}
		return null;
	}

	private void ensureTooltipBox(String text) {
		boolean recreate = tooltipBox == null || this.scene != tooltipBoxScene;
		if (recreate) {
			if (tooltipBox != null && tooltipBoxScene != null) {
				tooltipBoxScene.getGuiElements().remove(tooltipBox);
			}
			TooltipLabel box = new TooltipLabel(text);
			box.setBackgroundColor(new Color(15, 17, 20, 245));
			box.setTextColor(new Color(230, 230, 230));
			box.setFont(new ApplicationFont("Default", 12, false, false));
			box.setAlignment(EnumAlignment.LEFT);
			box.setPadding(8);
			box.setBorder(new Border(1, new Color(148, 148, 148)));
			box.setVisible(false);
			box.setZIndex(Integer.MAX_VALUE / 2);
			this.tooltipBox = box;
			this.tooltipBoxScene = this.scene;
			if (this.scene != null) {
				this.scene.getGuiElements().add(box);
			}
		} else if (!tooltipBox.getText().equals(text)) {
			tooltipBox.setText(text);
		}
		((TooltipLabel) tooltipBox).refreshStyling();
	}

	private void positionAndShowTooltip() {
		String text = tooltipBox.getText();
		float padPx = Math.max(tooltipBox.getPadding(), 6f);
		int lineCount = (text.indexOf('\n') < 0) ? 1 : text.split("\n").length;
		int width = (int) measureTooltipBlockWidth(text) + (int) (2 * padPx);
		// Однострочная подсказка сохраняет историческую высоту; многострочная растёт по шагу строки.
		int height = (lineCount > 1) ? (int) Math.ceil(lineCount * tooltipLineAdvance()) + 8 : 26;
		int x = (int) lastCursorX + 14;
		int y = (int) lastCursorY + 20;
		// E9: границы окна в активном логическом пространстве (по умолчанию legacy 1920×1080).
		int viewW = ViewportSettings.effectiveLogicalWidth();
		int viewH = ViewportSettings.effectiveLogicalHeight();
		if (x + width > viewW - 8) {
			x = (int) lastCursorX - width - 14; // у правого края — слева от курсора
		}
		if (y + height > viewH - 8) {
			y = (int) lastCursorY - height - 12; // у нижнего края — над курсором
		}
		x = Math.max(8, x);
		y = Math.max(8, y);
		tooltipBox.resize(new UIElementGeometry(x, y, width, height));
		tooltipBox.setVisible(true);
	}

	/** Ширина блока подсказки: максимальная ширина его строк. */
	private float measureTooltipBlockWidth(String text) {
		String longest = "";
		for (String part : text.split("\n")) {
			if (part.trim().length() > longest.length()) {
				longest = part.trim();
			}
		}
		if (longest.isEmpty()) {
			longest = text;
		}
		UnicodeGlyphFont font = FontSystem.getInstance().ensureFont(new ApplicationFont("Default", 12, false, false));
		return font == null ? Math.max(24f, longest.length() * 7f) : font.getStringWidth(longest);
	}

	/** Шаг строки шрифта подсказок — общая метрика с многострочным блоком текста. */
	private float tooltipLineAdvance() {
		UnicodeGlyphFont font = FontSystem.getInstance().ensureFont(new ApplicationFont("Default", 12, false, false));
		float h = (font == null) ? 0f : font.getMaxHeight();
		return TextMetrics.lineAdvance(h, 12);
	}

	private void hideTooltipNow() {
		if (tooltipBox != null) {
			tooltipBox.setVisible(false);
		}
		tooltipOwner = null;
		hoverStartNanos = -1L;
	}

	public Label getToolTipBoxForTests() {
		return tooltipBox;
	}

}

