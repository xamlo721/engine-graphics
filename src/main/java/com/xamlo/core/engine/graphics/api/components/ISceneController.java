package com.xamlo.core.engine.graphics.api.components;

import com.xamlo.engine.api.devices.IKeyboard;
import com.xamlo.engine.api.devices.IMouse;
import com.xamlo.engine.device.events.KeyboardClickEvent;
import com.xamlo.engine.device.events.KeyboardHoldEvent;
import com.xamlo.engine.device.events.KeyboardReleaseEvent;
import com.xamlo.engine.device.events.MouseButtonReleaseEvent;
import com.xamlo.engine.device.events.MouseClickEvent;
import com.xamlo.engine.device.events.MouseDragAndropEvent;
import com.xamlo.engine.device.events.MouseHoldEvent;
import com.xamlo.engine.device.events.MouseHoverEvent;

public interface ISceneController {
	
	public void setCamera(ICamera camera);
	
	public void setMouse(IMouse mouse);
	
	public void setKeyboard(IKeyboard keyboard);
	
	public void setScene(IScene scene);

	/** Нажатие клавиши (edge): одно событие на физическое нажатие. */
	public void onKeyboardClienEvent(final KeyboardClickEvent event);

	/** Отпускание клавиши (edge). */
	public void onKeyboardReleaseEvent(final KeyboardReleaseEvent event);

	/** Непрерывный набор удерживаемых клавиш — для «пока зажато»-логики и движения камеры. */
	public void onKeyboardHoldEvent(final KeyboardHoldEvent event);

	/** Настоящий клик мышью: отпускание кнопки без значительного смещения с момента нажатия. */
	public void onMouseClienEvent(final MouseClickEvent event);

	/** Отпускание кнопки мыши по координатам отпускания. */
	public void onMouseButtonReleaseEvent(final MouseButtonReleaseEvent event);

	/** Непрерывный набор удерживаемых кнопок мыши — для «пока зажато»-логики без чтения устройств напрямую. */
	public void onMouseHoldEvent(final MouseHoldEvent event);

	public void onMouseHoverEvent(final MouseHoverEvent event);

	public void onMouseDragAndDropEvent(final MouseDragAndropEvent event);

}
