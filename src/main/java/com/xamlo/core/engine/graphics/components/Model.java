package com.xamlo.core.engine.graphics.components;

import com.xamlo.engine.api.resources.IVertex;
import com.xamlo.engine.api.resources.IVertexStructure;

public class Model extends AbstractModel {

	
	public Model(String identifier, IVertexStructure structure, IVertex[] vertices, int[] indices, int polygonCount) {
		super(identifier);
		this.vertexScruct = structure;
		this.vertices = vertices;
		this.indices = indices;
		this.polygonCount = polygonCount;
		
	}

	
	@Override
	public String toString() {
		return "[Model: "+ identifier + "]"
				+ "[structure: " + vertexScruct.toString() + "]"
				+ "[vertices: " + this.vertices.length + "]"
				+ "[indices: " + this.indices.length + "]"
				+ "[polygonCount: " + polygonCount + "]" ;
	}


}
