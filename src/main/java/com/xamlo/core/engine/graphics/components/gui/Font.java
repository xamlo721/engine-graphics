package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.IFont;

public class Font implements IFont {
	
    private String fontFamily;
    private int fontSize;
    private boolean bold;
    private boolean italic;

    public Font(String fontFamily, int fontSize, boolean bold, boolean italic) {
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
        this.bold = bold;
        this.italic = italic;
    }

    @Override
    public String getFontFamily() {
        return fontFamily;
    }

    @Override
    public int getFontSize() {
        return fontSize;
    }

    @Override
    public boolean isBold() {
        return bold;
    }

    @Override
    public boolean isItalic() {
        return italic;
    }
    
}