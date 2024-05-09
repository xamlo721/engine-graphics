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
	
	
	protected static AbstractRenderableObject uiGrapphicElement = new AbstractRenderableObject() {

	    private static GraphicalMesh defaultWWidgetMesh;

		@Override
		public void init() {
			//NO-OP
		}

		@Override
		public void release() {
			defaultWWidgetMesh.cleanup();				
		}

		@Override
		public GraphicalMesh getMesh() {
			
			if (defaultWWidgetMesh == null) {
				this.loadMesh();
			}

			return defaultWWidgetMesh;
		}

		@Override
		public void loadMesh() {
			model = ResourceLoader.INSTANCE().loadModel("model.gui.default.element");
	    	defaultWWidgetMesh = new GraphicalMesh(model.getVertices(), model.getStructure(), model.getIndices());
	    	
		}
		
	};
	
	
	public AbstractUIElement() {
		super();
		this.vertexShaderSource = ResourceLoader.INSTANCE().loadShader("shader.primitive.textured.vertex");
		this.fragmentShaderSource = ResourceLoader.INSTANCE().loadShader("shader.primitive.textured.fragment");
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

	@Override
	public void loadMesh() {
		uiGrapphicElement.loadMesh();
		
	}

	@Override
	public GraphicalMesh getMesh() {
		return uiGrapphicElement.getMesh();
	}

}
