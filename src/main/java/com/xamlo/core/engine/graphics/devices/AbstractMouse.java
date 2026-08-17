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
	protected Vector2f previousCursorPosition;
	protected Vector2f cursorPositionDiff;
	protected final Vector2f scrollDelta = new Vector2f(0f, 0f);
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
	public ArrayList<EnumMouseButtons> getReleasedButtons() {
		return releasedButtons;
	}

	@Override
	public Vector2f getCursorPosition() {
		return cursorPosition;
	}

	@Override
	public Vector2f getLockedCursorPosition() {
		return previousCursorPosition;
	}

	@Override
	public Vector2f getCursorPositionDiff() {
		return cursorPositionDiff;
	}

	@Override
	public void setCursorPositionDiff(Vector2f cursorPosition) {
		this.cursorPositionDiff = cursorPosition;
	}
	
	@Override
	public void setLockedCursorPosition(Vector2f lockedCursorPosition) {
		this.previousCursorPosition = lockedCursorPosition;
	}

	@Override
	public Vector2f getScrollDelta() {
		// Защитная копия: callback'и GLFW могут накапливать параллельно со снимком.
		return new Vector2f(scrollDelta);
	}

	@Override
	public void addScrollTick(float deltaX, float deltaY) {
		scrollDelta.x += deltaX;
		scrollDelta.y += deltaY;
	}

	/** Сбрасывает накопленную дельту колеса (вызывается из update() после снимка кадра). */
	protected void resetScrollDelta() {
		scrollDelta.set(0f, 0f);
	}

}
