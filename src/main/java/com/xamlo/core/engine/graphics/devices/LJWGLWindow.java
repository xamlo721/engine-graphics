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
		// glfwPollEvents() здесь был убран намеренно: RenderThread уже качает события
		// в начале каждой итерации ДО снимка ввода (updateInputDevices). Пул между
		// построением снимка и очисткой накопленных списков devices создавал «мёртвую зону»:
		// каждое нажатие доставлялось сюда, попадало в pushed/released после копия и
		// стиралось тем же циклом, так что edge'и никогда не попадали ни в один кадр.
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
	public void setVsyncEnabled(boolean enabled) {
		super.setVsyncEnabled(enabled);
		glfwSwapInterval(enabled ? 1 : 0);
	}

	@Override
	public void swapBuffers() {
		glfwSwapBuffers(window);
	}


	
	@Override
	public void resize(int width, int height){
		// Программный запрос из кода приложения: применить + попросить GLFW о новом размере.
		// E9: поля и событие обновляются только при реально изменившемся размере —
		// повторный вызов с тем же размером GLFW не дёргает и события не шлёт.
		if (!super.applyNewSize(width, height)) {
			return;
		}
		glfwSetWindowSize(window, width, height);
	}

	/**
	 * Нативное изменение окна (X/compositor сам переместил/изменил размер): фиксируем только
		поля и событие. Обратного glfwSetWindowSize нет намеренно: эхо уже применённого размера
		посреди активного жеста ресайза рассинхронизирует WM/CSD — мигание, «ломанные» рамки,
		залипающие чёрные области.
	 */
	void onNativeResize(int width, int height) {
	    super.applyNewSize(width, height);
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
