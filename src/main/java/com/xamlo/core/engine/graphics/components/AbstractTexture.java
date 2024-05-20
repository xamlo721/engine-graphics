package com.xamlo.core.engine.graphics.components;

import com.xamlo.engine.api.resources.ITextureResource;

public abstract class AbstractTexture implements ITextureResource<String> {

	protected final String identifier;
	// Идентификатор текстуры OpenGL
	protected int glTextureID = -1;
	// Ширина изображения
	protected final int width;
	// Высота изображения
	protected final int height;

	public abstract void bind();
	
	public abstract void unbind();
	
	public AbstractTexture(String identifier, int width, int height) {
		this.identifier = identifier;
		this.width = width;
		this.height = height;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int getTextureSize() {
		return width * height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

    public int getTextureID() {
        return glTextureID;
    }
}
