package com.xamlo.core.engine.graphics.api.gui.richtext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Splits parsed rich-text segments into physical lines that fit a pixel budget.
 *
 * <p>Algorithm shape ported from the classic chat word-wrap: scan left to right accumulating width, cut at the last
 * space seen within budget (the trailing space itself is dropped, everything after it moves on), hard-split mid-word
 * when no space exists yet, and treat embedded newlines as forced line breaks with state carry-over. Because every
 * segment carries its resolved style explicitly, each emitted line re-emits active styles from scratch by construction -
 * continuation starts keep their styling without any code-string manipulation.</p>
 *
 * <p>The measurer is injectable so tests can fake widths ({@code s -> s.length()}); production integration binds a font's
 * string-width function here later. Widths are measured per prefix step (O(n^2) worst case, fine for UI strings).</p>
 */
public final class RichTextWrapper {

	private RichTextWrapper() {
	}

	/**
	 * @param segments parsed segments in document order (null/empty yields a single empty line)
	 * @param maxWidthPx horizontal budget in pixels
	 * @param widthOf measures an arbitrary string in pixels
	 * @return one inner list of styled slices per physical line; characters are never lost except spaces/newlines used as break points
	 */
	public static List<List<RichSegment>> split(List<? extends RichSegment> segments, int maxWidthPx, ToIntFunction<String> widthOf) {
		List<? extends RichSegment> input = segments == null ? List.of() : segments;
		Objects.requireNonNull(widthOf, "widthOf");

		int n = 0;
		for (RichSegment seg : input) {
			n += seg.text().length();
		}
		if (n == 0) {
			return new ArrayList<>(List.of(new ArrayList<>()));
		}

		char[] chars = new char[n];
		RichSegment[] owners = new RichSegment[n];
		int[] posInOwner = new int[n];
		int fill = 0;
		for (RichSegment seg : input) {
			String t = seg.text();
			for (int i = 0; i < t.length(); i++) {
				chars[fill] = t.charAt(i);
				owners[fill] = seg;
				posInOwner[fill] = i;
				fill++;
			}
		}

		List<List<RichSegment>> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		int base = 0;
		int lastSpace = -1;

		for (int i = 0; i < n;) {
			char c = chars[i];
			if (c == '\n') {
				emitLine(lines, owners, posInOwner, base, line.length());
				line.setLength(0);
				lastSpace = -1;
				base = i + 1;
				i++;
				continue;
			}
			line.append(c);
			if (c == ' ') {
				lastSpace = line.length() - 1;
			}
			boolean overflow = widthOf.applyAsInt(line.toString()) > maxWidthPx;
			if (!overflow) {
				i++;
				continue;
			}
			int keepEnd = lastSpace >= 0 ? lastSpace : Math.max(0, line.length() - 1);
			if (lastSpace < 0 && keepEnd <= 0) {
				// single glyph wider than the whole budget: force a solo line to guarantee progress
				emitLine(lines, owners, posInOwner, base, line.length());
				line.setLength(0);
				lastSpace = -1;
				base = i + 1;
				i++;
				continue;
			}
			boolean spaceCut = lastSpace >= 0;
			emitLine(lines, owners, posInOwner, base, keepEnd);
			line.setLength(0);
			lastSpace = -1;
			// space cut: the trailing space itself is dropped and everything after it moves on; hard cut retries the overflowing glyph in place
			int resume = spaceCut ? base + keepEnd + 1 : i;
			base = resume;
			i = resume;
		}
		if (!line.isEmpty()) {
			emitLine(lines, owners, posInOwner, base, line.length());
		}
		return lines;
	}

	private static void emitLine(List<List<RichSegment>> outLines, RichSegment[] owners, int[] posInOwner, int startGlobal, int len) {
		List<RichSegment> row = new ArrayList<>();
		int end = startGlobal + len;
		int p = startGlobal;
		while (p < end) {
			RichSegment owner = owners[p];
			int q = p + 1;
			while (q < end && owners[q] == owner) {
				q++;
			}
			String t = owner.text();
			int from = posInOwner[p];
			row.add(owner.withText(t.substring(from, from + (q - p))));
			p = q;
		}
		outLines.add(row);
	}

}
