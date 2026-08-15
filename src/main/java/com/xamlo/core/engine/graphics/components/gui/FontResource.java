package com.xamlo.core.engine.graphics.components.gui;

import java.awt.Font;

import com.xamlo.core.engine.graphics.font.UnicodeGlyphFont;
import com.xamlo.engine.api.font.IFontAtlas;
import com.xamlo.engine.api.font.IGlyphPage;
import com.xamlo.engine.api.resources.IFontResource;

public class FontResource implements IFontResource<String> {
	

	private final String resourceID;
	private final UnicodeGlyphFont glyphFont;
	
    public FontResource(String resourceID, Font baseFont) {
        this.resourceID = resourceID;
        this.glyphFont = new UnicodeGlyphFont(resourceID, baseFont, true);
    }

	@Override
	public String getIdentifier() {
		return this.resourceID;
	}

	@Override
	public IFontAtlas getFontAtlas() {
		return glyphFont.getFontAtlas();
	}

	@Override
	public IGlyphPage getGlyphPage(char c) {
		return glyphFont.getGlyphPage(c);
	}

	@Override
	public float getMaxHeight(char c) {
		return glyphFont.getMaxHeight(c);
	}

	@Override
	public float getImageSize() {
		return glyphFont.getImageSize();
	}

	@Override
	public float getCharacterHeight(char c) {
		return glyphFont.getCharacterHeight(c);
	}

	@Override
	public float getStringHeight(String text) {
		return glyphFont.getStringHeight(text);
	}

	@Override
	public float getCharacterWidth(char c) {
		return glyphFont.getCharacterWidth(c);
	}

	@Override
	public float getStringWidth(String text) {
		return glyphFont.getStringWidth(text);
	}
	
}
