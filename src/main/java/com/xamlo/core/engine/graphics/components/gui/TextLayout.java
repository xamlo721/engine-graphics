package com.xamlo.core.engine.graphics.components.gui;

import java.util.ArrayList;
import java.util.List;

import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.TextLineSpec;
import com.xamlo.core.engine.graphics.api.gui.font.IFont;

/**
 * Чистая математика многострочной вёрстки: позиционирование строк по выравниванию
 * и перенос длинных строк по словам. Без GL и шрифтовой системы — ширина строки
 * и высота шага приходят извне (реальный шрифт или подделка в тестах).
 */
public final class TextLayout {

	/** Метрика ширины фрагмента текста (пиксели); версия со стилем строки — для смешанных шрифтов. */
	public interface Measure {
		float widthOf(String text);

		default float widthOf(TextLineSpec context, String text) {
			return widthOf(text);
		}
	}

	/** Высота шага строки для её стиля, пиксели. */
	public interface LineAdvance {
		float advanceOf(TextLineSpec spec);
	}

	/** Размещённая строка с абсолютными координатами якоря глифов (нижний левый угол). */
	public static final class PlacedLine {
		public final String text;
		public final IColor color;
		public final IFont lineFont;
		public final float xLeftPx;
		public final float yBottomPx;

		PlacedLine(String text, IColor color, IFont lineFont, float xLeftPx, float yBottomPx) {
			this.text = text;
			this.color = color;
			this.lineFont = lineFont;
			this.xLeftPx = xLeftPx;
			this.yBottomPx = yBottomPx;
		}
	}

	private TextLayout() {
	}

	/**
	 * Раскладывает строки блока: {@code absX/absY} — верхняя левая точка элемента в экранных
	 * координатах, {@code width}/{@code height} — его размер, {@code pad} — отступ контента,
	 * {@code align} — выравнивание по горизонтали. Строки читаются сверху вниз; перенос
	 * включается флагом {@code wordWrap}.
	 */
	public static List<PlacedLine> layout(float absX, float absY, int width, int height,
			float pad, EnumAlignment align, boolean wordWrap,
			List<TextLineSpec> specs, Measure measure, LineAdvance advance) {

		final float contentW = Math.max(0f, (float) width - 2f * pad);
		final EnumAlignment effectiveAlign = (align == null) ? EnumAlignment.LEFT : align;

		List<Object[]> rows = new ArrayList<>(); // [размещаемый текст, исходная spec]
		for (TextLineSpec spec : specs) {
			String text = spec.getText();
			if (!wordWrap || text.isEmpty()) {
				rows.add(new Object[] { text, spec });
				continue;
			}
			addWrappedRows(rows, text, contentW, spec, measure);
		}

		List<PlacedLine> placed = new ArrayList<>(rows.size());
		float rowTop = absY + pad;
		for (Object[] row : rows) {
			TextLineSpec spec = (TextLineSpec) row[1];
			float adv = advanceOfSafe(spec, advance);
			float w = measure.widthOf(spec, (String) row[0]);
			float xLeft;
			switch (effectiveAlign) {
				case RIGHT:
					xLeft = absX + width - pad - w;
					break;
				case CENTER:
					xLeft = absX + Math.max(pad, (width - w) / 2f);
					break;
				default:
					xLeft = absX + pad;
					break;
			}
			placed.add(new PlacedLine((String) row[0], spec.getColor(), spec.getLineFont(), xLeft, rowTop + adv));
			rowTop += adv;
		}
		return placed;
	}

	private static float advanceOfSafe(TextLineSpec spec, LineAdvance advance) {
		if (advance == null) {
			return 16f;
		}
		float v = advance.advanceOf(spec);
		return (v <= 0f) ? 16f : v;
	}

	/** Жадный перенос по словам; слово длиннее доступной ширины режется посимвольно. */
	static void addWrappedRows(List<Object[]> out, String text, float maxWidth, TextLineSpec spec, Measure measure) {
		String[] tokens = splitTokens(text);
		StringBuilder current = new StringBuilder();
		for (String token : tokens) {
			boolean fitsWithCurrent = current.length() > 0 && measure.widthOf(current.toString())
					+ spaceWidth(measure) + measure.widthOf(token) <= maxWidth;
			if (!fitsWithCurrent && current.length() > 0) {
				emitSegment(out, current.toString(), maxWidth, spec, measure);
				current.setLength(0);
			}
			appendToken(current, token);
		}
		if (current.length() > 0) {
			emitSegment(out, current.toString(), maxWidth, spec, measure);
		}
	}

	private static void emitSegment(List<Object[]> out, String segment, float maxWidth, TextLineSpec spec, Measure measure) {
		while (measure.widthOf(spec, segment) > maxWidth && !segment.isEmpty()) {
			int cut = findCharCut(segment, maxWidth, spec, measure);
			out.add(new Object[] { segment.substring(0, cut), spec });
			segment = segment.substring(cut).replaceFirst("^ ", "");
		}
		out.add(new Object[] { segment, spec });
	}

	/** Наибольший посимвольный срез не шире {@code maxWidth}. */
	static int findCharCut(String text, float maxWidth, TextLineSpec spec, Measure measure) {
		for (int i = 1; i <= text.length(); i++) {
			if (measure.widthOf(spec, text.substring(0, i)) > maxWidth) {
				return Math.max(1, i - 1);
			}
		}
		return text.length();
	}

	private static void appendToken(StringBuilder sb, String token) {
		if (sb.length() > 0) {
			sb.append(' ');
		}
		sb.append(token);
	}

	private static float spaceWidth(Measure m) {
		float w = m.widthOf(" ");
		return (w <= 0f) ? 4f : w;
	}

	private static String[] splitTokens(String text) {
		List<String> tokens = new ArrayList<>();
		int start = -1;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == ' ') {
				if (start >= 0) {
					tokens.add(text.substring(start, i));
					start = -1;
				}
			} else if (start < 0) {
				start = i;
			}
		}
		if (start >= 0) {
			tokens.add(text.substring(start));
		}
		return tokens.toArray(new String[0]);
	}

}
