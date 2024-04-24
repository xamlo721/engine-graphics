package com.xamlo.core.engine.graphics.primitives;

import java.util.ArrayList;
import java.util.List;

import com.xamlo.core.engine.graphics.api.primitives.IVertexAttribute;
import com.xamlo.core.engine.graphics.api.primitives.IVertexStructure;

public class VertexStructure implements IVertexStructure {

	private List<IVertexAttribute> attributes;
	private int vertexCount;
	
	public VertexStructure() {
		this.attributes = new ArrayList<IVertexAttribute>();
	}

    public VertexStructure(List<IVertexAttribute> attributes) {
        this.attributes = attributes;
    }

	@Override
    public VertexStructure addAttribute(IVertexAttribute attrib) {
    	this.attributes.add(attrib);
		return this;
    }
	@Override
	public void setVertexCount(int count) {
		this.vertexCount = count;
	}
	
	@Override
	public int getVertexCount() {
		return vertexCount;
	}


	@Override
    public int getVertexStride() {
    	int stride = 0;
    	for (IVertexAttribute iVertexAttribute : attributes) {
			stride += iVertexAttribute.getOffset();
		}
		return stride;
    }
    
	@Override
    public int getVertexSize() {
    	int stride = 0;
    	for (IVertexAttribute attr : attributes) {
			stride += attr.getDimensionSize().value();
		}
		return stride;
    }
    
	@Override
    public List<IVertexAttribute> getAttributes() {
        return attributes;
    }

	@Override
	public void clear() {
		this.attributes.clear();
	}


    
}
