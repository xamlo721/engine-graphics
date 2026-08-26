package com.xamlo.core.engine.graphics.components;

import java.util.function.Function;

/** Регистрирует шрифтовой бэкенд для тестов UI без полного старта движка (JUnit5 extension). */
public final class FontBackendForTests implements org.junit.jupiter.api.extension.BeforeAllCallback {
    public static void register() {
        Function<com.xamlo.core.engine.graphics.font.ApplicationFont, com.xamlo.core.engine.graphics.font.UnicodeGlyphFont> p =
                k -> com.xamlo.core.engine.graphics.fontsystem.FontSystem.getInstance().ensureFont(k);
        com.xamlo.core.engine.graphics.font.UnicodeGlyphFont.setFaceProvider(p);
        com.xamlo.core.engine.graphics.font.UnicodeGlyphFont.setAtlasFactory(
                f -> new com.xamlo.core.engine.graphics.font.FontAtlas(f));
        com.xamlo.core.engine.graphics.font.UnicodeGlyphFont.setRasterizer(
                (c, f, s, m, sp, aa) -> com.xamlo.core.engine.graphics.fontsystem.GlyphGenerator
                        .generateGlyph(c, f, s, m, sp, aa));
    }

    @Override
    public void beforeAll(org.junit.jupiter.api.extension.ExtensionContext context) {
        register();
    }
}
