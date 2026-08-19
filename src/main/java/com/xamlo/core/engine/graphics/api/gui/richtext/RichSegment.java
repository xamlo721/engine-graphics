package com.xamlo.core.engine.graphics.api.gui.richtext;

/**
 * Immutable run of rich text sharing one resolved style, produced by {@link RichTextParser}.
 *
 * <p>A non-negative {@code color} is an ARGB value (0xAARRGGBB). {@link #INHERIT_COLOR} ({@code -1})
 * means "use ambient/default color": the parser emits it for text before any color code and after a reset.</p>
 */
public record RichSegment(
		String text,
		int color,
		boolean bold,
		boolean italic,
		boolean underline,
		boolean strikethrough,
		boolean randomStyle) {

	/** Sentinel meaning "inherit surrounding default color". */
	public static final int INHERIT_COLOR = -1;

	public RichSegment {
		if (text == null) {
			text = "";
		}
	}

	/** Plain unstyled segment inheriting its color. */
	public static RichSegment plain(String text) {
		return new RichSegment(text, INHERIT_COLOR, false, false, false, false, false);
	}

	/** Resolves this segment's color against an ambient fallback: inherited colors become {@code fallback}. */
	public int resolveColor(int fallback) {
		return color == INHERIT_COLOR ? fallback : color;
	}

	/** Returns a copy of this segment with different text but identical style/color (used by line splitting). */
	public RichSegment withText(String newText) {
		return new RichSegment(newText, color, bold, italic, underline, strikethrough, randomStyle);
	}

}
