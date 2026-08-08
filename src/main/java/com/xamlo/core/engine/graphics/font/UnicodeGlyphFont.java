package com.xamlo.core.engine.graphics.font;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;


import com.xamlo.core.engine.graphics.fontsystem.GlyphGenerator;
import com.xamlo.engine.api.resources.IFontResource;

public class UnicodeGlyphFont implements IFontResource<String> {

    private final Font font;
    private final boolean antiAlias;
    private final Map<Integer, GlyphPage> GLYPH_REGISTRY = new HashMap<>();
    private static final int IMG_SIZE = 512;
    private final int MARGIN;
    private final int spacing;
    private String identifier;

    /**
     * Construct a glyph font with specified font
     * Defaults to use antialiasing, a spacing of 0 and defaults to initialise a cache with ASCII characters
     *
     * @param font the font to use
     */
    public UnicodeGlyphFont(final String identifier, Font font) {
        this(identifier, font, true);
    }

    /**
     * Construct a glyph font with specified font and specified anti-aliasing preferences
     * Defaults to initialise a cache with ASCII characters and a spacing of 0
     *
     * @param font      the font to use
     * @param antiAlias the anti-aliasing preference
     */
    public UnicodeGlyphFont(final String identifier, Font font, boolean antiAlias) {
        this(identifier, font, antiAlias, 0, 0);
    }

    /**
     * Construct a glyph font with specified font, specified anti-aliasing preferences and specified
     * initial glyph pages to cache
     *
     * @param font         the font to use
     * @param antiAlias    the anti-aliasing preference
     * @param initialCache the initial glyph pages to cache
     */
    public UnicodeGlyphFont(final String identifier, Font font, boolean antiAlias, int spacing, int... initialCache) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.spacing = spacing;
        this.MARGIN = (int) (font.getSize() / 5f);
        this.identifier = identifier;

        for (int id : initialCache) {
        	GlyphPage glyph = GlyphGenerator.generateGlyph(id, font, IMG_SIZE, MARGIN, spacing, antiAlias);
            GLYPH_REGISTRY.put(id, glyph);
        }
        
    }

	@Override
	public String getIdentifier() {
		return identifier;
	}


    /**
     * Returns the width of the specified string if drawn to the screen
     *
     * @param text the text
     * @return the width of the specified text
     */
    public float getStringWidth(String text) {
        return (float) text.chars().mapToDouble(c -> getCharacterWidth((char) c)).sum();
    }

    /**
     * Returns the width of the specified character if drawn to the screen
     *
     * @param c the character
     * @return the width of the specified character
     */
    public float getCharacterWidth(char c) {
        return (float) getGlyphPage(c).getCharacterData(c).getWidth();
    }

    /**
     * Returns the height of the specified string if drawn to the screen
     *
     * @param text the text
     * @return the height of the specified text
     */
    public float getStringHeight(String text) {
        return (float) text.chars().mapToDouble(c -> getCharacterHeight((char) c)).max().orElse(0);
    }

    /**
     * Returns the height of the specified character if drawn to the screen
     *
     * @param c the character
     * @return the height of the specified character
     */
    public float getCharacterHeight(char c) {
        return (float) getGlyphPage(c).getCharacterData(c).getHeight();
    }

    /**
     * Returns the max-height of the standard ascii characters if drawn to the screen
     *
     * @return the max-height of standard ascii characters
     */
    public float getMaxHeight() {
        return getMaxHeight('a');
    }
    
    public float getImageSize() {
        return IMG_SIZE;
    }

    /**
     * Returns the max-height of the specified character's glyph page if drawn to the screen
     *
     * @param c the character
     * @return the max-height of the specified character's glyph page
     */
    public float getMaxHeight(char c) {
        return (float) getGlyphPage(c).getMaxHeight();
    }


    private int getGlyphID(char c) {
        return c >> 8 & 0xFF;
    }

    public GlyphPage getGlyphPage(char c) {
    	
        int glyphID = getGlyphID(c);
        
        if (!GLYPH_REGISTRY.containsKey(glyphID)) {
        	GlyphPage glyph = GlyphGenerator.generateGlyph(glyphID, font, IMG_SIZE, MARGIN, spacing, antiAlias);
            GLYPH_REGISTRY.put(glyphID, glyph);
        }
        
        return GLYPH_REGISTRY.get(glyphID);
    }



}