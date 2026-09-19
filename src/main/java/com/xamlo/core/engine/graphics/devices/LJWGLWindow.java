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

        // Placement: find a monitor whose workarea fits the requested size (the primary handle can be NULL under some compositors),
        // then center inside its workarea so multi-head setups do not reject large windows placed at (0,0) of another head.
        int winX = 0;
        int winY = 0;
        java.nio.IntBuffer waX = java.nio.IntBuffer.allocate(1);
        java.nio.IntBuffer waY = java.nio.IntBuffer.allocate(1);
        java.nio.IntBuffer waW = java.nio.IntBuffer.allocate(1);
        java.nio.IntBuffer waH = java.nio.IntBuffer.allocate(1);
        long monHandle = glfwGetPrimaryMonitor();
        if (monHandle == 0L || !workAreaFits(monHandle, width, height, waX, waY, waW, waH)) {
            org.lwjgl.PointerBuffer mons = glfwGetMonitors(); // can be NULL on hosts without detectable monitors
            int monCount = mons == null ? 0 : mons.capacity();
            for (int i = 0; i < monCount; ++i) {
                long m = mons.get(i);
                if (m == 0L) continue;
                if (workAreaFits(m, width, height, waX, waY, waW, waH)) {
                    monHandle = m;
                    break;
                } else if (monHandle == 0L) {
                    monHandle = m; // remember the largest-candidate seen so far as a placement anchor
                }
            }
        }
        if (monHandle != 0L && workAreaFits(monHandle, width, height, waX, waY, waW, waH)) {
            winX = Math.max(waX.get(0), waX.get(0) + (waW.get(0) - width) / 2);
            winY = Math.max(waY.get(0), waY.get(0) + (waH.get(0) - height) / 2);
        }
        System.err.println("[window] placing " + width + "x" + height + " at (" + winX + "," + winY + ") monitor=" + (monHandle == 0L ? "none" : Long.toString(monHandle)));

		window = glfwCreateWindow(width, height, "base window title", winX, winY);
		
		if(window == 0) {
			System.err.println("[window] first create failed at "+width+"x"+height+", retrying after full attribute reset");
			glfwDefaultWindowHints();
			glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
			glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			glfwWindowHint(GLFW_PLATFORM, platform);
			window = glfwCreateWindow(width, height, "base window title", winX, winY);
		}

        // Robust fallback: keep trying the requested size with alternating hint variants; heal libglfw in place once plain
        // retries are exhausted. Probe-sized geometry is only a last resort so the UI canvas keeps its real dimensions.
		int attempt = 0;
		while (window == 0 && ++attempt <= 8) {
			try { Thread.sleep(250); } catch (InterruptedException ignore) {}
			if (attempt == 4) {
				System.err.println("[window] plain retries exhausted, forcing glfwTerminate+glfwInit");
				try { glfwTerminate(); } catch (Throwable t) {}
				boolean ok = glfwInit();
				System.err.println("[window] forced re-init result=" + ok);
			}
			glfwDefaultWindowHints();
			glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
			glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
			if ((attempt & 1) != 0) {
				glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			} else {
				glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
				glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			}
			glfwWindowHint(GLFW_PLATFORM, platform);
            int w = (attempt > 6) ? Math.min(width, 640) : width; // probe-sized geometry only as the very last resort
            int h = (attempt > 6) ? Math.min(height, 480) : height;
			long candidate = glfwCreateWindow(w, h, "base window title", winX, winY);
			if (candidate == 0L) {
				System.err.println("[window] retry #" + attempt + " (" + w + "x" + h + ") failed");
			} else {
				System.err.println("[window] recovered on retry #" + attempt + " at " + w + "x" + h);
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

	private static boolean workAreaFits(long monitor, int needW, int needH, java.nio.IntBuffer outX, java.nio.IntBuffer outY, java.nio.IntBuffer outW, java.nio.IntBuffer outH) {
		glfwGetMonitorWorkarea(monitor, outX, outY, outW, outH);
		return outW.get(0) >= needW && outH.get(0) >= needH;
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
