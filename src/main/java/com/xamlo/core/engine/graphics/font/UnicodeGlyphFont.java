package com.xamlo.core.engine.graphics.font;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class UnicodeGlyphFont {

    private final Font font;
    private final boolean antiAlias;
    private final Map<Integer, GlyphPage> GLYPH_REGISTRY = new HashMap<>();
    private static final int IMG_SIZE = 512;
    private final int MARGIN;
    private final int spacing;

    /**
     * Construct a glyph font with specified font
     * Defaults to use antialiasing, a spacing of 0 and defaults to initialise a cache with ASCII characters
     *
     * @param font the font to use
     */
    public UnicodeGlyphFont(Font font) {
        this(font, true);
    }

    /**
     * Construct a glyph font with specified font and specified anti-aliasing preferences
     * Defaults to initialise a cache with ASCII characters and a spacing of 0
     *
     * @param font      the font to use
     * @param antiAlias the anti-aliasing preference
     */
    public UnicodeGlyphFont(Font font, boolean antiAlias) {
        this(font, antiAlias, 0, 0);
    }

    /**
     * Construct a glyph font with specified font, specified anti-aliasing preferences and specified
     * initial glyph pages to cache
     *
     * @param font         the font to use
     * @param antiAlias    the anti-aliasing preference
     * @param initialCache the initial glyph pages to cache
     */
    public UnicodeGlyphFont(Font font, boolean antiAlias, int spacing, int... initialCache) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.spacing = spacing;
        this.MARGIN = (int) (font.getSize() / 5f);

        for (int id : initialCache)
            setupGlyph(id);
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

    private void setupGlyph(int id) {
    	
        Map<Integer, CharacterData> charData = new HashMap<>();
        BufferedImage bufferedImage = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = setupGraphics(bufferedImage);
        
        if (id > 0) 
        	graphics2D.setFont(new Font("DEFAULT", font.getStyle(), font.getSize()));
        
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        int x = MARGIN;
        int y = MARGIN;
        int maxHeight = 0;
        
        for (int i = id * 256; i < (id + 1) * 256; i++) {
            String character = String.valueOf((char) i);
            Rectangle2D dimensions = fontMetrics.getStringBounds(character, graphics2D);
            int width = (int) dimensions.getWidth() + spacing;
            int height = (int)  dimensions.getHeight();
            
            if (x + width > IMG_SIZE) {
                x = MARGIN;
                y += maxHeight + MARGIN;
                maxHeight = 0;
            }
            
            if (height > maxHeight)
                maxHeight = height;
            
            graphics2D.drawString(character, x, y + fontMetrics.getAscent());

            charData.put(i, new CharacterData(x, y, width, height));
            
            System.out.println("saving char: " + character + ", size: " +  charData.get(i).getWidth() + "|" + charData.get(i).getHeight());

            x += width + MARGIN;
        }
        
        saveBufferedImage(bufferedImage, "font_atlas_" + id + ".png");
        
        FontTexture texture = new FontTexture(bufferedImage);
        texture.bind();
        GLYPH_REGISTRY.put(id, new GlyphPage(texture, maxHeight, charData));
        graphics2D.dispose();
    }

    private int getGlyphID(char c) {
        return c >> 8 & 0xFF;
    }

    public GlyphPage getGlyphPage(char c) {
        int glyphID = getGlyphID(c);
        if (!GLYPH_REGISTRY.containsKey(glyphID))
            setupGlyph(glyphID);
        return GLYPH_REGISTRY.get(glyphID);
    }
    
    public static void saveBufferedImage(BufferedImage image, String filename) {
        try {
            File outputFile = new File(filename);
            ImageIO.write(image, "PNG", outputFile);
            System.out.println("Texture atlas saved as: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Couldnt save texture atlas: " + e.getMessage());
        }
    }
    
    private Graphics2D setupGraphics(BufferedImage bufferedImage) {
    	
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setColor(new Color(255, 255, 255, 0));
        graphics2D.fill(new Rectangle(0, 0, IMG_SIZE, IMG_SIZE));
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(font);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        return graphics2D;
    }

}