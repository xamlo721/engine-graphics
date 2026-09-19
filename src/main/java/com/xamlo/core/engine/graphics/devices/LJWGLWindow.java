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
		//Если требуемая минимальная версия не поддерживается на компьютере, создание контекста (и окна) завершится ошибкой
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);	
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);		
		// Явный выбор бэкенда вместо автодетекта GLFW: в процессах с заранее загруженными нативными библиотеками
		// автоматический подбор платформы может выбрать нерабочий вариант, хотя каждый бэкенд сам по себе цел.
		String display = System.getenv("DISPLAY");
		int platform = (display != null && !display.isEmpty()) ? GLFW_PLATFORM_X11 : GLFW_PLATFORM_WAYLAND;
		if (!"Linux".equals(System.getProperty("os.name"))) {
			platform = GLFW_ANY_PLATFORM; // win32/cocoa: автоподбор корректен и единственен
		}
		glfwWindowHint(GLFW_PLATFORM, platform);	
		
		//Включение возможности изменения размера окна
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);	
		
		// bisection: принудительный чистый цикл libglfw перед первым create (только под флагом)
		if ("true".equals(System.getProperty("mcport.glfwReset"))) {
			System.err.println("[MCP-WIN] forced glfwTerminate+glfwInit before first create");
			try { glfwTerminate(); } catch (Throwable t) {}
			boolean ok = glfwInit();
			System.err.println("[MCP-WIN] re-init result=" + ok);
		}

		window = glfwCreateWindow(width, height, "base window title", 0, 0);
		
		if(window == 0) {
			System.err.println("[window] first create failed at "+width+"x"+height+", retrying after full attribute reset");
			glfwDefaultWindowHints();
			glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
			glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			glfwWindowHint(GLFW_PLATFORM, platform);
			window = glfwCreateWindow(width, height, "base window title", 0, 0);
		}

        // Robust fallback: some host environments reject the initial hint set but accept the minimal one;
        // probe-style retries (same recipe as the diagnostic below) have been observed to succeed where the
        // primary attempts return NULL. Give it a few chances before giving up.
		int attempt = 0;
		while (window == 0 && ++attempt <= 6) {
			try { Thread.sleep(250); } catch (InterruptedException ignore) {}
            // After the plain retries are exhausted, heal in place: some host environments leave libglfw
            // half-initialized by earlier native loads; only terminate+init restores create capability.
			if (attempt == 4) {
				System.err.println("[window] plain retries exhausted, forcing glfwTerminate+glfwInit");
				try { glfwTerminate(); } catch (Throwable t) {}
				boolean ok = glfwInit();
				System.err.println("[window] forced re-init result=" + ok);
			}
			glfwDefaultWindowHints();
			glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
			if ((attempt & 1) != 0) {
				// Odd attempts mirror the diagnostic probe exactly (major-only version hint): some drivers reject an explicit core-profile pair but accept major only.
				glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
				glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			} else {
				glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
				glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
				glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			}
			glfwWindowHint(GLFW_PLATFORM, platform);
            int w = ((attempt & 1) != 0) ? width : 640; // alternate requested vs probe-size geometry
            int h = ((attempt & 1) != 0) ? height : 480;
			long candidate = glfwCreateWindow(w, h, "base window title", 0L, 0L);
			if (candidate == 0L) {
				System.err.println("[window] retry #" + attempt + " (" + w + "x" + h + ") failed");
			} else {
				System.err.println("[window] recovered on retry #" + attempt);
				window = candidate;
			}
		}

		//[M1-DIAG TEMPORARY] per-platform probe inside failing process — remove once root cause found
		if(window == 0 && Boolean.getBoolean("mcport.windowProbe")) {
			System.err.println("[window-probe] init="+glfwInit()+" curPlatform="+glfwGetPlatform());
			for (int pf : new int[]{GLFW_PLATFORM_X11, GLFW_PLATFORM_WAYLAND, GLFW_ANY_PLATFORM}) {
				glfwDefaultWindowHints();
				glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
				glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
				glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
				glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
				glfwWindowHint(GLFW_PLATFORM, pf);
				long w2 = glfwCreateWindow(640, 480, "diagprobe", 0L, 0L);
				System.out.println("[window-probe] platform "+pf+": " + (w2==0?"NULL":"OK id="+w2));
				if(w2!=0) glfwDestroyWindow(w2);
			}
		}

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
