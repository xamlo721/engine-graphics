package com.xamlo.core.engine.graphics.api.devices;

import org.lwjgl.glfw.GLFWImage;

public interface IWindow {
	
	public void create(int width, int height);
	
	public void setWindowTitle(String title);

	public void setWindowIcon(GLFWImage icon);

	/*
	 * Когда весь кадр отрисован, буферы необходимо поменять местами друг с другом, 
	 * чтобы задний буфер стал передним буфером и наоборот.
	 */
	public void swapBuffers();
	
	/**
	 * Когда пользователь пытается закрыть окно, либо нажав виджет закрытия в строке заголовка, 
	 * либо используя комбинацию клавиш, такую как Alt + F4, этот флаг устанавливается в 1. 
	 * Обратите внимание, что окно на самом деле не закрыто, поэтому ожидается, 
	 * что вы будете отслеживать этот флаг и либо уничтожите окно, 
	 * либо предоставите пользователю какую-либо обратную связь.
	 * 
	 */
	public boolean isCloseRequested();

	public void resize(int x, int y);

	public int getWidth();
	
	public int getHeight();

	public long getWindow();
	
	public void close();

}
