package com.xamlo.core.engine.graphics.components;

import org.joml.Vector3f;

import com.xamlo.core.engine.graphics.api.components.ICamera;
import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.components.ISceneController;
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
import com.xamlo.core.engine.graphics.api.gui.IWheelTarget;

import com.xamlo.core.engine.graphics.components.gui.Border;
import com.xamlo.core.engine.graphics.components.gui.Color;
import com.xamlo.core.engine.graphics.components.gui.EnumAlignment;
import com.xamlo.core.engine.graphics.components.gui.Label;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.font.ApplicationFont;
import com.xamlo.core.engine.graphics.font.UnicodeGlyphFont;
import com.xamlo.core.engine.graphics.fontsystem.FontSystem;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;

import com.xamlo.engine.api.devices.EnumKeyboardButtons;
import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.api.devices.IKeyboard;
import com.xamlo.engine.api.devices.IMouse;
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

import net.lenni0451.asmevents.event.EventTarget;

public class DefaultSceneController implements ISceneController {

	private ICamera camera;
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
	
	// Элемент, принимающий клавиатурный ввод (устанавливается по клику / ESC снимает)
	private IUIElement focusedElement;
	
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
	
	private static final float movAmt = 0.0011f;

	public DefaultSceneController() {
		this.inHUDMode = false;
	}
	
