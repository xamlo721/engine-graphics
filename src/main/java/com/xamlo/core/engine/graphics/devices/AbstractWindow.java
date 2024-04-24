package com.xamlo.core.engine.graphics.devices;

import com.xamlo.core.engine.graphics.api.devices.IUpdatableDevice;
import com.xamlo.core.engine.graphics.api.devices.IWindow;

public abstract class AbstractWindow implements IWindow, IUpdatableDevice {

	protected long window;
	protected int width;
	protected int height;
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public long getWindow() {
		return window;
	}
	
}
