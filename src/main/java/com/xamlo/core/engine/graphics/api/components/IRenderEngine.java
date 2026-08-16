package com.xamlo.core.engine.graphics.api.components;

import com.xamlo.core.engine.graphics.api.devices.IInputFrameProvider;

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

	/** Запрошено ли закрытие окна пользователем (false до создания окна). */
	public boolean isCloseRequested();
	
	public void stop();
	
	public boolean isRendering();
	
	public void release();

}
	