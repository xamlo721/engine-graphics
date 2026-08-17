package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.gui.TextLineSpec;
import com.xamlo.core.engine.graphics.font.ApplicationFont;

/** Чистая математика вёрстки: выравнивание, порядок строк, жадный перенос, жёсткий срез. */
public class TextLayoutTest {

	private static final float CHAR_W = 4f;
	private static final float ADVANCE = 20f;

	/** Поддельные метрики: каждый символ (включая пробел) одинаковой ширины. */
	private final TextLayout.Measure measure = new TextLayout.Measure() {
		@Override
		public float widthOf(String text) {
			return text.length() * CHAR_W;
		}

		@Override
		public float widthOf(TextLineSpec context, String text) {
			return text.length() * CHAR_W;
		}
	};

	private final TextLayout.LineAdvance advance = spec -> ADVANCE;

	@Test
	public void leftAlignmentStacksLinesTopToBottom() {
		List<TextLineSpec> specs = Arrays.asList(new TextLineSpec("AB"), new TextLineSpec("CD"));

		List<TextLayout.PlacedLine> placed = TextLayout.layout(100f, 50f, 200, 100, 6f, EnumAlignment.LEFT, false,
				specs, measure, advance);

		assertEquals(2, placed.size());
		assertEquals("AB", placed.get(0).text);
		assertEquals(106f, placed.get(0).xLeftPx, 1e-4f, "левое выравнивание от padding'а");
		assertEquals(76f, placed.get(0).yBottomPx, 1e-4f, "первая строка: верх + pad + шаг");
		assertEquals("CD", placed.get(1).text);
		assertEquals(96f, placed.get(1).yBottomPx, 1e-4f, "вторая строка ниже на один шаг");
	}

	@Test
	public void centerAndRightOffsetsFollowRowWidth() {
		List<TextLineSpec> specs = Arrays.asList(new TextLineSpec("A"), new TextLineSpec("BBBBBBB"));

		List<TextLayout.PlacedLine> centered = TextLayout.layout(100f, 50f, 200, 100, 6f, EnumAlignment.CENTER, false,
				specs, measure, advance);
		assertEquals(100f + (200 - 4) / 2f, centered.get(0).xLeftPx, 1e-4f);
		assertEquals(100f + (200 - 28) / 2f, centered.get(1).xLeftPx, 1e-4f);

		List<TextLayout.PlacedLine> right = TextLayout.layout(100f, 50f, 200, 100, 6f, EnumAlignment.RIGHT, false,
				specs, measure, advance);
		assertEquals(100f + 200 - 6f - 4f, right.get(0).xLeftPx, 1e-4f);
		assertEquals(100f + 200 - 6f - 28f, right.get(1).xLeftPx, 1e-4f);
	}

	@Test
	public void nullAlignmentBehavesAsLeft() {
		List<TextLineSpec> specs = List.of(new TextLineSpec("AB"));

		List<TextLayout.PlacedLine> placed = TextLayout.layout(100f, 50f, 200, 100, 6f, null, false, specs, measure, advance);

		assertEquals(106f, placed.get(0).xLeftPx, 1e-4f);
	}

	@Test
	public void greedyWrapKeepsWordOrderAndBreaksLate() {
		// «AA BB» ровно помещается (20px), третья пара уже не влезает.
		TextLineSpec spec = new TextLineSpec("AA BB CC DD EE");

		List<TextLayout.PlacedLine> placed = TextLayout.layout(0f, 0f, 32, 100, 6f, EnumAlignment.LEFT, true,
				List.of(spec), measure, advance);

		assertEquals(Arrays.asList("AA BB", "CC DD", "EE"), rowTexts(placed));
		float baseY = 6f + ADVANCE;
		assertEquals(baseY, placed.get(0).yBottomPx, 1e-4f);
		assertEquals(baseY + ADVANCE, placed.get(1).yBottomPx, 1e-4f);
		assertEquals(baseY + 2 * ADVANCE, placed.get(2).yBottomPx, 1e-4f);
	}

	@Test
	public void longTokenSplitsByCharacters() {
		TextLineSpec spec = new TextLineSpec("ABCDEFGH"); // без пробелов: только жёсткий срез

		List<TextLayout.PlacedLine> placed = TextLayout.layout(0f, 0f, 32, 100, 6f, EnumAlignment.LEFT, true,
				List.of(spec), measure, advance);

		assertEquals(Arrays.asList("ABCDE", "FGH"), rowTexts(placed), "срез по символу на пределе ширины");
	}

	@Test
	public void emptyLineStillOccupiesRowHeight() {
		List<TextLineSpec> specs = Arrays.asList(new TextLineSpec("A"), new TextLineSpec(""), new TextLineSpec("B"));

		List<TextLayout.PlacedLine> placed = TextLayout.layout(0f, 0f, 100, 80, 6f, EnumAlignment.LEFT, false,
				specs, measure, advance);

		assertEquals(3, placed.size());
		float baseY = 6f + ADVANCE;
		assertEquals(baseY, placed.get(0).yBottomPx, 1e-4f);
		assertEquals("", placed.get(1).text);
		assertEquals(baseY + ADVANCE, placed.get(1).yBottomPx, 1e-4f, "пустая строка сохраняет вертикальный ритм");
		assertEquals(baseY + 2 * ADVANCE, placed.get(2).yBottomPx, 1e-4f);
	}

	@Test
	public void perLineStylePassesThroughPlacedLines() {
		Color lineColor = new Color(9, 8, 7, 255);
		ApplicationFont lineFont = new ApplicationFont("Mono", 14, true, false);
		TextLineSpec spec = new TextLineSpec("X", lineColor, lineFont);

		List<TextLayout.PlacedLine> placed = TextLayout.layout(0f, 0f, 60, 40, 4f, EnumAlignment.LEFT, false,
				List.of(spec), measure, advance);

		assertSame(lineColor, placed.get(0).color);
		assertSame(lineFont, placed.get(0).lineFont);
		assertTrue(placed.get(0).xLeftPx >= 4f - 1e-4f);
	}

	private List<String> rowTexts(List<TextLayout.PlacedLine> rows) {
		return rows.stream().map(r -> r.text).collect(java.util.stream.Collectors.toList());
	}

}
