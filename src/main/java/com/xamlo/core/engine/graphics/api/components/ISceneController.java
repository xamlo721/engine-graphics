package com.xamlo.core.engine.graphics.api.components;

import com.xamlo.engine.api.devices.IKeyboard;
import com.xamlo.engine.api.devices.IMouse;
import com.xamlo.engine.device.events.KeyboardClickEvent;
import com.xamlo.engine.device.events.MouseClickEvent;
import com.xamlo.engine.device.events.MouseDragAndDropEvent;
import com.xamlo.engine.device.events.MouseHoverEvent;

public interface ISceneController {
	
	public void setCamera(ICamera camera);
	
	public void setMouse(IMouse mouse);
	
	public void setKeyboard(IKeyboard keyboard);
	
	public void setScene(IScene scene);

	public void onKeyboardClienEvent(final KeyboardClickEvent event);

	public void onMouseClienEvent(final MouseClickEvent event);

	public void onMouseHoverEvent(final MouseHoverEvent event);

	public void onMouseDragAndDropEvent(final MouseDragAndDropEvent event);

}
