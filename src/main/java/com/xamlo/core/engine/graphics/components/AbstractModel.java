package com.xamlo.core.engine.graphics.components;

import com.xamlo.engine.api.resources.IModelResource;
import com.xamlo.engine.api.resources.IVertex;
import com.xamlo.engine.api.resources.IVertexStructure;

public abstract class AbstractModel implements IModelResource<String> {

	protected final String identifier;
	protected IVertex[] vertices;
	protected int[] indices;
	protected int polygonCount;
	protected IVertexStructure vertexScruct;
	
	protected AbstractModel(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getIdentifier() {
		return this.identifier;
	}
	
	@Override
	public int getPolygonCount() {
		return polygonCount;
	}

	@Override
	public IVertex[] getVertices() {
		return vertices;
	}

	@Override
	public int[] getIndices() {
		return indices;
	}

	@Override
	public IVertexStructure getStructure() {
		return this.vertexScruct;
	}

}
