package com.xamlo.core.engine.graphics.font;

import java.util.Map;

import com.xamlo.engine.api.font.IGlyphPage;
import com.xamlo.engine.api.resources.ITextureResource;


public class GlyphPage implements IGlyphPage {

    private final ITextureResource<String> texture;
    private final int maxHeight;
    private final Map<Integer, CharacterData> characterRegistry;

    public GlyphPage(FontTexture texture, int maxHeight, Map<Integer, CharacterData> characterRegistry) {
        this.texture = texture;
        this.maxHeight = maxHeight;
        this.characterRegistry = characterRegistry;
    }

    @Override
	public ITextureResource<String> getTexture() {
        return texture;
    }

    @Override
	public int getMaxHeight() {
        return maxHeight;
    }

    @Override
	public CharacterData getCharacterData(char c) {
        return characterRegistry.get((int) c);
    }
    
}