package com.xamlo.core.engine.graphics.api.gui.richtext;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.Test;

/** Profile injectability: any palette/derivation can be supplied; DEFAULT stays neutral and self-consistent. */
public class StyleProfileTest {

	private static final int[] CUSTOM = new int[16];

	static {
		for (int i = 0; i < 16; i++) {
			CUSTOM[i] = 0xFF000000 | ((i + 1) << 8);
		}
	}

	@Test
	void constructorRejectsNullOrWrongLengthPalette() {
		assertThrows(IllegalArgumentException.class, () -> new StyleProfile(null));
		assertThrows(IllegalArgumentException.class, () -> new StyleProfile(new int[15]));
		assertThrows(NullPointerException.class, () -> new StyleProfile(CUSTOM, null));
	}

	@Test
	void defaultProfileIsNeutralAndSelfConsistent() {
		int[] palette = StyleProfile.DEFAULT.getPalette();
		assertEquals(16, palette.length);
		for (int c : palette) {
			assertEquals(0xFF, (c >>> 24) & 0xFF, "default entries carry full alpha");
		}
		assertEquals(StyleProfile.DEFAULT_SHADOW_DERIVATION.applyAsInt(palette[15]), StyleProfile.DEFAULT.shadowColorAt(15));
		assertNotEquals(palette[15], StyleProfile.DEFAULT.shadowColorAt(15), "derived shadow must differ for bright slots");
	}

	@Test
	void customDerivationControlsShadowTableExactly() {
		IntUnaryOperator identity = argb -> argb;
		StyleProfile profile = new StyleProfile(CUSTOM, identity);
		assertArrayEquals(CUSTOM, profile.getShadowPalette());

		StyleProfile quartered = StyleProfile.of(CUSTOM);
		for (int i = 0; i < 16; i++) {
			assertEquals(0xFF000000 | (((i + 1) >>> 2) << 8), quartered.shadowColorAt(i), "slot " + i);
		}
	}

	@Test
	void constructorCopiesInputArrayAndGettersDoNotAliasInternalState() {
		int[] source = CUSTOM.clone();
		StyleProfile profile = new StyleProfile(source);
		source[3] = 0xDEADBEEF;
		assertEquals(CUSTOM[3], profile.colorAt(3), "mutating the caller's array after construction has no effect");

		int[] paletteCopy = profile.getPalette();
		paletteCopy[5] = 0xCAFEBABE;
		assertEquals(CUSTOM[5], profile.colorAt(5), "getPalette returns a defensive copy");

		assertThrows(IndexOutOfBoundsException.class, () -> profile.colorAt(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> profile.colorAt(16));
		assertThrows(IndexOutOfBoundsException.class, () -> profile.shadowColorAt(99));
	}

}
