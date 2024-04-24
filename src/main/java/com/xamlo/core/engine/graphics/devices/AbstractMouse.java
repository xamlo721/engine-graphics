package com.xamlo.core.engine.graphics.devices;


import java.util.ArrayList;

import org.joml.Vector2f;

import com.xamlo.core.engine.graphics.api.devices.IUpdatableDevice;
import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.api.devices.IMouse;

public abstract class AbstractMouse implements IMouse, IUpdatableDevice {

	protected ArrayList<EnumMouseButtons> pushedButtons = new ArrayList<EnumMouseButtons>();
	protected ArrayList<EnumMouseButtons> buttonsHolding = new ArrayList<EnumMouseButtons>();
	protected ArrayList<EnumMouseButtons> releasedButtons = new ArrayList<EnumMouseButtons>();

	protected Vector2f cursorPosition;
	protected Vector2f lockedCursorPosition;
	protected float scrollOffset;
	protected boolean showCursor;
	
	@Override
	public boolean isShowCursor() {
		return showCursor;
	}
	
	@Override
	public boolean isButtonPushed(EnumMouseButtons key) {
		return pushedButtons.contains(key);
	}

	@Override
	public boolean isButtonReleased(EnumMouseButtons key) {
		return releasedButtons.contains(key);
	}

	@Override
	public boolean isButtonHolding(EnumMouseButtons key) {
		return buttonsHolding.contains(key);
	}
	
	@Override
	public ArrayList<EnumMouseButtons> getButtonsHolding() {
		return buttonsHolding;
	}

	@Override
	public ArrayList<EnumMouseButtons> getPushedButtons() {
		return pushedButtons;
	}

	@Override
	public Vector2f getCursorPosition() {
		return cursorPosition;
	}

	@Override
	public Vector2f getLockedCursorPosition() {
		return lockedCursorPosition;
	}

	@Override
	public void setLockedCursorPosition(Vector2f lockedCursorPosition) {
		this.lockedCursorPosition = lockedCursorPosition;
	}

	@Override
	public float getScrollOffset() {
		return scrollOffset;
	}

	@Override
	public void setScrollOffset(float scrollOffset) {
		this.scrollOffset = scrollOffset;
	}

}
