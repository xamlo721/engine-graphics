package com.xamlo.core.engine.graphics.devices;

import com.xamlo.core.engine.graphics.api.devices.IUpdatableDevice;
import com.xamlo.core.engine.graphics.api.devices.IWindow;
import com.xamlo.engine.device.events.WindowResizeEvent;

import net.lenni0451.asmevents.EventManager;

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

	/**
	 * E9: если размер реально изменился — обновляет поля и шлёт {@link WindowResizeEvent} ровно
	 * один раз по тому же потоку EventManager'а, что и все события устройств (повторный вызов с
	 * прежним размером ничего не делает). Возвращает false, когда менять было нечего.
	 */
	protected boolean applyNewSize(int newWidth, int newHeight) {
	    if (newWidth == this.width && newHeight == this.height) {
	        return false;
	    }
	    this.width = newWidth;
	    this.height = newHeight;
	    EventManager.call(new WindowResizeEvent(width, height));
	    return true;
	}
	
}
