package com.xamlo.core.engine.graphics.components.attribs;

import com.xamlo.core.engine.graphics.primitives.EnumDimestionSize;

public class NormalAttribute extends AbstractAttribute {

	private final static EnumDimestionSize attribSize = EnumDimestionSize._3D;	
	private final static EnumAttributeType attribType = EnumAttributeType.Float;

	public NormalAttribute() {
		super(attribSize, attribType);
	}
	
	@Override
	public boolean isNormalized() {
		return false;
	}

}
