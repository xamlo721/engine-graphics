package com.xamlo.core.engine.graphics.devices;

import org.lwjgl.glfw.GLFWScrollCallback;

public class MouseScrollCallback extends GLFWScrollCallback {
	
	private AbstractMouse mouse;
	
	public MouseScrollCallback(AbstractMouse mouse) {
		this.mouse = mouse;
	}

	@Override
	public void invoke(long window, double xoffset, double yoffset) {
		this.mouse.setScrollOffset((float) yoffset);
	}

}
