package com.xamlo.core.engine.graphics.api.gui.richtext;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

/**
 * Injectable palette configuration for inline format-code parsing.
 *
 * <p>The engine ships one neutral built-in profile ({@link #DEFAULT}) using common web/CSS hex values in the
 * canonical slot order. It is intentionally NOT tied to any specific application's chat table: every consumer
 * may supply its own 16-entry ARGB palette and its own shadow-pass derivation via {@code of(...)} or the constructor.</p>
 */
public final class StyleProfile {

	/** Default shadow/dark pass derivation: each RGB channel scaled by a quarter, alpha preserved. */
	public static final IntUnaryOperator DEFAULT_SHADOW_DERIVATION = argb -> {
		int r = ((argb >>> 16) & 0xFF) >> 2;
		int g = ((argb >>> 8) & 0xFF) >> 2;
		int b = (argb & 0xFF) >> 2;
		return (((argb >>> 24) & 0xFF) << 24) | (r << 16) | (g << 8) | b;
	};

	private static final int[] DEFAULT_PALETTE = {
			0xFF000000, // §0 black
			0xFF8B0000, // §1 dark red
			0xFF008000, // §2 green
			0xFF008080, // §3 teal
			0xFF000080, // §4 navy
			0xFF800080, // §5 purple
			0xFF8B4513, // §6 saddle brown
			0xFF808080, // §7 gray
			0xFF696969, // §8 dim gray
			0xFFFF0000, // §9 red
			0xFF00FF00, // §A lime
			0xFF00FFFF, // §B aqua
			0xFFFFFF00, // §C yellow
			0xFFFF00FF, // §D magenta
			0xFFC0C0C0, // §E silver
			0xFFFFFFFF  // §F white
	};

	public static final StyleProfile DEFAULT = new StyleProfile(DEFAULT_PALETTE);

	private final int[] colors;
	private final int[] shadows;
	private final IntUnaryOperator shadowDerivation;

	/** @param palette exactly 16 ARGB values in canonical slot order. */
	public StyleProfile(int[] palette) {
		this(palette, DEFAULT_SHADOW_DERIVATION);
	}

	/**
	 * @param palette exactly 16 ARGB values in canonical slot order
	 * @param shadowDerivation maps a normal-pass ARGB value to its shadow/dark pass variant (null uses the default quarter-darken)
	 */
	public StyleProfile(int[] palette, IntUnaryOperator shadowDerivation) {
		if (palette == null || palette.length != 16) {
			throw new IllegalArgumentException("StyleProfile requires exactly 16 color entries");
		}
		this.colors = Arrays.copyOf(palette, 16);
		this.shadowDerivation = Objects.requireNonNull(shadowDerivation, "shadowDerivation");
		int[] derived = new int[16];
		for (int i = 0; i < 16; i++) {
			derived[i] = shadowDerivation.applyAsInt(colors[i]);
		}
		this.shadows = derived;
	}

	/** Convenience factory using {@link #DEFAULT_SHADOW_DERIVATION}. */
	public static StyleProfile of(int[] palette) {
		return new StyleProfile(palette);
	}

	/** Normal-pass ARGB for a slot index. */
	public int colorAt(int index) {
		checkIndex(index);
		return colors[index];
	}

	/** Shadow/dark pass ARGB for a slot index (normal entry passed through the derivation). */
	public int shadowColorAt(int index) {
		checkIndex(index);
		return shadows[index];
	}

	private void checkIndex(int index) {
		if (index < 0 || index >= 16) {
			throw new IndexOutOfBoundsException("color slot out of range: " + index);
		}
	}

	/** Defensive copy of the normal-pass table. */
	public int[] getPalette() {
		return Arrays.copyOf(colors, 16);
	}

	/** Defensive copy of the precomputed shadow-pass table. */
	public int[] getShadowPalette() {
		return Arrays.copyOf(shadows, 16);
	}

	public IntUnaryOperator getShadowDerivation() {
		return shadowDerivation;
	}

}
