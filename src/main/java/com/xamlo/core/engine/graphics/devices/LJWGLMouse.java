package com.xamlo.core.engine.graphics.devices;


import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import com.xamlo.engine.api.devices.EnumMouseButtons;

public class LJWGLMouse extends AbstractMouse {

	// Сильные ссылки на объекты колбэков: GLFW держит только нативный указатель,
	// без своих ссылок JVM может собрать их и молча перестать доставлять события.
	private final GLFWMouseButtonCallback mouseButtonCb;
	private final GLFWCursorPosCallback cursorPosCb;
	private final GLFWScrollCallback scrollCb;

	public LJWGLMouse() {

		cursorPosition = new Vector2f();
		previousCursorPosition = new Vector2f();
		cursorPositionDiff = new Vector2f();

		long windowHandle = LJWGLWindow.getInstance().getWindow();

		mouseButtonCb = new MouseButtonCallback(this);
		cursorPosCb = new CursorPosCallback(this);
		scrollCb = new MouseScrollCallback(this);

		glfwSetMouseButtonCallback(windowHandle, mouseButtonCb);

		glfwSetCursorPosCallback(windowHandle, cursorPosCb);

		glfwSetScrollCallback(windowHandle, scrollCb);

	}
	
	@Override
	public void update() {
		
		cursorPositionDiff.x = previousCursorPosition.x - cursorPosition.x;
		cursorPositionDiff.y = previousCursorPosition.y - cursorPosition.y;
		
        // Обновляем предыдущую позицию
		previousCursorPosition.set(cursorPosition);
		
        // Переносим клавиши, которые были нажаты в предыдущем кадре, в список удерживаемых
        for (EnumMouseButtons key : pushedButtons) {
            if (!buttonsHolding.contains(key)) {
            	buttonsHolding.add(key);
            }
        }
        
        // Удаляем из удерживаемых клавиши, которые были отпущены
        buttonsHolding.removeAll(releasedButtons);

		resetScrollDelta();
		pushedButtons.clear();

		releasedButtons.clear();
	}
	
	@Override
	public void setCursorPosition(Vector2f cursorPosition) {
		this.cursorPosition = cursorPosition;
		
		glfwSetCursorPos(LJWGLWindow.getInstance().getWindow(), cursorPosition.x(), cursorPosition.y());
	}

}
