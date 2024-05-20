package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.components.AbstractRenderableObject;
import com.xamlo.core.engine.graphics.components.GraphicalMesh;
import com.xamlo.core.engine.graphics.components.ShaderProgram;
import com.xamlo.engine.api.resources.IModelResource;
import com.xamlo.engine.api.resources.IShaderResource;
import com.xamlo.engine.resources.ResourceLoader;

public abstract class AbstractUIElement extends AbstractRenderableObject {

	// Shaders
	private final IShaderResource<String> vertexShaderSource;
	
	private final IShaderResource<String> fragmentShaderSource;
	
	private static IModelResource<String> model;
    private static GraphicalMesh defaultWWidgetMesh;
	
	
	public AbstractUIElement() {
		super();
		this.vertexShaderSource = ResourceLoader.INSTANCE().loadShader("shader.default.gui.textured.vertex");
		this.fragmentShaderSource = ResourceLoader.INSTANCE().loadShader("shader.default.gui.textured.fragment");
		model = ResourceLoader.INSTANCE().loadModel("model.gui.default.element");

	}
	
	@Override
	public void init() {


		objectShader = new ShaderProgram();
		objectShader.addVertexShader(vertexShaderSource.getShaderProgram());
		objectShader.addFragmentShader(fragmentShaderSource.getShaderProgram());
		objectShader.compileShader();
		objectShader.bind();
		try {
			objectShader.createUniform("mvp");
            objectShader.createUniform("useTexture");
            objectShader.createUniform("backgroundColor"); 
            objectShader.createUniform("hoverColor");
            objectShader.createUniform("hoverIntensity");
			objectShader.createUniform("texture_sampler");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		objectShader.unbind();
	}

	@Override
	public void release() {
		this.objectShader.cleanup();
		defaultWWidgetMesh.cleanup();				
	}

	@Override
	public void loadMesh() {
    	defaultWWidgetMesh = new GraphicalMesh(model.getVertices(), model.getStructure(), model.getIndices());
    	
		
	}

	@Override
	public GraphicalMesh getMesh() {
		
		if (defaultWWidgetMesh == null) {
			this.loadMesh();
		}

		return defaultWWidgetMesh;
	}

}
