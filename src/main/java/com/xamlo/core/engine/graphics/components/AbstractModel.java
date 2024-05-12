package com.xamlo.core.engine.graphics.components;

import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.engine.api.resources.IModelResource;

public abstract class AbstractModel extends AbstractSceneElement implements IModelResource<String> {

	protected final String identifier;

	protected AbstractModel(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getIdentifier() {
		return this.identifier;
	}

	@Override
	public int getPolygonCount() {
		throw new UnsupportedOperationException("No implemented yet");
	}

	@Override
	public void setPolygonCount(int count) {
		throw new UnsupportedOperationException("No implemented yet");
	}

}
