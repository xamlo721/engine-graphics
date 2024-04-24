package com.xamlo.core.engine.graphics.devices;

import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.GL_FALSE;

public class LJWGLWindow extends AbstractWindow {

	private static LJWGLWindow instance = null;

	public static LJWGLWindow getInstance() {
	    if(instance == null) {
	    	instance = new LJWGLWindow();
	    }
	      return instance;
	}
		
	@Override
	public void create(int width, int height) {
		
		this.width = width;
		this.height = height;
		
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        
		//Установка минимальных версий OpenGL 3.3
		//Если требуемая минимальная версия не поддерживается на компьютере, создание контекста (и окна) завершается ошибкой
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);	
		//Установка профайла для которого создается контекст
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);	
		
		//Включение возможности изменения размера окна
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);	
		
		window = glfwCreateWindow(width, height, "base window title", 0, 0);
		
		if(window == 0) {
			//Хотя создание окон редко завершается сбоем, 
			//создание контекста зависит от правильно установленных драйверов и 
			//может завершиться сбоем даже на машинах с необходимым оборудованием
		    throw new RuntimeException("Failed to create window");
		}

		/**
		 * Создание текущего контекста
		 * 
		 * Контекст будет оставаться текущим до тех пор, 
		 * пока вы не сделаете текущим другой контекст или пока окно, 
		 * владеющее текущим контекстом, не будет уничтожено
		 */
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		glfwShowWindow(window);
		
		/**
		 * Кроме того, поскольку буферы будут заменены в середине обновления экрана, что приведет к разрыву экрана.
		 * По этим причинам приложения обычно хотят установить интервал подкачки равным единице.
		 *  Можно установить более высокие значения, но обычно это не рекомендуется из-за задержки ввода, к которой это приводит.
		 */
		glfwSwapInterval(1);
		
		glfwSetFramebufferSizeCallback(LJWGLWindow.getInstance().getWindow(), new WindowCallback());

	}

	@Override
	public void update() {
		//Пул ивентс вызывает обработки движений клавиатуры, мышки и т.д
		//Без него не будет нормально передвигаться окно
		//Вызывать надо в начале каждого цикла, но пока не знаю куда перетащить
		glfwPollEvents();		
	}
	
	@Override
	public void setWindowTitle(String title) {
		glfwSetWindowTitle(window, title);
	}
	
	@Override
	public void setWindowIcon(GLFWImage icon) {
		GLFWImage.Buffer images = GLFWImage.malloc(1);
        images.put(0, icon);
		glfwSetWindowIcon(window, images);
	}

	@Override
	public void swapBuffers() {
		glfwSwapBuffers(window);
	}


	
	@Override
	public void resize(int width, int height){
		glfwSetWindowSize(window, width, height);
		this.width = width;
		this.height = height;
	}

	@Override
	public boolean isCloseRequested() {
		return glfwWindowShouldClose(window);
	}
	
	@Override
	public void close() {
		glfwDestroyWindow(window);
	}



}
