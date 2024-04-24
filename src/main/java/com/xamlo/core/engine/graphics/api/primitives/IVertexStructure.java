package com.xamlo.core.engine.graphics.api.primitives;

import java.util.List;

import com.xamlo.core.engine.graphics.primitives.VertexStructure;

public interface IVertexStructure {

	public VertexStructure addAttribute(IVertexAttribute attrib);
	
	public void setVertexCount(int count);
	
	public int getVertexStride();
	
	public int getVertexSize();
	
	public int getVertexCount();
	
	public List<IVertexAttribute> getAttributes();
	
	public void clear();
	
}
