package com.xamlo.core.engine.graphics.api.devices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.xamlo.engine.api.devices.EnumKeyboardButtons;
import com.xamlo.engine.api.devices.EnumMouseButtons;

/**
 * Иммутабельный снимок состояния устройств ввода на один кадр рендера.
 * Производится потоком LWJGL (единственный писатель), потребляется одним
 * диспетчерским потоком через {@link IInputFrameProvider}. Все коллекции —
 * защитные копии, поэтому чтение из другого потока безопасно без
 * дополнительных блокировок.
 */
public final class InputFrame {

	private final long sequence;
	private final Set<EnumKeyboardButtons> pushedKeys;
	private final Set<EnumKeyboardButtons> releasedKeys;
	private final Set<EnumKeyboardButtons> keysHolding;
	private final List<Character> charsTyped;
	private final List<EnumMouseButtons> pushedButtons;
	private final List<EnumMouseButtons> releasedButtons;
	private final List<EnumMouseButtons> buttonsHolding;
	private final float cursorX;
	private final float cursorY;
	private final float scrollOffset;

	public InputFrame(long sequence,
			Set<EnumKeyboardButtons> pushedKeys,
			Set<EnumKeyboardButtons> releasedKeys,
			Set<EnumKeyboardButtons> keysHolding,
			List<Character> charsTyped,
			List<EnumMouseButtons> pushedButtons,
			List<EnumMouseButtons> releasedButtons,
			List<EnumMouseButtons> buttonsHolding,
			float cursorX,
			float cursorY,
			float scrollOffset) {
		this.sequence = sequence;
		this.pushedKeys = Collections.unmodifiableSet(new HashSet<>(pushedKeys));
		this.releasedKeys = Collections.unmodifiableSet(new HashSet<>(releasedKeys));
		this.keysHolding = Collections.unmodifiableSet(new HashSet<>(keysHolding));
		this.charsTyped = new ArrayList<>(charsTyped);
		this.pushedButtons = new ArrayList<>(pushedButtons);
		this.releasedButtons = new ArrayList<>(releasedButtons);
		this.buttonsHolding = new ArrayList<>(buttonsHolding);
		this.cursorX = cursorX;
		this.cursorY = cursorY;
		this.scrollOffset = scrollOffset;
	}

	public long getSequence() {
		return sequence;
	}

	public Set<EnumKeyboardButtons> getPushedKeys() {
		return pushedKeys;
	}

	public Set<EnumKeyboardButtons> getReleasedKeys() {
		return releasedKeys;
	}

	public Set<EnumKeyboardButtons> getKeysHolding() {
		return keysHolding;
	}

	public List<Character> getCharsTyped() {
		return charsTyped;
	}

	public List<EnumMouseButtons> getPushedButtons() {
		return pushedButtons;
	}

	public List<EnumMouseButtons> getReleasedButtons() {
		return releasedButtons;
	}

	public List<EnumMouseButtons> getButtonsHolding() {
		return buttonsHolding;
	}

	public float getCursorX() {
		return cursorX;
	}

	public float getCursorY() {
		return cursorY;
	}

	public float getScrollOffset() {
		return scrollOffset;
	}
}
