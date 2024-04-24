package com.xamlo.core.engine.graphics.devices;


import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import org.joml.Vector2f;

public class LJWGLMouse extends AbstractMouse {
	
	public LJWGLMouse() {
		
		cursorPosition = new Vector2f();
		
		glfwSetMouseButtonCallback(LJWGLWindow.getInstance().getWindow(), new MouseButtonCallback(this));
		
		glfwSetCursorPosCallback(LJWGLWindow.getInstance().getWindow(), new CursorPosCallback(this));
		
		glfwSetScrollCallback(LJWGLWindow.getInstance().getWindow(), new MouseScrollCallback(this));
		
	}
	
	@Override
	public void update() {
		setScrollOffset(0);
		pushedButtons.clear();
		releasedButtons.clear();
	}
	
	@Override
	public void setCursorPosition(Vector2f cursorPosition) {
		this.cursorPosition = cursorPosition;
		
		glfwSetCursorPos(LJWGLWindow.getInstance().getWindow(), cursorPosition.x(), cursorPosition.y());
	}
	
}
