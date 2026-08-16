package com.xamlo.core.engine.graphics.api.components;

import com.xamlo.core.engine.graphics.api.devices.IInputFrameProvider;
import com.xamlo.core.engine.graphics.threads.RenderTaskQueue;

public interface IRenderEngine {
	
	public void init();
	
	public void createWindow(int width, int height);
	
	public void start();
	
	public void loadInputDevices();

	public void loadScene();
	
	public void transformScene();
	
	public void renderFrame();
	
	/**
	 * Строит снимок состояния устройств ввода (см. InputFrame) и публикует его в канал
	 * для диспетчерского потока, после чего обновляет сами устройства. Диспетч событий
	 * здесь не выполняется — он живёт на потоке устройства.
	 */
	public void updateInputDevices();
	
	/** Канал снимков ввода: поток рендера публикует, один диспетчерский поток потребляет. */
	public IInputFrameProvider getInputFrames();

	/**
	 * Выполняет задачи, поданные другими потоками через {@link #getRenderTaskQueue()}.
	 * Вызывать на рендер-потоке (владеет GLFW-окном и GL-контекстом).
	 */
	public void processRenderTasks();

	/** Очередь задач на рендер-поток (для не тред-сейф'овых GLFW-вызовов из других потоков). */
	public RenderTaskQueue getRenderTaskQueue();

	/** Запрошено ли закрытие окна пользователем (false до создания окна). */
	public boolean isCloseRequested();
	
	public void stop();
	
	public boolean isRendering();
	
	public void release();

}
	