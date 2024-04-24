package com.xamlo.core.engine.graphics.api.components;

public interface IRenderEngine {
	
	public void init();
	
	public void createWindow(int width, int height);
	
	public void start();
	
	public void loadInputDevices();

	public void loadScene();
	
	public void transformScene();
	
	public void renderFrame();
	
	public void updateInputDevices();
	
	public void stop();
	
	public boolean isRendering();
	
	public void release();

}
	