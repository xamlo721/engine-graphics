package com.xamlo.core.engine.graphics.components;

import org.joml.Vector3f;

import com.xamlo.core.engine.graphics.api.components.ICamera;
import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.components.ISceneController;
import com.xamlo.core.engine.graphics.api.gui.IClickable;
import com.xamlo.core.engine.graphics.api.gui.IHoverable;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;

import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.api.devices.IKeyboard;
import com.xamlo.engine.api.devices.IMouse;
import com.xamlo.engine.device.events.KeyboardClickEvent;
import com.xamlo.engine.device.events.MouseClickEvent;
import com.xamlo.engine.device.events.MouseDragAndDropEvent;
import com.xamlo.engine.device.events.MouseHoverEvent;

import net.lenni0451.asmevents.event.EventTarget;

public class DefaultSceneController implements ISceneController {

	private ICamera camera;
	private IMouse mouse;
	private IKeyboard keyboard;
	private IScene scene;
	
	//this flag allow to execute mouse click&hover event like a UI actions
	private boolean inHUDMode;
	
	private static final float movAmt = 0.0011f;

	public DefaultSceneController() {
		this.inHUDMode = false;
	}
	
	@Override
	@EventTarget(noParamEvents = KeyboardClickEvent.class)
	public void onKeyboardClienEvent(final KeyboardClickEvent event) {
        
		//System.out.println("KEYBOARD EVENT " + event.getButton().toString());

		switch (event.getButton()) {
		
			case KEY_W: {
				camera.move( new Vector3f(0.0f, 0.0f, -movAmt));
				break;
			}
			case KEY_S: {
				camera.move( new Vector3f(0.0f, 0.0f, movAmt));
				break;
			}
			case KEY_A: {
				camera.move( new Vector3f(-movAmt, 0.0f, 0.0f));
				break;
			}
			case KEY_D: {
				camera.move( new Vector3f(movAmt, 0.0f, 0.0f));
				break;
			}
			case KEY_SPACE: {
				camera.move( new Vector3f(0.0f, movAmt, 0.0f));
				break;
			}
			case KEY_LEFT_SHIFT: {
				camera.move( new Vector3f(0.0f, -movAmt, 0.0f));
				break;
			}
			case KEY_Q: {
				camera.rotate(new Vector3f(0.0f, 0.0f, movAmt * 40));
				break;
			}
			case KEY_E: {
				camera.rotate(new Vector3f(0.0f, 0.0f, -movAmt * 40));
				break;
			}
			default: {
				//throw new IllegalArgumentException("Unexpected value: " + event.getButton());
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
					break;
				}

				if (element instanceof IClickable) {
					//TODO: А это точно нормальное состояние для вызова листенера...

					((IClickable)element).getClickListener().onClicked(((IClickable)element));
				}
				
				break;
			}
			case MOUSE_BUTTON_2: {
				System.out.println("MOUSE 2 CLICK!");
				break;
			}
			case MOUSE_BUTTON_3: {
				System.out.println("MOUSE 3 CLICK!");
				break;
			}
			case MOUSE_BUTTON_4: {
				System.out.println("MOUSE 4 CLICK!");
				this.inHUDMode = true;
				break;
			}
			case MOUSE_BUTTON_5: {
				System.out.println("MOUSE 5 CLICK!");
				this.inHUDMode = false;
				break;
			}
			case MOUSE_BUTTON_6: {
				System.out.println("MOUSE 6 CLICK!");
				break;
			}
			case MOUSE_BUTTON_7: {
				System.out.println("MOUSE 7 CLICK!");
				break;
			}
			default: {
				throw new IllegalArgumentException("Unexpected value: " + event.getButton());
			}
		}


	}

	@Override
	@EventTarget(noParamEvents = MouseHoverEvent.class)
	public void onMouseHoverEvent(MouseHoverEvent event) {

		if (mouse.isButtonHolding(EnumMouseButtons.MOUSE_BUTTON_2) && this.inHUDMode) {
			camera.rotate(new Vector3f( event.getDy() * movAmt * 10, event.getDx() * movAmt * 10, 0.0f));
			return;
		}
		

		
		for (IUIElement otherElement : scene.getGuiElements()) {
			if ( otherElement instanceof IHoverable ) {
				((IHoverable) otherElement).setHovered(false);
			}
		}
		IUIElement element = this.scene.findElementAt(event.getOldXCoord() + event.getDx(), event.getOldYCoord() + event.getDy());

		if (element != null && element instanceof IHoverable) {
			IHoverable hoverableElement = (IHoverable) element;
			hoverableElement.setHovered(true);
		}
//		glfwSetCursorPos(LJWGLWindow.getInstance().getWindow(),
//				mouse.getLockedCursorPosition().x(),
//				mouse.getLockedCursorPosition().y());

		
	}

	@Override
	@EventTarget(noParamEvents = MouseDragAndDropEvent.class)
	public void onMouseDragAndDropEvent(MouseDragAndDropEvent event) {
		// TODO Auto-generated method stub
		
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