	@Override
	@EventTarget(noParamEvents = KeyboardClickEvent.class)
	public void onKeyboardClienEvent(final KeyboardClickEvent event) {

        // ESC снимает фокус с элемента ввода
        if (event.getButton() == EnumKeyboardButtons.KEY_ESCAPE) {
            clearFocus();
        }

        // Клавиша уходит элементу в фокусе (текстовое поле и т.п.)
        if (focusedElement instanceof IKeyboardHandler handler) {
            boolean ctrl = isModifierHeld(EnumKeyboardButtons.KEY_LEFT_CONTROL, EnumKeyboardButtons.KEY_RIGHT_CONTROL);
            boolean shift = isModifierHeld(EnumKeyboardButtons.KEY_LEFT_SHIFT, EnumKeyboardButtons.KEY_RIGHT_SHIFT);
            handler.onKeyPressed(event.getButton(), ctrl, shift);
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
        // Edge-событие «отпущена».
    }
	
	@Override
	@EventTarget(noParamEvents = KeyboardHoldEvent.class)
	public void onKeyboardHoldEvent(final KeyboardHoldEvent event) {

		// Пока элемент ввода в фокусе, клавиатура «его»: тикаем его (мигание caret)
		// и не двигаем камерой, чтобы набор текста не управлял миром.
		if (focusedElement != null) {
			if (focusedElement instanceof IFrameTickable tickable) {
				tickable.onFrame();
			}
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
		
		switch (event.getButton()) {
			
		case MOUSE_BUTTON_1: {
			
			IUIElement element = this.scene.findElementAt(event.getxPos(), event.getyPos());

			if (element == null) {
				clearFocus();
				break;
			}

			// Клик по фокусируемому элементу (или его потомку) ставит фокус,
			// клик мимо — снимает.
			focusNearestFocusable(element);

			if (element instanceof IClickable) {
				((IClickable)element).getClickListener().onClicked(((IClickable)element));
			}

			if (element instanceof IPointer pointer) {
				IPointerListener listener = pointer.getPointerListener();
				if (listener != null) {
					listener.onPointer(element, event.getxPos(), event.getyPos());
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
		noteCursorPosition(event.getxPos(), event.getyPos());
	}

	@Override
	@EventTarget(noParamEvents = MouseButtonPressEvent.class)
	public void onMouseButtonPressEvent(final MouseButtonPressEvent event) {
		if (event.getButton() != EnumMouseButtons.MOUSE_BUTTON_1) {
			return;
		}
		// Старт выделения: ближайший к курсору элемент, поддерживающий выделение.
		IUIElement element = this.scene.findElementAt(event.getxPos(), event.getyPos());
		IUIElement target = nearestTextSelectionHandler(element);
		this.selectingElement = target;
		if (target != null) {
			// Поле, где начинают выделять, получает и клавиатурный фокус
			// (drag без клика не прогоняет focusNearestFocusable из click-обработчика).
			focusNearestFocusable(target);
			((ITextSelectionHandler) target).onSelectionStart(event.getxPos(), event.getyPos());
		}
		noteCursorPosition(event.getxPos(), event.getyPos());
	}

	@Override
	@EventTarget(noParamEvents = MouseButtonReleaseEvent.class)
	public void onMouseButtonReleaseEvent(final MouseButtonReleaseEvent event) {
		if (event.getButton() == EnumMouseButtons.MOUSE_BUTTON_1 && selectingElement != null) {
			((ITextSelectionHandler) selectingElement).onSelectionEnd(event.getxPos(), event.getyPos());
			selectingElement = null;
		}
		noteCursorPosition(event.getxPos(), event.getyPos());
	}

	/** Прокрутка колеса: дельта уходит ближайшему предку-таргету под курсором. */
	@Override
	@EventTarget(noParamEvents = MouseScrollEvent.class)
	public void onMouseScrollEvent(final MouseScrollEvent event) {
		IUIElement element = this.scene.findElementAt(event.getxPos(), event.getyPos());
		for (IUIElement candidate = element; candidate != null; candidate = candidate.hasParent() ? candidate.getParent() : null) {
			if (candidate instanceof IWheelTarget target) {
				target.onScrolled(event.getDeltaX(), event.getDeltaY());
				return;
			}
		}
	}
	
	@Override
	@EventTarget(noParamEvents = MouseHoldEvent.class)
	public void onMouseHoldEvent(final MouseHoldEvent event) {

		tickTooltip();
		hudMouseButtonHeld = event.getHeldButtons().contains(EnumMouseButtons.MOUSE_BUTTON_2);
		leftButtonHeld = event.getHeldButtons().contains(EnumMouseButtons.MOUSE_BUTTON_1);
	}
	
	@Override
	@EventTarget(noParamEvents = MouseHoverEvent.class)
	public void onMouseHoverEvent(MouseHoverEvent event) {

		// Идёт выделение перетаскиванием — растягиваем его до курсора.
		if (selectingElement != null && leftButtonHeld) {
			((ITextSelectionHandler) selectingElement).onSelectionDrag(event.getXCoord(), event.getYCoord());
			return;
		}

		if (hudMouseButtonHeld && this.inHUDMode) {
			camera.rotate(new Vector3f( event.getDy() * movAmt * 10, event.getDx() * movAmt * 10, 0.0f));
			return;
		}
		
		
		for (IUIElement otherElement : scene.getGuiElements()) {
			if ( otherElement instanceof IHoverable ) {
				((IHoverable)otherElement).setHovered(false);
			}
		}
		IUIElement element = this.scene.findElementAt(event.getXCoord(), event.getYCoord());

		if (element != null && element instanceof IHoverable) {
			IHoverable hoverableElement = (IHoverable)element;
			hoverableElement.setHovered(true);
		}
		
		updateFromCursor(event.getXCoord(), event.getYCoord());
	}

	@Override
	@EventTarget(noParamEvents = MouseDragAndropEvent.class)
	public void onMouseDragAndDropEvent(final MouseDragAndropEvent event) {
		
		switch (event.getPhase()) {
			
			case START: {
				// Жест стал перетаскиванием — ищем элемент под курсором без исключений
				IUIElement found = this.scene.findElementAt(event.getxPos(), event.getyPos());
				if (found instanceof IDraggable && ((IDraggable)found).getDragListener() != null) {
					this.draggingElement = (IDraggable)found;
					IDragListener listener = draggingElement.getDragListener();
					listener.onDragStart(draggingElement, event.getxPos(), event.getyPos());
				} else {
					this.draggingElement = null;
				}
				break;
			}
			
			case DRAG: {
				if (draggingElement == null) break;
				
				IDragListener listener = draggingElement.getDragListener();
				
				// Таргет под курсором: сам перетаскиваемый элемент из хит-теста исключаем
				IUIElement underCursor = this.scene.findElementAt(event.getxPos(), event.getyPos(), draggingElement);
				IDropTarget newTarget = (underCursor instanceof IDropTarget) ? (IDropTarget)underCursor : null;
				
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
				
				listener.onDrag(draggingElement, event.getxPos(), event.getyPos(), event.getDeltaX(), event.getDeltaY());
				break;
			}
			
			case END: {
				if (draggingElement == null) break;
				
				IDragListener listener = draggingElement.getDragListener();
				
				if (dragTarget != null) {
					dragTarget.setDropActive(false);
					listener.onDrop(draggingElement, dragTarget, event.getxPos(), event.getyPos());
				} else {
					IUIElement underCursor = this.scene.findElementAt(event.getxPos(), event.getyPos(), draggingElement);
					if (underCursor instanceof IDropTarget) {
						IDropTarget lastSecond = (IDropTarget)underCursor;
						lastSecond.setDropActive(false);
						listener.onDrop(draggingElement, lastSecond, event.getxPos(), event.getyPos());
					} else {
						listener.onDragEnd(draggingElement, event.getxPos(), event.getyPos());
					}
				}
				
				this.draggingElement = null;
				this.dragTarget = null;
				break;
			}
		}
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

	@Override
	public void setCamera(ICamera camera) {
		this.camera = camera;
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

	private static final int WINDOW_W = 1920;
	private static final int WINDOW_H = 1080;

	/** Подсказка не перехватывает клики: containsPoint всегда false. */
	private static final class TooltipLabel extends Label {
		private TooltipLabel(String text) {
			super(text);
		}

		@Override
		public boolean containsPoint(float x, float y) {
			return false;
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
	}

	private void positionAndShowTooltip() {
		String text = tooltipBox.getText();
		float padPx = Math.max(tooltipBox.getPadding(), 6f);
		int width = (int) measureTooltipTextWidth(text) + (int) (2 * padPx);
		int height = 26;
		int x = (int) lastCursorX + 14;
		int y = (int) lastCursorY + 20;
		if (x + width > WINDOW_W - 8) {
			x = (int) lastCursorX - width - 14; // у правого края — слева от курсора
		}
		if (y + height > WINDOW_H - 8) {
			y = (int) lastCursorY - height - 12; // у нижнего края — над курсором
		}
		x = Math.max(8, x);
		y = Math.max(8, y);
		tooltipBox.resize(new UIElementGeometry(x, y, width, height));
		tooltipBox.setVisible(true);
	}

	private float measureTooltipTextWidth(String text) {
		UnicodeGlyphFont font = FontSystem.getInstance().ensureFont(new ApplicationFont("Default", 12, false, false));
		return font == null ? Math.max(24f, text.length() * 7f) : font.getStringWidth(text);
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

