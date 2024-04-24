package com.xamlo.core.engine.graphics.components.attribs;

import com.xamlo.core.engine.graphics.primitives.EnumDimestionSize;

public class TexCoordAttribute extends AbstractAttribute {

	private final static EnumDimestionSize attribSize = EnumDimestionSize._2D;	
	private final static EnumAttributeType attribType = EnumAttributeType.Float;

	public TexCoordAttribute() {
		super(attribSize, attribType);
	}
	
	@Override
	public boolean isNormalized() {
		return false;
	}

}
