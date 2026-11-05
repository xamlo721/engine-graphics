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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.xamlo.core.engine.graphics.font.CharacterData;
import com.xamlo.core.engine.graphics.font.FontTexture;
import com.xamlo.core.engine.graphics.font.GlyphPage;

public class GlyphGenerator {

    private static final int MAX_PAGE_SIZE = 1 << 14;

    private static final class GlyphPlacement {
        private final int codeUnit;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private GlyphPlacement(int codeUnit, int x, int y, int width, int height) {
            this.codeUnit = codeUnit;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static final class PageLayout {
        private final List<GlyphPlacement> placements;
        private final int maxHeight;

        private PageLayout(List<GlyphPlacement> placements, int maxHeight) {
            this.placements = placements;
            this.maxHeight = maxHeight;
        }
    }

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
    	
        int pageSize = Math.max(1, IMG_SIZE);
        Graphics2D metricsGraphics = setupGraphics(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), font, pageSize, antiAlias);
        if (id > 0) {
            metricsGraphics.setFont(new Font("DEFAULT", font.getStyle(), font.getSize()));
        }
        FontMetrics fontMetrics = metricsGraphics.getFontMetrics();

        PageLayout layout;
        while (true) {
            layout = layoutGlyphs(metricsGraphics, fontMetrics, id, pageSize, MARGIN, spacing);
            if (layout != null) {
                break;
            }
            if (pageSize >= MAX_PAGE_SIZE) {
                metricsGraphics.dispose();
                throw new IllegalStateException("Glyph page " + id + " does not fit within " + pageSize + "px");
            }
            pageSize <<= 1;
        }
        metricsGraphics.dispose();

        BufferedImage bufferedImage = new BufferedImage(pageSize, pageSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = setupGraphics(bufferedImage, font, pageSize, antiAlias);
        if (id > 0) {
            graphics2D.setFont(new Font("DEFAULT", font.getStyle(), font.getSize()));
        }

        Map<Integer, CharacterData> charData = new HashMap<>();
        for (GlyphPlacement placement : layout.placements) {
            String character = String.valueOf((char) placement.codeUnit);
            graphics2D.drawString(character, placement.x, placement.y + fontMetrics.getAscent());
            charData.put(placement.codeUnit,
                    new CharacterData(placement.x, placement.y, placement.width, placement.height));
        }
        
        saveBufferedImage(bufferedImage, "font_atlas_" + id + ".png");
        
        FontTexture texture = new FontTexture(bufferedImage);
        GlyphPage glyph = new GlyphPage(texture, layout.maxHeight, charData);
        graphics2D.dispose();
        return glyph;
    }

    private static PageLayout layoutGlyphs(Graphics2D graphics2D, FontMetrics fontMetrics, int id,
            int pageSize, int MARGIN, int spacing) {
        List<GlyphPlacement> placements = new ArrayList<>(256);
        int x = MARGIN;
        int y = MARGIN;
        int maxHeight = 0;

        for (int offset = 0; offset < 256; offset++) {
            int codeUnit = id * 256 + offset;
            String character = String.valueOf((char) codeUnit);
            Rectangle2D dimensions = fontMetrics.getStringBounds(character, graphics2D);
            int width = (int) dimensions.getWidth() + spacing;
            int height = (int) dimensions.getHeight();

            if (MARGIN + width + MARGIN > pageSize) {
                return null;
            }

            if (x + width > pageSize) {
                x = MARGIN;
                y += maxHeight + MARGIN;
                maxHeight = 0;
            }

            if (y + height > pageSize) {
                return null;
            }

            placements.add(new GlyphPlacement(codeUnit, x, y, width, height));
            maxHeight = Math.max(maxHeight, height);
            x += width + MARGIN;
        }

        return new PageLayout(placements, maxHeight);
    }
    
    public static void saveBufferedImage(BufferedImage image, String filename) {
        try {
            File outputFile = new File(filename);
            ImageIO.write(image, "PNG", outputFile);
        } catch (IOException e) {
            System.err.println("Couldnt save texture atlas: " + e.getMessage());
        }
    }
    
}
