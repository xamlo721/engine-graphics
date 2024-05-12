package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.components.AbstractRenderableObject;
import com.xamlo.core.engine.graphics.components.ShaderProgram;
import com.xamlo.engine.api.resources.IShaderResource;
import com.xamlo.engine.resources.ResourceLoader;

public abstract class AbstractSceneElement extends AbstractRenderableObject {

	// Shaders
	
	private final IShaderResource<String> vertexShaderSource;
	
	private final IShaderResource<String> fragmentShaderSource;
	
	
	
	public AbstractSceneElement() {
		super();
		this.vertexShaderSource = ResourceLoader.INSTANCE().loadShader("shader.primitive.colored.vertex");
		this.fragmentShaderSource = ResourceLoader.INSTANCE().loadShader("shader.primitive.colored.fragment");
	}
	
	@Override
	public void init() {


		objectShader = new ShaderProgram();
		objectShader.addVertexShader(vertexShaderSource.getShaderProgram());
		objectShader.addFragmentShader(fragmentShaderSource.getShaderProgram());
		objectShader.compileShader();
		objectShader.bind();
		try {
			objectShader.createUniform("projectionMatrix");
			objectShader.createUniform("objectMatrix");
			objectShader.createUniform("positionMatrix");
			objectShader.createUniform("scaleMatrix");
			objectShader.createUniform("rotationMatrix");
			objectShader.createUniform("texture_sampler");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		objectShader.unbind();
	}

	@Override
	public void release() {
		this.objectShader.cleanup();
		
	}

}
