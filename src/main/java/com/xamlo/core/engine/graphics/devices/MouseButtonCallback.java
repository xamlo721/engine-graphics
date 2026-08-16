package com.xamlo.core.engine.graphics.devices;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import com.xamlo.engine.api.devices.EnumMouseButtons;

public class MouseButtonCallback extends GLFWMouseButtonCallback {

	private AbstractMouse mouse;

	public MouseButtonCallback(AbstractMouse mouse) {
		this.mouse = mouse;
	}

    @Override
    public void invoke(long window, int keyCode, int action, int mods) {

        synchronized (mouse) {

	    	EnumMouseButtons key = converteMouseKeycode(keyCode);

	        if (action == GLFW_PRESS){
	        	if (!this.mouse.pushedButtons.contains(key)){
	        		this.mouse.pushedButtons.add(key);
	        		this.mouse.buttonsHolding.add(key);
	        	}
	        }

	        if (action == GLFW_RELEASE){
	        	this.mouse.releasedButtons.add(key);
	        	this.mouse.buttonsHolding.remove(key);
	        }
        }

    }
    

	/**
	 * @param ljwglKeyCode целочисленны код клавиши из Opengl
	 * @return EnumMouseButtons тип клавишы, единый для всего движка
	 */
	private EnumMouseButtons converteMouseKeycode(int ljwglKeyCode) {
		
		switch (ljwglKeyCode) {
			case GLFW.GLFW_MOUSE_BUTTON_1: return EnumMouseButtons.MOUSE_BUTTON_1;
			case GLFW.GLFW_MOUSE_BUTTON_2: return EnumMouseButtons.MOUSE_BUTTON_2;
			case GLFW.GLFW_MOUSE_BUTTON_3: return EnumMouseButtons.MOUSE_BUTTON_3;
			case GLFW.GLFW_MOUSE_BUTTON_4: return EnumMouseButtons.MOUSE_BUTTON_4;
			case GLFW.GLFW_MOUSE_BUTTON_5: return EnumMouseButtons.MOUSE_BUTTON_5;
			case GLFW.GLFW_MOUSE_BUTTON_6: return EnumMouseButtons.MOUSE_BUTTON_6;
			case GLFW.GLFW_MOUSE_BUTTON_7: return EnumMouseButtons.MOUSE_BUTTON_7;
			case GLFW.GLFW_MOUSE_BUTTON_8: return EnumMouseButtons.MOUSE_BUTTON_8;

			
			default:
				return EnumMouseButtons.MOUSE_BUTTON_UNCKNOWN;
		}
		
	}

}
