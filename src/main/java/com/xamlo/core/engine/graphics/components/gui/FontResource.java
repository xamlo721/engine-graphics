package com.xamlo.core.engine.graphics.components.gui;

import java.awt.Font;

import com.xamlo.engine.api.resources.IFontResource;

public class FontResource implements IFontResource<String> {
	

	private final String resourceID;
	private final Font font;
	
    public FontResource(String resourceID, Font baseFont) {
        this.resourceID = resourceID;
        this.font = baseFont;
    }

	@Override
	public String getIdentifier() {
		return this.resourceID;
	}
	
	
    
}
