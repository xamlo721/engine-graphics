package com.xamlo.core.engine.graphics.api.gui;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.xamlo.core.engine.graphics.api.primitives.IVertex;
import com.xamlo.core.engine.graphics.components.AbstractRenderableObject;
import com.xamlo.core.engine.graphics.components.GraphicalMesh;
import com.xamlo.core.engine.graphics.components.ShaderProgram;
import com.xamlo.core.engine.graphics.components.attribs.PositionAttribute;
import com.xamlo.core.engine.graphics.components.attribs.TexCoordAttribute;
import com.xamlo.core.engine.graphics.primitives.Vertex;
import com.xamlo.core.engine.graphics.primitives.VertexStructure;
import com.xamlo.engine.api.resources.IShaderResource;
import com.xamlo.engine.resources.ResourceLoader;

public abstract class AbstractUIElement extends AbstractRenderableObject {

	// Shaders
	
	private final IShaderResource<String> vertexShaderSource;
	
	private final IShaderResource<String> fragmentShaderSource;
	
	
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
			
	    	IVertex[] vertices = new Vertex[4];
	    	int i = 0;
	    	
	    	VertexStructure vertexScruct = new VertexStructure();
	    	vertexScruct.addAttribute(new PositionAttribute());
	    	vertexScruct.addAttribute(new TexCoordAttribute());
	    	vertexScruct.setVertexCount(4);

//			Мне что-то кажется, что это не так работает. Шёл третий час ночи 09.02.2024
//	    	IVertex v1 = new Vertex(5).append(new Vector3f(-1.0f,  1.0f, 0.0f)).append(new Vector2f(0.0f, 0.0f)); //V1
//	    	IVertex v2 = new Vertex(5).append(new Vector3f(-1.0f, -1.0f, 0.0f)).append(new Vector2f(0.0f, 1.0f)); //V2
//	    	IVertex v3 = new Vertex(5).append(new Vector3f( 1.0f, -1.0f, 0.0f)).append(new Vector2f(1.0f, 1.0f)); //V3
//	    	IVertex v4 = new Vertex(5).append(new Vector3f( 1.0f,  1.0f, 0.0f)).append(new Vector2f(1.0f, 0.0f)); //V4
	    	
	    	IVertex v1 = new Vertex(5).append(new Vector3f( 0.0f,  1.0f, 0.0f)).append(new Vector2f(0.0f, 0.0f)); //V1
	    	IVertex v2 = new Vertex(5).append(new Vector3f( 0.0f,  0.0f, 0.0f)).append(new Vector2f(0.0f, 1.0f)); //V2
	    	IVertex v3 = new Vertex(5).append(new Vector3f( 1.0f,  0.0f, 0.0f)).append(new Vector2f(1.0f, 1.0f)); //V3
	    	IVertex v4 = new Vertex(5).append(new Vector3f( 1.0f,  1.0f, 0.0f)).append(new Vector2f(1.0f, 0.0f)); //V4
	    	
	    	vertices[i++] = v1;
	    	vertices[i++] = v2;
	    	vertices[i++] = v3;
	    	vertices[i++] = v4;


	    	i = 0;
	    	int[] indices = new int[6]; 
	    	//FACE
	    	indices[i++] = 0;
	    	indices[i++] = 1;
	    	indices[i++] = 3;
	    	
	    	indices[i++] = 3;
	    	indices[i++] = 1;
	    	indices[i++] = 2;

	    	defaultWWidgetMesh = new GraphicalMesh(vertices, vertexScruct, indices);
	    	
	    	v1.release();
	    	v2.release();
	    	v3.release();
	    	v4.release();
	    	
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
