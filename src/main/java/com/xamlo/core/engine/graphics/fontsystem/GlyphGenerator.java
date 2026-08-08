package com.xamlo.core.engine.graphics.fontsystem;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.xamlo.core.engine.graphics.font.CharacterData;
import com.xamlo.core.engine.graphics.font.FontTexture;
import com.xamlo.core.engine.graphics.font.GlyphPage;

public class GlyphGenerator {

    
    private static Graphics2D setupGraphics(BufferedImage bufferedImage, Font font, int IMG_SIZE, boolean antiAlias) {
    	
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

    public static GlyphPage generateGlyph(int id, Font font, int IMG_SIZE, int MARGIN, int spacing,  boolean antiAlias) {
    	
        Map<Integer, CharacterData> charData = new HashMap<>();
        BufferedImage bufferedImage = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = setupGraphics(bufferedImage, font, IMG_SIZE, antiAlias);
        
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
            
            x += width + MARGIN;
        }
        
        saveBufferedImage(bufferedImage, "font_atlas_" + id + ".png");
        
        FontTexture texture = new FontTexture(bufferedImage);
        GlyphPage glyph = new GlyphPage(texture, maxHeight, charData);
        graphics2D.dispose();
        return glyph;
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
    
}
