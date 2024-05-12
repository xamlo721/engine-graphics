package com.xamlo.core.engine.graphics.api.components.scene;

import com.xamlo.core.engine.graphics.components.ShaderProgram;


public interface IShederable {

	
	public void setShader(ShaderProgram currentShader);
	
	
	public ShaderProgram getShader();
	
	
}
