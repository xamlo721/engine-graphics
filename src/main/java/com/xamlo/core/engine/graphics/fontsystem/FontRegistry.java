package com.xamlo.core.engine.graphics.fontsystem;

import java.awt.Font;
import java.util.HashSet;
import java.util.Set;

import com.xamlo.core.engine.graphics.font.ApplicationFont;
import com.xamlo.core.engine.graphics.font.UnicodeGlyphFont;

public class FontRegistry {
    private static Set<String> registeredFonts = new HashSet<>();
    
    public static boolean isRegistered(String fontIdentifier) {
        return registeredFonts.contains(fontIdentifier);
    }
    
    public static void registerFont(String fontIdentifier) {
        registeredFonts.add(fontIdentifier);
    }
    
    public static void unregisterFont(String fontIdentifier) {
        registeredFonts.remove(fontIdentifier);
    }
    
    public static void releaseFonts() {
        registeredFonts.clear();
        FontSystem.getInstance().releaseAllFonts();
    }
    
    // Helper method to register font with FontSystem
    public static UnicodeGlyphFont registerFontWithSystem(ApplicationFont key, Font awtFont) {
        registerFont(key.toString());
        return FontSystem.getInstance().registerFont(key, awtFont);
    }
}