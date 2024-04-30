package com.xamlo.core.engine.graphics.devices;


import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import org.joml.Vector2f;

import com.xamlo.engine.api.devices.EnumMouseButtons;

public class LJWGLMouse extends AbstractMouse {
	
	public LJWGLMouse() {
		
		cursorPosition = new Vector2f();
		previousCursorPosition = new Vector2f();
		cursorPositionDiff = new Vector2f();
		
		glfwSetMouseButtonCallback(LJWGLWindow.getInstance().getWindow(), new MouseButtonCallback(this));
		
		glfwSetCursorPosCallback(LJWGLWindow.getInstance().getWindow(), new CursorPosCallback(this));
		
		glfwSetScrollCallback(LJWGLWindow.getInstance().getWindow(), new MouseScrollCallback(this));
		
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
