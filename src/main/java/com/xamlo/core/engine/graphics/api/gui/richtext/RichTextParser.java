package com.xamlo.core.engine.graphics.api.gui.richtext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pure state machine over inline format codes inside plain strings (no rendering dependencies).
 *
 * <p>Semantics ported from the classic chat renderer convention:</p>
 * <ul>
 *   <li>{@code §0..§f} set a palette color and clear every style toggle first;</li>
 *   <li>{@code k/l/m/n/o} individually enable random-style/bold/strikethrough/underline/italic (toggles accumulate);</li>
 *   <li>{@code r} clears all toggles AND restores the base (inherited) color;</li>
 *   <li>a code pair whose second character is not recognized ({@code §z}) is silently consumed as a no-op, styles preserved;</li>
 *   <li>a trailing lone {@code §} with nothing after it renders literally.</li>
 * </ul>
 */
public final class RichTextParser {

	/** Format marker character (U+00A7 SECTION SIGN). */
	public static final char MARKER = '\u00a7';

	private static final String CODES = "0123456789abcdefklmnor";

	/**
	 * Parses raw text in the normal pass: color codes resolve through the profile's normal table.
	 *
	 * @param defaultColor ambient fallback for inherited colors, see {@link RichSegment#resolveColor(int)}
	 */
	public List<RichSegment> parse(String raw, StyleProfile profile, int defaultColor) {
		return parseImpl(raw, profile, false);
	}

	/**
	 * Shadow/dark pass variant: color codes resolve to the derived dark variants of palette entries;
	 * reset still returns the bright inherited base (faithful port).
	 */
	public List<RichSegment> parseShadowPass(String raw, StyleProfile profile, int defaultColor) {
		return parseImpl(raw, profile, true);
	}

	private List<RichSegment> parseImpl(String text, StyleProfile profile, boolean shadowColors) {
		if (text == null || text.isEmpty()) {
			return Collections.emptyList();
		}
		Objects.requireNonNull(profile, "profile");
		if (text.indexOf(MARKER) < 0) {
			return Collections.singletonList(RichSegment.plain(text));
		}

		List<RichSegment> out = new ArrayList<>();
		StringBuilder buf = new StringBuilder();
		int color = RichSegment.INHERIT_COLOR;
		boolean randomStyle = false;
		boolean bold = false;
		boolean strikethrough = false;
		boolean underline = false;
		boolean italic = false;

		for (int i = 0; i < text.length();) {
			char c = text.charAt(i);
			if (c == MARKER && i + 1 < text.length()) {
				int idx = CODES.indexOf(Character.toLowerCase(text.charAt(i + 1)));
				int nColor = color;
				boolean nRandom = randomStyle;
				boolean nBold = bold;
				boolean nStrike = strikethrough;
				boolean nUnderline = underline;
				boolean nItalic = italic;
				if (idx >= 0 && idx <= 15) {
					nRandom = false;
					nBold = false;
					nStrike = false;
					nUnderline = false;
					nItalic = false;
					nColor = shadowColors ? profile.shadowColorAt(idx) : profile.colorAt(idx);
				} else if (idx == 16) {
					nRandom = true;
				} else if (idx == 17) {
					nBold = true;
				} else if (idx == 18) {
					nStrike = true;
				} else if (idx == 19) {
					nUnderline = true;
				} else if (idx == 20) {
					nItalic = true;
				} else if (idx == 21) {
					nRandom = false;
					nBold = false;
					nStrike = false;
					nUnderline = false;
					nItalic = false;
					nColor = RichSegment.INHERIT_COLOR;
				}
				if (nColor != color || nRandom != randomStyle || nBold != bold || nStrike != strikethrough
						|| nUnderline != underline || nItalic != italic) {
					flush(buf, out, color, randomStyle, bold, strikethrough, underline, italic);
					color = nColor;
					randomStyle = nRandom;
					bold = nBold;
					strikethrough = nStrike;
					underline = nUnderline;
					italic = nItalic;
				}
				i += 2;
			} else {
				buf.append(c);
				i++;
			}
		}
		flush(buf, out, color, randomStyle, bold, strikethrough, underline, italic);
		return out;
	}

	private static void flush(StringBuilder buf, List<RichSegment> out, int color, boolean randomStyle,
			boolean bold, boolean strikethrough, boolean underline, boolean italic) {
		if (!buf.isEmpty()) {
			out.add(new RichSegment(buf.toString(), color, bold, italic, underline, strikethrough, randomStyle));
		}
		buf.setLength(0);
	}

}
