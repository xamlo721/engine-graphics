package com.xamlo.core.engine.graphics.devices;

import java.util.ArrayList;

import com.xamlo.core.engine.graphics.api.devices.IUpdatableDevice;
import com.xamlo.engine.api.devices.EnumKeyboardButtons;
import com.xamlo.engine.api.devices.IKeyboard;

public abstract class AbstractKeyboard implements IKeyboard, IUpdatableDevice {

	protected ArrayList<EnumKeyboardButtons> pushedKeys = new ArrayList<EnumKeyboardButtons>();
	protected ArrayList<EnumKeyboardButtons> keysHolding = new ArrayList<EnumKeyboardButtons>();
	protected ArrayList<EnumKeyboardButtons> releasedKeys = new ArrayList<EnumKeyboardButtons>();

	@Override
	public boolean isKeyPushed(EnumKeyboardButtons key) {
		return pushedKeys.contains(key);
	}

	@Override
	public boolean isKeyReleased(EnumKeyboardButtons key) {
		return releasedKeys.contains(key);
	}

	@Override
	public boolean isKeyHold(EnumKeyboardButtons key) {
		return keysHolding.contains(key);
	}

	@Override
	public ArrayList<EnumKeyboardButtons> getPushedKeys() {
		return pushedKeys;
	}


	@Override
	public ArrayList<EnumKeyboardButtons> getKeysHolding() {
		return keysHolding;
	}



}
