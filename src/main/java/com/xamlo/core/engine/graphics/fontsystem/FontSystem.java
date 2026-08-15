package com.xamlo.core.engine.graphics.fontsystem;

import com.xamlo.core.engine.graphics.font.ApplicationFont;
import com.xamlo.core.engine.graphics.font.UnicodeGlyphFont;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

public class FontSystem {
	
    private static FontSystem instance;
    private final Map<ApplicationFont, UnicodeGlyphFont> fontCache = new HashMap<>();
    
    private FontSystem() {
        // Private constructor for singleton
    }
    
    public static FontSystem getInstance() {
        if (instance == null) {
            instance = new FontSystem();
        }
        return instance;
    }
    
    public UnicodeGlyphFont getFont(ApplicationFont key) {
        return fontCache.get(key);
    }
    
    public UnicodeGlyphFont registerFont(ApplicationFont key, Font awtFont) {
        String identifier = generateFontIdentifier(key);
        UnicodeGlyphFont glyphFont = new UnicodeGlyphFont(identifier, awtFont, true);
        fontCache.put(key, glyphFont);
        return glyphFont;
    }
    
    public boolean isFontRegistered(ApplicationFont key) {
        return fontCache.containsKey(key);
    }
    
    public void unregisterFont(ApplicationFont key) {
        UnicodeGlyphFont font = fontCache.remove(key);
        if (font != null) {
            // Add any cleanup logic here if needed
        }
    }
    
    public void releaseAllFonts() {
        fontCache.clear();
        // Add any additional cleanup logic here
    }
    
    private String generateFontIdentifier(ApplicationFont key) {
        return key.toString();
    }
    
    public UnicodeGlyphFont attach(ApplicationFont key, UnicodeGlyphFont glyphFont) {
        if (key == null || glyphFont == null) {
            return null;
        }
        fontCache.put(key, glyphFont);
        return glyphFont;
    }

    // Helper methods for common font registration
    public UnicodeGlyphFont registerFont(String fontFamily, int fontSize, boolean bold, boolean italic, Font awtFont) {
    	ApplicationFont key = new ApplicationFont(fontFamily, fontSize, bold, italic);
        return registerFont(key, awtFont);
    }
}