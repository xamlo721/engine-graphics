package com.xamlo.core.engine.graphics.fontsystem;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.font.GlyphPage;

class GlyphGeneratorTest {

    private static final Path DEFAULT_DUMP = Path.of("font_atlas_200.png");
    private static final Path ENABLED_DUMP = Path.of("font_atlas_201.png");

    @AfterEach
    void cleanup() throws IOException {
        System.clearProperty("engine.fontDump");
        Files.deleteIfExists(DEFAULT_DUMP);
        Files.deleteIfExists(ENABLED_DUMP);
    }

    @Test
    void doesNotWriteAtlasWithoutDebugFlag() throws IOException {
        Files.deleteIfExists(DEFAULT_DUMP);
        System.clearProperty("engine.fontDump");

        GlyphPage page = generate(200);

        assertNotNull(page);
        assertFalse(Files.exists(DEFAULT_DUMP));
    }

    @Test
    void writesAtlasWhenDebugFlagIsEnabled() throws IOException {
        Files.deleteIfExists(ENABLED_DUMP);
        System.setProperty("engine.fontDump", "true");

        GlyphPage page = generate(201);

        assertNotNull(page);
        assertTrue(Files.exists(ENABLED_DUMP));
    }

    private static GlyphPage generate(int pageId) {
        return GlyphGenerator.generateGlyph(pageId, new Font(Font.SANS_SERIF, Font.PLAIN, 12),
                512, 4, 0, true);
    }
}
