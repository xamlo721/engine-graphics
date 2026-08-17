package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.gui.TextLineSpec;

/** Модель содержимого блока и его размещение через переопределяемые метрики. */
public class TextBlockTest {

	/** Блок с поддельными метриками: символ = 4px, шаг строки фиксированный. */
	private static final class FakeMetricsBlock extends TextBlock {
		FakeMetricsBlock() {
			super();
		}

		@Override
		protected float lineAdvanceOf(TextLineSpec spec) {
			return 20f;
		}

		@Override
		protected TextLayout.Measure createMeasure() {
			return new TextLayout.Measure() {
				@Override
				public float widthOf(String text) {
					return text.length() * 4f;
				}

				@Override
				public float widthOf(TextLineSpec context, String text) {
					return text.length() * 4f;
				}
			};
		}
	}

	@Test
	public void addLinesAccumulateAndSetTextReplacesAll() {
		TextBlock block = new TextBlock();
		block.addLine("one").addLine(new TextLineSpec("two"));
		assertEquals(2, block.getLines().size());
		assertEquals("one\ntwo", block.getText());

		block.setText("replaced");
		assertEquals(1, block.getLines().size(), "setText заменяет всё содержимое");
		assertEquals("replaced", block.getText());
	}

	@Test
	public void getLinesIsUnmodifiableView() {
		FakeMetricsBlock block = new FakeMetricsBlock();
		block.addLine("a");

		assertThrows(UnsupportedOperationException.class, () -> block.getLines().clear());
	}

	@Test
	public void placedLinesFollowGeometryPaddingAndWrapFlag() {
		FakeMetricsBlock block = new FakeMetricsBlock();
		block.resize(new UIElementGeometry(100, 50, 32, 80));
		block.setPadding(6);
		block.setAlignment(EnumAlignment.LEFT);
		block.setWordWrap(false);
		block.addLine("AA BB CC DD EE");

		List<TextLayout.PlacedLine> withoutWrap = block.computePlacedLines();
		assertEquals(1, withoutWrap.size(), "без переноса строка не делится");
		assertEquals(106f, withoutWrap.get(0).xLeftPx, 1e-4f);

		block.setWordWrap(true);
		assertTrue(block.isWordWrapEnabled());
		List<TextLayout.PlacedLine> wrapped = block.computePlacedLines();
		assertEquals(List.of("AA BB", "CC DD", "EE"), rowTexts(wrapped), "перенос по словам под ширину блока");
		float baseY = 50 + 6 + 20; // absY + pad + шаг
		assertEquals(baseY, wrapped.get(0).yBottomPx, 1e-4f);
		assertEquals(baseY + 20f, wrapped.get(1).yBottomPx, 1e-4f);
	}

	@Test
	public void wordWrapIsOnByDefaultAndSplitsLongLines() {
		FakeMetricsBlock block = new FakeMetricsBlock();
		assertTrue(block.isWordWrapEnabled(), "перенос включён по умолчанию");
		block.resize(new UIElementGeometry(0, 0, 32, 80));
		block.setPadding(6);
		block.setAlignment(EnumAlignment.LEFT);
		block.addLine("AA BB CC DD EE");

		assertEquals(List.of("AA BB", "CC DD", "EE"), rowTexts(block.computePlacedLines()));
	}

	@Test
	public void selfClippingFlagFollowsToggle() {
		TextBlock block = new TextBlock();
		assertTrue(block.isContentClipped(), "контент блока клипится рамкой по умолчанию");
		block.setSelfClipping(false);
		assertFalse(block.isContentClipped());
	}

	private List<String> rowTexts(List<TextLayout.PlacedLine> rows) {
		return rows.stream().map(r -> r.text).collect(java.util.stream.Collectors.toList());
	}

}
