package com.xamlo.core.engine.graphics.devices;

import org.lwjgl.glfw.GLFWCursorPosCallback;

public class CursorPosCallback extends GLFWCursorPosCallback {
	
	private AbstractMouse mouse;
	
	public CursorPosCallback(AbstractMouse mouse) {
		this.mouse = mouse;
	}

    @Override
    public void invoke(long window, double xpos, double ypos) {
    	mouse.cursorPosition.x = ((float) xpos);
    	mouse.cursorPosition.y = ((float) ypos);
    }

}
