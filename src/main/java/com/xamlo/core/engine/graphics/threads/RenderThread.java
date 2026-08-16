package com.xamlo.core.engine.graphics.threads;

import org.lwjgl.glfw.GLFW;

import com.xamlo.core.engine.graphics.api.components.IRenderEngine;

/**
 * Я думаю, что я немного ошибся, позволив потоку управлять RenderEngine
 * На самом деле он должен быть частью RenderEngine, а управление
 * должно происходить как и положено прямыми вызовами RednerEngine.init etc
 * Попробую переписать, когда будет понятно какие обязанности на себя берёт engine
 * @author Satomi
 */
public class RenderThread extends Thread {

	private IRenderEngine renderingEngine;
	
	private static final long NANOSECOND = 1000000000;
	@SuppressWarnings("unused")
	private static final long SECOND = 1;

    public RenderThread(IRenderEngine renderingEngine) {
        this.renderingEngine = renderingEngine;
        this.setName("LJWGL-Thread");
    }

    @Override
    public void run() {

    	
		renderingEngine.init();
		//FIXME: Настройки здесь не должны находиться 100%
		renderingEngine.createWindow(1920, 1080);
		renderingEngine.loadInputDevices();
    	renderingEngine.loadScene();	
	    renderingEngine.start();

        
		//****************Пресет***************//
		//Количество отрисованных кадров
		int frames = 0;
		//Время последнего отрисованного кадра
		long lastTime = System.nanoTime();
		//Прошедшее время с начала цикла
		double idleTime = 0;
		//Лимит кадров
		float framerate = 60;
		//Количество кадров за прошлую секунду
		int fps = 0;
		//Счётчик времени по которому мы будем мерить 1 секунду
		int secondsForFpsCounter = 0;
		//Лимит времени на отрисовку 1 кадра
		float frameTime = 1.0f/framerate;
		//************************************//


		while (renderingEngine.isRendering()) {

			//Явная выдача событий ОС: без этого callback'и GLFW доставляются
			//непредсказуемо, и при низком FPS нажатия теряются.
			GLFW.glfwPollEvents();

			if (renderingEngine.isCloseRequested()) {
				break;
			}

			boolean isRenderFrame = false;
						
			//Количество циклов сейчас
			long currentTime = System.nanoTime();

			//прибавим к времени ожидания с прошлого кадра время которое проспал цикл
			idleTime += (currentTime - lastTime) / (double) NANOSECOND;
			
			//Разница в секундах между прошлым замером fps и текущим временем
			int currentDelta = (int) ((currentTime / (double) NANOSECOND) - secondsForFpsCounter);
			
			//Если прошла секунда, отмерить ФПС и сбросиь счётчики
			if (currentDelta > 0) {
				fps = frames;
				frames = 0;
				secondsForFpsCounter +=currentDelta;
			}
			
			//Если с времени последнего кадра прошло времени больше чем время кадра, то пора рисовать следующий
			if (idleTime > frameTime) {
				isRenderFrame = true;
				idleTime -= frameTime;
				lastTime = currentTime;
			}
						
			if(isRenderFrame) {
				renderingEngine.transformScene();
				renderingEngine.renderFrame();
				renderingEngine.updateInputDevices();
				frames++;
			} else {
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
			
			
		}
		
		renderingEngine.stop();
		renderingEngine.release();	

    	
    }

}