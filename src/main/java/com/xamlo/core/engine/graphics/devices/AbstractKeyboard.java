package com.xamlo.core.engine.graphics.devices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.xamlo.core.engine.graphics.api.devices.IUpdatableDevice;
import com.xamlo.engine.api.devices.EnumKeyboardButtons;
import com.xamlo.engine.api.devices.IKeyboard;

public abstract class AbstractKeyboard implements IKeyboard, IUpdatableDevice {

	//Клавиши, которые прожали в текущий тик
	protected Set<EnumKeyboardButtons> pushedKeys = new HashSet<EnumKeyboardButtons>();
	//Клавиши, которые удеживаются ещё с прошлого тика
	protected Set<EnumKeyboardButtons> keysHolding = new HashSet<EnumKeyboardButtons>();
	//Клавиши, которые отпустили
	protected Set<EnumKeyboardButtons> releasedKeys = new HashSet<EnumKeyboardButtons>();
	//Символы, набранные в текущем тик (GLFW char-callback)
	protected List<Character> charsTyped = new ArrayList<Character>();

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
	public Set<EnumKeyboardButtons> getPushedKeys() {
		return pushedKeys;
	}


	@Override
	public Set<EnumKeyboardButtons> getKeysHolding() {
		return keysHolding;
	}

	@Override
	public Set<EnumKeyboardButtons> getReleasedKeys() {
		return releasedKeys;
	}

	@Override
	public List<Character> getCharsTyped() {
		return charsTyped;
	}

}
