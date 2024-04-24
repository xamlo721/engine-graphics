package com.xamlo.core.engine.graphics.devices;

import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

public class LJWGLKeyboard extends AbstractKeyboard {
	
	public LJWGLKeyboard() {
		glfwSetKeyCallback(LJWGLWindow.getInstance().getWindow(), new KeyboardButtonCallback(this));
		
	}

	@Override
	public void update() {
		pushedKeys.clear();
		releasedKeys.clear();
	}

}
