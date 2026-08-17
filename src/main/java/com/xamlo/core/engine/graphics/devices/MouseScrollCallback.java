package com.xamlo.core.engine.graphics.devices;

import org.lwjgl.glfw.GLFWScrollCallback;

public class MouseScrollCallback extends GLFWScrollCallback {
	
	private AbstractMouse mouse;
	
	public MouseScrollCallback(AbstractMouse mouse) {
		this.mouse = mouse;
	}

	@Override
	public void invoke(long window, double xoffset, double yoffset) {
		// Один тик несёт обе оси; за кадр их может быть несколько — накапливаем.
		this.mouse.addScrollTick((float) xoffset, (float) yoffset);
	}

}
