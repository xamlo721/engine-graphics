package com.xamlo.core.engine.graphics.devices;

import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

import com.xamlo.engine.api.devices.EnumKeyboardButtons;

public class LJWGLKeyboard extends AbstractKeyboard {
	
	public LJWGLKeyboard() {
		glfwSetKeyCallback(LJWGLWindow.getInstance().getWindow(), new KeyboardButtonCallback(this));
		
	}

	@Override
	public void update() {
		
        // Переносим клавиши, которые были нажаты в предыдущем кадре, в список удерживаемых
        for (EnumKeyboardButtons key : pushedKeys) {
            if (!keysHolding.contains(key)) {
                keysHolding.add(key);
            }
        }
        
        // Удаляем из удерживаемых клавиши, которые были отпущены
        keysHolding.removeAll(releasedKeys);

        // Очищаем списки нажатых и отпущенных клавиш для следующего кадра
		pushedKeys.clear();
		releasedKeys.clear();
	}

}
