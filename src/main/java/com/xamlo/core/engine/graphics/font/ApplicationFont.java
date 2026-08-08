package com.xamlo.core.engine.graphics.font;

import com.xamlo.core.engine.graphics.api.gui.font.IFont;

public class ApplicationFont implements IFont {

	private final String fontFamily;
	private final int fontSize;
	private final boolean bold;
	private final boolean italic;
	
	public ApplicationFont(final String fontFamily, final int fontSize, final boolean bold, final boolean italic) {
		this.fontFamily = fontFamily;
		this.fontSize = fontSize;
		this.bold = bold;
		this.italic = italic;
	}
	
	@Override
	public String getFontFamily() {
		// TODO Auto-generated method stub
		return fontFamily;
	}

	@Override
	public int getFontSize() {
		// TODO Auto-generated method stub
		return fontSize;
	}

	@Override
	public boolean isBold() {
		// TODO Auto-generated method stub
		return bold;
	}

	@Override
	public boolean isItalic() {
		// TODO Auto-generated method stub
		return italic;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ApplicationFont fontKey = (ApplicationFont) o;
        
        if (fontSize != fontKey.fontSize) return false;
        if (bold != fontKey.bold) return false;
        if (italic != fontKey.italic) return false;
        return fontFamily != null ? fontFamily.equals(fontKey.fontFamily) : fontKey.fontFamily == null;
    }
    
    @Override
    public int hashCode() {
        int result = fontFamily != null ? fontFamily.hashCode() : 0;
        result = 31 * result + fontSize;
        result = 31 * result + (bold ? 1 : 0);
        result = 31 * result + (italic ? 1 : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return fontFamily + "-" + fontSize + (bold ? "-Bold" : "") + (italic ? "-Italic" : "");
    }
    
}
