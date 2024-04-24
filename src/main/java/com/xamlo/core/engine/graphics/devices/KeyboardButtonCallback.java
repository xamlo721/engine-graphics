package com.xamlo.core.engine.graphics.devices;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import com.xamlo.engine.api.devices.EnumKeyboardButtons;

public class KeyboardButtonCallback extends GLFWKeyCallback {
	
	//Сомнительное решение сюда пробрасывать клавиатуру, ну ладно. может кто знает как лучше?
	private AbstractKeyboard keyboard;
	
	public KeyboardButtonCallback(AbstractKeyboard keyboard) {
		this.keyboard = keyboard;
	}

    @Override
    public void invoke(long window, int keyCode, int scancode, int action, int mods) {
    	
    	EnumKeyboardButtons key = converteKeyboardCode(keyCode);
    	if (action == GLFW_PRESS){
    		if (!keyboard.pushedKeys.contains(key)){
    			keyboard.pushedKeys.add(key);
    			keyboard.keysHolding.add(key);
    		}
        }
    	
        if (action == GLFW_RELEASE) {
        	keyboard.keysHolding.remove(key);
        	keyboard.releasedKeys.add(key);
        }

    }
    
	
	/**
	 * @param ljwglKeyCode целочисленны код клавиши из Opengl
	 * @return EnumKeyboardKey тип клавишы, единый для всего движка
	 */
	private EnumKeyboardButtons converteKeyboardCode(int ljwglKeyCode) {
		
		switch (ljwglKeyCode) {
			case GLFW.GLFW_KEY_SPACE: return EnumKeyboardButtons.KEY_SPACE;
			case GLFW.GLFW_KEY_APOSTROPHE: return EnumKeyboardButtons.KEY_APOSTROPHE;
			case GLFW.GLFW_KEY_COMMA: return EnumKeyboardButtons.KEY_COMMA;
			case GLFW.GLFW_KEY_MINUS: return EnumKeyboardButtons.KEY_MINUS;
			case GLFW.GLFW_KEY_PERIOD: return EnumKeyboardButtons.KEY_PERIOD;
			case GLFW.GLFW_KEY_SLASH: return EnumKeyboardButtons.KEY_SLASH;
			case GLFW.GLFW_KEY_0: return EnumKeyboardButtons.KEY_0;
			case GLFW.GLFW_KEY_1: return EnumKeyboardButtons.KEY_1;
			case GLFW.GLFW_KEY_2: return EnumKeyboardButtons.KEY_2;
			case GLFW.GLFW_KEY_3: return EnumKeyboardButtons.KEY_3;
			case GLFW.GLFW_KEY_4: return EnumKeyboardButtons.KEY_4;
			case GLFW.GLFW_KEY_5: return EnumKeyboardButtons.KEY_5;
			case GLFW.GLFW_KEY_6: return EnumKeyboardButtons.KEY_6;
			case GLFW.GLFW_KEY_7: return EnumKeyboardButtons.KEY_7;
			case GLFW.GLFW_KEY_8: return EnumKeyboardButtons.KEY_8;
			case GLFW.GLFW_KEY_9: return EnumKeyboardButtons.KEY_9;
			case GLFW.GLFW_KEY_SEMICOLON: return EnumKeyboardButtons.KEY_SEMICOLON;
			case GLFW.GLFW_KEY_EQUAL: return EnumKeyboardButtons.KEY_EQUAL;
			case GLFW.GLFW_KEY_A: return EnumKeyboardButtons.KEY_A;
			case GLFW.GLFW_KEY_B: return EnumKeyboardButtons.KEY_B;
			case GLFW.GLFW_KEY_C: return EnumKeyboardButtons.KEY_C;
			case GLFW.GLFW_KEY_D: return EnumKeyboardButtons.KEY_D;
			case GLFW.GLFW_KEY_E: return EnumKeyboardButtons.KEY_E;
			case GLFW.GLFW_KEY_F: return EnumKeyboardButtons.KEY_F;
			case GLFW.GLFW_KEY_G: return EnumKeyboardButtons.KEY_G;
			case GLFW.GLFW_KEY_H: return EnumKeyboardButtons.KEY_H;
			case GLFW.GLFW_KEY_I: return EnumKeyboardButtons.KEY_I;
			case GLFW.GLFW_KEY_J: return EnumKeyboardButtons.KEY_J;
			case GLFW.GLFW_KEY_K: return EnumKeyboardButtons.KEY_K;
			case GLFW.GLFW_KEY_L: return EnumKeyboardButtons.KEY_L;
			case GLFW.GLFW_KEY_M: return EnumKeyboardButtons.KEY_M;
			case GLFW.GLFW_KEY_N: return EnumKeyboardButtons.KEY_N;
			case GLFW.GLFW_KEY_O: return EnumKeyboardButtons.KEY_O;
			case GLFW.GLFW_KEY_P: return EnumKeyboardButtons.KEY_P;
			case GLFW.GLFW_KEY_Q: return EnumKeyboardButtons.KEY_Q;
			case GLFW.GLFW_KEY_R: return EnumKeyboardButtons.KEY_R;
			case GLFW.GLFW_KEY_S: return EnumKeyboardButtons.KEY_S;
			case GLFW.GLFW_KEY_T: return EnumKeyboardButtons.KEY_T;
			case GLFW.GLFW_KEY_U: return EnumKeyboardButtons.KEY_U;
			case GLFW.GLFW_KEY_V: return EnumKeyboardButtons.KEY_V;
			case GLFW.GLFW_KEY_W: return EnumKeyboardButtons.KEY_W;
			case GLFW.GLFW_KEY_X: return EnumKeyboardButtons.KEY_X;
			case GLFW.GLFW_KEY_Y: return EnumKeyboardButtons.KEY_Y;
			case GLFW.GLFW_KEY_Z: return EnumKeyboardButtons.KEY_Z;
			case GLFW.GLFW_KEY_LEFT_BRACKET: return EnumKeyboardButtons.KEY_LEFT_BRACKET1;
			case GLFW.GLFW_KEY_BACKSLASH: return EnumKeyboardButtons.KEY_BACKSLASH;
			case GLFW.GLFW_KEY_RIGHT_BRACKET: return EnumKeyboardButtons.KEY_RIGHT_BRACKET;
			case GLFW.GLFW_KEY_GRAVE_ACCENT: return EnumKeyboardButtons.KEY_GRAVE_ACCENT;
			case GLFW.GLFW_KEY_WORLD_1: return EnumKeyboardButtons.KEY_WORLD_1;
			case GLFW.GLFW_KEY_WORLD_2: return EnumKeyboardButtons.KEY_WORLD_2;
			case GLFW.GLFW_KEY_ESCAPE: return EnumKeyboardButtons.KEY_ESCAPE;
			case GLFW.GLFW_KEY_ENTER: return EnumKeyboardButtons.KEY_ENTER;
			case GLFW.GLFW_KEY_TAB: return EnumKeyboardButtons.KEY_TAB;
			case GLFW.GLFW_KEY_BACKSPACE: return EnumKeyboardButtons.KEY_BACKSPACE;
			case GLFW.GLFW_KEY_INSERT: return EnumKeyboardButtons.KEY_INSERT;
			case GLFW.GLFW_KEY_DELETE: return EnumKeyboardButtons.KEY_DELETE;
			case GLFW.GLFW_KEY_RIGHT: return EnumKeyboardButtons.KEY_RIGHT;
			case GLFW.GLFW_KEY_LEFT: return EnumKeyboardButtons.KEY_LEFT;
			case GLFW.GLFW_KEY_DOWN: return EnumKeyboardButtons.KEY_DOWN;
			case GLFW.GLFW_KEY_UP: return EnumKeyboardButtons.KEY_UP;
			case GLFW.GLFW_KEY_PAGE_UP: return EnumKeyboardButtons.KEY_PAGE_UP;
			case GLFW.GLFW_KEY_PAGE_DOWN: return EnumKeyboardButtons.KEY_PAGE_DOWN;
			case GLFW.GLFW_KEY_HOME: return EnumKeyboardButtons.KEY_HOME;
			case GLFW.GLFW_KEY_END: return EnumKeyboardButtons.KEY_END;
			case GLFW.GLFW_KEY_CAPS_LOCK: return EnumKeyboardButtons.KEY_CAPS_LOCK;
			case GLFW.GLFW_KEY_SCROLL_LOCK: return EnumKeyboardButtons.KEY_SCROLL_LOCK;
			case GLFW.GLFW_KEY_NUM_LOCK: return EnumKeyboardButtons.KEY_NUM_LOCK;
			case GLFW.GLFW_KEY_PRINT_SCREEN: return EnumKeyboardButtons.KEY_PRINT_SCREEN;
			case GLFW.GLFW_KEY_PAUSE: return EnumKeyboardButtons.KEY_PAUSE;
			case GLFW.GLFW_KEY_F1: return EnumKeyboardButtons.KEY_F1;
			case GLFW.GLFW_KEY_F2: return EnumKeyboardButtons.KEY_F2;
			case GLFW.GLFW_KEY_F3: return EnumKeyboardButtons.KEY_F3;
			case GLFW.GLFW_KEY_F4: return EnumKeyboardButtons.KEY_F4;
			case GLFW.GLFW_KEY_F5: return EnumKeyboardButtons.KEY_F5;
			case GLFW.GLFW_KEY_F6: return EnumKeyboardButtons.KEY_F6;
			case GLFW.GLFW_KEY_F7: return EnumKeyboardButtons.KEY_F7;
			case GLFW.GLFW_KEY_F8: return EnumKeyboardButtons.KEY_F8;
			case GLFW.GLFW_KEY_F9: return EnumKeyboardButtons.KEY_F9;
			case GLFW.GLFW_KEY_F10: return EnumKeyboardButtons.KEY_F10;
			case GLFW.GLFW_KEY_F11: return EnumKeyboardButtons.KEY_F11;
			case GLFW.GLFW_KEY_F12: return EnumKeyboardButtons.KEY_F12;
			case GLFW.GLFW_KEY_F13: return EnumKeyboardButtons.KEY_F13;
			case GLFW.GLFW_KEY_F14: return EnumKeyboardButtons.KEY_F14;
			case GLFW.GLFW_KEY_F15: return EnumKeyboardButtons.KEY_F15;
			case GLFW.GLFW_KEY_F16: return EnumKeyboardButtons.KEY_F16;
			case GLFW.GLFW_KEY_F17: return EnumKeyboardButtons.KEY_F17;
			case GLFW.GLFW_KEY_F18: return EnumKeyboardButtons.KEY_F18;
			case GLFW.GLFW_KEY_F19: return EnumKeyboardButtons.KEY_F19;
			case GLFW.GLFW_KEY_F20: return EnumKeyboardButtons.KEY_F20;
			case GLFW.GLFW_KEY_F21: return EnumKeyboardButtons.KEY_F21;
			case GLFW.GLFW_KEY_F22: return EnumKeyboardButtons.KEY_F22;
			case GLFW.GLFW_KEY_F23: return EnumKeyboardButtons.KEY_F23;
			case GLFW.GLFW_KEY_F24: return EnumKeyboardButtons.KEY_F24;
			case GLFW.GLFW_KEY_F25: return EnumKeyboardButtons.KEY_F25;
			case GLFW.GLFW_KEY_KP_0: return EnumKeyboardButtons.KEY_KP_0;
			case GLFW.GLFW_KEY_KP_1: return EnumKeyboardButtons.KEY_KP_1;
			case GLFW.GLFW_KEY_KP_2: return EnumKeyboardButtons.KEY_KP_2;
			case GLFW.GLFW_KEY_KP_3: return EnumKeyboardButtons.KEY_KP_3;
			case GLFW.GLFW_KEY_KP_4: return EnumKeyboardButtons.KEY_KP_4;
			case GLFW.GLFW_KEY_KP_5: return EnumKeyboardButtons.KEY_KP_5;
			case GLFW.GLFW_KEY_KP_6: return EnumKeyboardButtons.KEY_KP_6;
			case GLFW.GLFW_KEY_KP_7: return EnumKeyboardButtons.KEY_KP_7;
			case GLFW.GLFW_KEY_KP_8: return EnumKeyboardButtons.KEY_KP_8;
			case GLFW.GLFW_KEY_KP_9: return EnumKeyboardButtons.KEY_KP_9;
			case GLFW.GLFW_KEY_KP_DECIMAL: return EnumKeyboardButtons.KEY_KP_DECIMAL;
			case GLFW.GLFW_KEY_KP_DIVIDE: return EnumKeyboardButtons.KEY_KP_DIVIDE;
			case GLFW.GLFW_KEY_KP_MULTIPLY: return EnumKeyboardButtons.KEY_KP_MULTIPLY;
			case GLFW.GLFW_KEY_KP_SUBTRACT: return EnumKeyboardButtons.KEY_KP_SUBTRACT;
			case GLFW.GLFW_KEY_KP_ADD: return EnumKeyboardButtons.KEY_KP_ADD;
			case GLFW.GLFW_KEY_KP_ENTER: return EnumKeyboardButtons.KEY_KP_ENTER;
			case GLFW.GLFW_KEY_KP_EQUAL: return EnumKeyboardButtons.KEY_KP_EQUAL;
			case GLFW.GLFW_KEY_LEFT_SHIFT: return EnumKeyboardButtons.KEY_LEFT_SHIFT;
			case GLFW.GLFW_KEY_LEFT_CONTROL: return EnumKeyboardButtons.KEY_LEFT_CONTROL;
			case GLFW.GLFW_KEY_LEFT_ALT: return EnumKeyboardButtons.KEY_LEFT_ALT;
			case GLFW.GLFW_KEY_LEFT_SUPER: return EnumKeyboardButtons.KEY_LEFT_SUPER;
			case GLFW.GLFW_KEY_RIGHT_SHIFT: return EnumKeyboardButtons.KEY_RIGHT_SHIFT;
			case GLFW.GLFW_KEY_RIGHT_CONTROL: return EnumKeyboardButtons.KEY_RIGHT_CONTROL;
			case GLFW.GLFW_KEY_RIGHT_ALT: return EnumKeyboardButtons.KEY_RIGHT_ALT;
			case GLFW.GLFW_KEY_RIGHT_SUPER: return EnumKeyboardButtons.KEY_RIGHT_SUPER;
			case GLFW.GLFW_KEY_MENU: return EnumKeyboardButtons.KEY_MENU;
			
			default:
				return EnumKeyboardButtons.KEY_UNKNOWN;
		}
		
	}

}
