package com.xamlo.core.engine.graphics.api.gui.richtext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Parser semantics ported from the classic chat renderer convention, driven by an injected palette. */
public class RichTextParserTest {

	private static final int[] PALETTE = {
			0xFF000001, 0xFF000002, 0xFF000003, 0xFF000004, 0xFF000005, 0xFF000006,
			0xFFFF8000, // slot 6 chosen so quarter-darkening is exact and easy to assert
			0xFF000007, 0xFF000008, 0xFF000009, 0xFF00000A, 0xFF00000B, 0xFF00000C,
			0xFF00000D, 0xFF00000E, 0xFF00000F
	};

	private static final StyleProfile PROFILE = new StyleProfile(PALETTE);
	private static final RichTextParser PARSER = new RichTextParser();

	private static char codeChar(int index) {
		return index < 10 ? (char) ('0' + index) : (char) ('a' + index - 10);
	}

	@Test
	void plainTextReturnsSingleInheritingSegment() {
		List<RichSegment> segments = PARSER.parse("Hello world", PROFILE, -1);
		assertEquals(List.of(RichSegment.plain("Hello world")), segments);
		RichSegment seg = segments.get(0);
		assertEquals(RichSegment.INHERIT_COLOR, seg.color());
		assertFalse(seg.bold());
		assertFalse(seg.italic());
		assertFalse(seg.underline());
		assertFalse(seg.strikethrough());
		assertFalse(seg.randomStyle());
	}

	@Test
	void nullAndEmptyInputYieldNoSegments() {
		assertTrue(PARSER.parse(null, PROFILE, -1).isEmpty());
		assertTrue(PARSER.parse("", PROFILE, -1).isEmpty());
	}

	@Test
	void injectedPaletteResolvesAllSixteenColors() {
		for (int i = 0; i < 16; i++) {
			String raw = "\u00a7" + codeChar(i) + "X";
			List<RichSegment> segments = PARSER.parse(raw, PROFILE, -1);
			assertEquals(1, segments.size(), raw);
			assertEquals(PALETTE[i], segments.get(0).color(), raw);
			assertEquals("X", segments.get(0).text(), raw);
		}
	}

	@Test
	void uppercaseCodesAreEquivalentToLowercase() {
		assertEquals(PARSER.parse("\u00a7cX", PROFILE, -1), PARSER.parse("\u00a7CX", PROFILE, -1));
		assertEquals(PARSER.parse("\u00a7lB", PROFILE, -1), PARSER.parse("\u00a7LB", PROFILE, -1));
	}

	@Test
	void shadowPassUsesDerivedDarkVariantsForEverySlot() {
		for (int i = 0; i < 16; i++) {
			int expectedNormal = StyleProfile.DEFAULT_SHADOW_DERIVATION.applyAsInt(PALETTE[i]);
			assertEquals(expectedNormal, PROFILE.shadowColorAt(i), "slot " + i);
		}
		for (int i = 0; i < 16; i++) {
			String raw = "\u00a7" + codeChar(i) + "X";
			RichSegment normal = PARSER.parse(raw, PROFILE, -1).get(0);
			RichSegment dark = PARSER.parseShadowPass(raw, PROFILE, -1).get(0);
			assertEquals(PROFILE.colorAt(i), normal.color());
			assertEquals(PROFILE.shadowColorAt(i), dark.color());
		}
		assertEquals(0xFF3F2000, PROFILE.shadowColorAt(6));
	}

	@Test
	void styleTogglesAccumulateUntilResetClearsEverything() {
		// vanilla quirk ported faithfully: toggles accumulate; an earlier 'k' keeps applying until a color code or 'r' clears it
		List<RichSegment> expected = List.of(
				new RichSegment("A", RichSegment.INHERIT_COLOR, false, false, false, false, true),          // k: random
				new RichSegment("B", RichSegment.INHERIT_COLOR, true, false, false, false, true),           // +l: bold (random persists)
				new RichSegment("C", RichSegment.INHERIT_COLOR, true, false, false, true, true),            // +m: strikethrough
				new RichSegment("D", RichSegment.INHERIT_COLOR, true, false, true, true, true),             // +n: underline
				new RichSegment("E", RichSegment.INHERIT_COLOR, true, true, true, true, true),              // +o: italic
				RichSegment.plain("F"));                                                                  // r: base color, no styles
		assertEquals(expected, PARSER.parse("\u00a7kA\u00a7lB\u00a7mC\u00a7nD\u00a7oE\u00a7rF", PROFILE, -1));
	}

	@Test
	void colorCodeClearsStyleTogglesFirst() {
		RichSegment seg = PARSER.parse("\u00a7l\u00a7cX", PROFILE, -1).get(0);
		assertFalse(seg.bold(), "color code must clear prior toggles");
		assertEquals(PALETTE[12], seg.color());
	}

	@Test
	void unknownSecondCharIsSilentlyConsumedWithoutChangingState() {
		List<RichSegment> styled = PARSER.parse("\u00a7la\u00a7!b", PROFILE, -1);
		assertEquals(List.of(new RichSegment("ab", RichSegment.INHERIT_COLOR, true, false, false, false, false)), styled);

		List<RichSegment> plain = PARSER.parse("a\u00a7zb", PROFILE, -1);
		assertEquals(List.of(RichSegment.plain("ab")), plain);
	}

	@Test
	void trailingLoneMarkerRendersLiterallyAndStaysSafe() {
		assertEquals(List.of(RichSegment.plain("abc\u00a7")), PARSER.parse("abc\u00a7", PROFILE, -1));
	}

	@Test
	void adjacentCodesWithoutTextProduceNoEmptySegments() {
		List<RichSegment> segments = PARSER.parse("\u00a7c\u00a7lXY", PROFILE, -1);
		assertEquals(1, segments.size());
		RichSegment seg = segments.get(0);
		assertEquals("XY", seg.text());
		assertTrue(seg.bold());
		assertEquals(PALETTE[12], seg.color());
	}

	@Test
	void resetRestoresInheritedBaseColorAndClearsToggles() {
		List<RichSegment> normal = PARSER.parse("\u00a7l\u00a7cA\u00a7rB", PROFILE, 0xFFABCDEF);
		assertFalse(normal.get(1).bold());
		assertEquals(RichSegment.INHERIT_COLOR, normal.get(1).color());

		List<RichSegment> shadow = PARSER.parseShadowPass("\u00a7l\u00a7cA\u00a7rB", PROFILE, 0xFFABCDEF);
		assertEquals(PROFILE.shadowColorAt(12), shadow.get(0).color(), "shadow pass darkens color codes");
		assertEquals(RichSegment.INHERIT_COLOR, shadow.get(1).color(), "reset returns bright inherited base even in shadow pass");
	}

	@Test
	void resolveColorFallsBackOnlyForInheritedSegments() {
		int fallback = 0x12345678;
		assertEquals(fallback, RichSegment.plain("t").resolveColor(fallback));
		RichSegment explicit = new RichSegment("t", 0xFF99AAAA, false, false, false, false, false);
		assertEquals(0xFF99AAAA, explicit.resolveColor(fallback));
	}

}
