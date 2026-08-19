package com.xamlo.core.engine.graphics.api.gui.richtext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Word-wrap semantics ported from the classic chat wrap algorithm with an injectable measurer. */
public class RichTextWrapperTest {

	private static final StyleProfile PROFILE = new StyleProfile(
			new int[] {
					0xFF000001, 0xFF000002, 0xFF000003, 0xFF000004, 0xFF000005, 0xFF000006,
					0xFFFF8000, 0xFF000007, 0xFF000008, 0xFF000009, 0xFF00000A, 0xFF00000B,
					0xFF00000C, 0xFF00000D, 0xFF00000E, 0xFF00000F });
	private static final RichTextParser PARSER = new RichTextParser();

	private static List<RichSegment> single(String text) {
		return List.of(RichSegment.plain(text));
	}

	@Test
	void singleLineWhenItFits() {
		assertEquals(List.of(single("short")), RichTextWrapper.split(single("short"), 10, s -> s.length()));
	}

	@Test
	void splitsAtLastSpaceAndDropsTrailingSpace() {
		List<List<RichSegment>> lines = RichTextWrapper.split(single("aaaa bbbb"), 5, s -> s.length());
		assertEquals(List.of(single("aaaa"), single("bbbb")), lines);
	}

	@Test
	void hardSplitsOverlongWordPreservingAllChars() {
		List<List<RichSegment>> lines = RichTextWrapper.split(single("abcdefghij"), 4, s -> s.length());
		assertEquals(List.of(single("abcd"), single("efgh"), single("ij")), lines);
		String joined = String.join("", lines.stream().map(l -> l.get(0).text()).toList());
		assertEquals("abcdefghij", joined);
	}

	@Test
	void measurerDrivesTheCutNotCharCountAssumption() {
		RichSegment input = RichSegment.plain("aa b");
		List<List<RichSegment>> byLength = RichTextWrapper.split(List.of(input), 4, s -> s.length());
		assertEquals(List.of(single("aa b")), byLength);

		List<List<RichSegment>> byWeight = RichTextWrapper.split(List.of(input), 4, RichTextWrapperTest::wideAWeight);
		assertEquals(List.of(single("a"), single("a"), single("b")), byWeight);
	}

	private static int wideAWeight(String s) {
		int w = 0;
		for (int i = 0; i < s.length(); i++) {
			w += s.charAt(i) == 'a' ? 3 : 1;
		}
		return w;
	}

	@Test
	void wrapKeepsStylingAtContinuationStarts() {
		List<RichSegment> segments = PARSER.parse("\u00a7llongword here", PROFILE, -1);
		List<RichSegment> expectedRows = List.of(
				new RichSegment("longwo", RichSegment.INHERIT_COLOR, true, false, false, false, false),
				new RichSegment("rd", RichSegment.INHERIT_COLOR, true, false, false, false, false),
				new RichSegment("here", RichSegment.INHERIT_COLOR, true, false, false, false, false));
		assertEquals(expectedRows.stream().map(List::of).toList(), RichTextWrapper.split(segments, 6, s -> s.length()));
	}

	@Test
	void embeddedNewlineSplitsAndTrailingNewlineAddsNoRow() {
		List<List<RichSegment>> lines = RichTextWrapper.split(single("A\nB"), 100, s -> s.length());
		assertEquals(List.of(single("A"), single("B")), lines);

		List<List<RichSegment>> trailing = RichTextWrapper.split(single("A\n"), 100, s -> s.length());
		assertEquals(List.of(single("A")), trailing);
	}

	@Test
	void styleCarriesAcrossLiteralNewlineByDesign() {
		// Intentional divergence from the reference implementation: its per-line slate reset drops styles across \n;
		// here segment data carries user-set styling forward by construction.
		List<RichSegment> segments = PARSER.parse("\u00a7lX\nY", PROFILE, -1);
		List<RichSegment> expectedRows = List.of(
				new RichSegment("X", RichSegment.INHERIT_COLOR, true, false, false, false, false),
				new RichSegment("Y", RichSegment.INHERIT_COLOR, true, false, false, false, false));
		assertEquals(expectedRows.stream().map(List::of).toList(), RichTextWrapper.split(segments, 100, s -> s.length()));
	}

	@Test
	void emptyInputYieldsSingleEmptyLineLikeReferenceSplitOfEmptyString() {
		assertTrue(RichTextWrapper.split(null, 5, s -> s.length()).get(0).isEmpty());
		assertTrue(RichTextWrapper.split(List.of(), 5, s -> s.length()).get(0).isEmpty());
		assertTrue(RichTextWrapper.split(single(""), 5, s -> s.length()).get(0).isEmpty());
	}

	@Test
	void degenerateZeroBudgetTerminatesWithSoloLines() {
		List<List<RichSegment>> lines = RichTextWrapper.split(single("ab"), 0, s -> s.length());
		assertEquals(List.of(single("a"), single("b")), lines);
	}

}
