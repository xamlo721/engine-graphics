package com.xamlo.core.engine.graphics.fontsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.font.CharacterData;
import com.xamlo.core.engine.graphics.font.GlyphPage;

class GlyphGeneratorOverflowTest {

    private static final int PAGE_ID = 210;
    private static final Path PAGE_DUMP = Path.of("font_atlas_" + PAGE_ID + ".png");

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(PAGE_DUMP);
    }

    @Test
    void growsPageBeforeGlyphDataExceedsTextureBounds() throws IOException {
        Files.deleteIfExists(PAGE_DUMP);

        GlyphPage page = generate(64, 32);

        int width = page.getTexture().getWidth();
        int height = page.getTexture().getHeight();
        assertTrue(width > 64, "page must grow instead of keeping overflowing glyph coordinates");
        assertEquals(width, height);

        for (int offset = 0; offset < 256; offset++) {
            int codeUnit = PAGE_ID * 256 + offset;
            CharacterData data = page.getCharacterData((char) codeUnit);
            assertNotNull(data, "missing glyph data for code unit " + codeUnit);
            assertTrue(data.getX() >= 0, "negative x for code unit " + codeUnit);
            assertTrue(data.getY() >= 0, "negative y for code unit " + codeUnit);
            assertTrue(data.getX() + data.getWidth() <= width, "glyph exceeds page width for code unit " + codeUnit);
            assertTrue(data.getY() + data.getHeight() <= height, "glyph exceeds page height for code unit " + codeUnit);
        }
    }

    @Test
    void keepsRequestedPageSizeWhenGlyphsFit() throws IOException {
        Files.deleteIfExists(PAGE_DUMP);

        GlyphPage page = generate(512, 12);

        assertEquals(512, page.getTexture().getWidth());
        assertEquals(512, page.getTexture().getHeight());
    }

    private static GlyphPage generate(int pageSize, int fontSize) {
        return GlyphGenerator.generateGlyph(PAGE_ID, new Font(Font.SANS_SERIF, Font.PLAIN, fontSize),
                pageSize, 4, 0, true);
    }
}
