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
import com.xamlo.core.engine.graphics.api.gui.IUIElement;

import com.xamlo.engine.api.devices.EnumKeyboardButtons;
import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.api.devices.IKeyboard;
import com.xamlo.engine.api.devices.IMouse;
import com.xamlo.engine.device.events.CharacterInputEvent;
import com.xamlo.engine.device.events.KeyboardClickEvent;
import com.xamlo.engine.device.events.KeyboardHoldEvent;
import com.xamlo.engine.device.events.KeyboardReleaseEvent;
import com.xamlo.engine.device.events.MouseButtonReleaseEvent;
import com.xamlo.engine.device.events.MouseClickEvent;
import com.xamlo.engine.device.events.MouseDragAndropEvent;
import com.xamlo.engine.device.events.MouseHoldEvent;
import com.xamlo.engine.device.events.MouseHoverEvent;

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


	}

	@Override
	@EventTarget(noParamEvents = MouseButtonReleaseEvent.class)
	public void onMouseButtonReleaseEvent(final MouseButtonReleaseEvent event) {
        // Edge-событие отпускания кнопки — зарезервировано под сброс pressed-состояний элементов.
    }
	
	@Override
	@EventTarget(noParamEvents = MouseHoldEvent.class)
	public void onMouseHoldEvent(final MouseHoldEvent event) {
		
		hudMouseButtonHeld = event.getHeldButtons().contains(EnumMouseButtons.MOUSE_BUTTON_2);
	}
	
	@Override
	@EventTarget(noParamEvents = MouseHoverEvent.class)
	public void onMouseHoverEvent(MouseHoverEvent event) {

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

}
