package com.xamlo.core.engine.graphics.components;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL13.GL_BLEND;
import static org.lwjgl.opengl.GL13.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL13.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL13.glBlendFunc;
import static org.lwjgl.opengl.GL13.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL13.GL_LINE;
import static org.lwjgl.opengl.GL13.glPolygonMode;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.components.ISceneRenderer;
import com.xamlo.core.engine.graphics.api.gui.IWidget;
import com.xamlo.core.engine.graphics.api.primitives.IVertex;
import com.xamlo.core.engine.graphics.components.attribs.PositionAttribute;
import com.xamlo.core.engine.graphics.components.attribs.TexCoordAttribute;
import com.xamlo.core.engine.graphics.components.gui.WidgetGeometry;
import com.xamlo.core.engine.graphics.primitives.Vertex;
import com.xamlo.core.engine.graphics.primitives.VertexStructure;
import com.xamlo.engine.api.resources.IShaderResource;
import com.xamlo.engine.api.resources.ResourceLoader;

public class DefaultSceneRenderer implements ISceneRenderer {
	
	// Shaders
	
	private final IShaderResource<String> vertexShaderSource;
	
	private final IShaderResource<String> fragmentShaderSource;
	
	private ShaderProgram shaderProgram;

	public DefaultSceneRenderer(ResourceLoader<String> resourceLoader) {
		this.vertexShaderSource = resourceLoader.loadShader("shader.primitive.vertex");
		this.fragmentShaderSource = resourceLoader.loadShader("shader.primitive.fragment");
	}

	static AbstractRenderableObject uiGrapphicElement = new AbstractRenderableObject() {

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
	@Override
	public void init() {
        //Рисовать рамку или заливать цветом - закомментировать, если хотим цвет
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glEnable(GL_DEPTH_TEST);		
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		


		shaderProgram = new ShaderProgram();
		shaderProgram.addVertexShader(vertexShaderSource.getShaderProgram());
		shaderProgram.addFragmentShader(fragmentShaderSource.getShaderProgram());
		shaderProgram.compileShader();
		shaderProgram.bind();
		try {
			shaderProgram.createUniform("projectionMatrix");
			//shaderProgram.createUniform("worldMatrix");
			shaderProgram.createUniform("positionMatrix");
			shaderProgram.createUniform("scaleMatrix");
			shaderProgram.createUniform("rotationMatrix");
			shaderProgram.createUniform("texture_sampler");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		shaderProgram.unbind();
        
        // clear the framebuffer
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);


	}

	@Override
	public void loadScene(IScene scene) {
		
		//TODO: Подгрузка в кеш текстур и моделей сцены
		
		scene.load();
		
	}
	
	@Override
	public void render(IScene scene) {
        
		shaderProgram.bind();
		
		shaderProgram.setUniform("projectionMatrix", scene.getProjectionMatrix());
		shaderProgram.setUniform("texture_sampler", 0);
		
		System.out.println("i render " + scene.getRenderableObject().size() + " objects");

		for (AbstractRenderableObject obj : scene.getRenderableObject()) {
			//Теперь матрица преобразования обновляется каждый раз
			//shaderProgram.setUniform("worldMatrix", obj.getWorldMatrix());
//			shaderProgram.setUniform("positionMatrix", obj.getPositionMatrix());
//			shaderProgram.setUniform("rotationMatrix", obj.getPositionMatrix());
//			shaderProgram.setUniform("scaleMatrix", obj.getPositionScale());
		
			glActiveTexture(GL_TEXTURE0);
			
//			smile.bind();
			obj.getMesh().bind();
//			if (obj.hasBackgroundImage()) {
//				obj.getBackgroundImage().bind();
//			}

		    // Draw the vertices
		    //glDrawArrays(GL_TRIANGLES, 0, mesh.getNumVertices());
			

			/**
			 * mode: Задает примитивы для рендеринга, в данном случае треугольники. Здесь никаких изменений.
			 * count: Указывает количество элементов, которые должны быть отрисованы.
			 * type: Указывает тип значения в данных индексов. В данном случае мы используем целые числа.
			 * indices: Задает смещение, которое необходимо применить к данным индексов для начала рендеринга.
			 */
			glDrawElements(GL_TRIANGLES, obj.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);

		}
		System.out.println("i render " + scene.getGuiElements().size() + " ui elements");

		for (IWidget obj : scene.getGuiElements()) {
			

			
			//TODO: Протестить, те ли вообще поля я трогаю
			//this.setExpandGeometry((float)this.geometry.width / (float)geometry.width, (float)this.geometry.height / (float)geometry.height, 1.0f);
			//this.setExpandGeometry((float)geometry.width/ (float)1920 ,(float)geometry.height/ (float)1080  , 1.0f);

			WidgetGeometry geometry = obj.getWidgetGeometry();
			System.out.println("																				");
			System.out.println("rendering UIElement :" + obj);

			
			
			
			
			final float screenWidght = 1920.0f *1;
			final float screenheight = 1080.0f *1;
			
			//Координты начала отрисовки объекта
			float localScreenXCoord = (((float) geometry.getXCoord() - screenWidght/2)) / (screenWidght); 		// 0,052083332 --- 0,229166662
			float localScreenYCoord = (((float) geometry.getYCoord() - screenheight/2)) / screenheight;   		// 0,037037037 --- 0,962962937
			
			


			float displayedWidth = geometry.getWidth()  / screenWidght;							//0,17708333
			float displayedHeight= geometry.getHeight() / screenheight;							//0,9259259

			
			Matrix4f positionMatrix = new Matrix4f().identity().translate(localScreenXCoord, localScreenYCoord, 0.666f);
			
			if (scene.getGuiElements().indexOf(obj) == 0) {
				
				System.out.println("UIElement wight" + this + ". coords:  width [" + geometry.getXCoord() + ", " + (geometry.getWidth() + geometry.getXCoord()) + "]" );
				System.out.println("UIElement height" + this + ". coords:  height [" + geometry.getYCoord() + ", " + (geometry.getHeight() + geometry.getYCoord()) + "]" );

				System.out.println("UIElement draw X position:  [" + localScreenXCoord + "]" );		// 0,052083332 --- 0,229166662
				System.out.println("UIElement draw Y position:  [" + localScreenYCoord + "]" );		// 0,037037037 --- 0,962962937
				
				System.out.println("UIElement draw width:  [" + displayedWidth  + "]" );			//0.17708333
				System.out.println("UIElement draw height: [" + displayedHeight + "]" );			//0.9259259
				
				Vector3f startPoint = new Vector3f(geometry.getXCoord(), geometry.getYCoord(), 1.0f);
				System.out.println("vector moved from : [" + startPoint.toString() + "]" );	
				System.out.println("vector moved to : [" + positionMatrix.transformPosition(startPoint).toString() + "]" );	
			}

			shaderProgram.setUniform("positionMatrix", positionMatrix);

			/**
			 * {1.0, 0.0, 0.0,  0.0}
			 * {0.0, 1.0, 0.0,  0.0}
			 * {0.0, 0.0, 1.0,  0.0}
			 * {  X,   Y,   Z,  1.0}
			 * 
			 */
			//Хотя должно быть
			/**
			 * {1.0, 0.0, 0.0,    X}
			 * {0.0, 1.0, 0.0,    Y}
			 * {0.0, 0.0, 1.0,    Z}
			 * {0.0, 0.0, 0.0,  1.0}
			 * 
			 */
			Matrix4f rotationMatrix = new Matrix4f().identity().rotateX(0.0f).rotateY(0.0f).rotateZ(0.0f);
			shaderProgram.setUniform("rotationMatrix", rotationMatrix);

			/**
			 * 			    X						Y 								Z
			 * {1.0,  0.0, 	   0.0,    0.0} * {  cos(y), 0.0, sin(y),  0.0} * {cos(z), -sin(z), 0.0,  0.0}
			 * {0.0, cos(x), -cos(x),  0.0} * {   0.0,   1.0,  0.0,    0.0} * {sin(z),  cos(z), 0.0,  0.0}
			 * {0.0, sin(x),  cos(x),  0.0} * { -sin(y), 0.0, сos(y),  0.0} * { 0.0,     0.0,   1.0,  0.0}
			 * {0.0,  0.0,     0.0,    1.0} * {   0.0,   0.0,  0.0,    1.0} * { 0.0,     0.0,   0.0,  1.0} 
			 * 
			 */

			
			
			Matrix4f scaleMatrix = new Matrix4f().identity().scale(new Vector3f(displayedWidth, displayedHeight, 1.0f));
			shaderProgram.setUniform("scaleMatrix", scaleMatrix);

			/**
			 * { X , 0.0, 0.0,  0.0}
			 * {0.0,  Y , 0.0,  0.0}
			 * {0.0, 0.0,  Z ,  0.0}
			 * {0.0, 0.0, 0.0,  1.0}
			 * 
			 */
			
			if (scene.getGuiElements().indexOf(obj) == 0) {

				System.out.println("[MATRIX] updating world matrix for positionMatrix");
				System.out.println("[MATRIX] {" + positionMatrix.m00() + ", " + positionMatrix.m01() + ", " + positionMatrix.m02() + ", " + positionMatrix.m03() + "}");
				System.out.println("[MATRIX] {" + positionMatrix.m10() + ", " + positionMatrix.m11() + ", " + positionMatrix.m12() + ", " + positionMatrix.m13() + "}");
				System.out.println("[MATRIX] {" + positionMatrix.m20() + ", " + positionMatrix.m21() + ", " + positionMatrix.m22() + ", " + positionMatrix.m23() + "}");
				System.out.println("[MATRIX] {" + positionMatrix.m30() + ", " + positionMatrix.m31() + ", " + positionMatrix.m32() + ", " + positionMatrix.m33() + "}");
				System.out.println("																																");
				System.out.println("																																");
				System.out.println("																																");
				
				System.out.println("[MATRIX] updating world matrix for rotationMatrix");
				System.out.println("[MATRIX] {" + rotationMatrix.m00() + ", " + rotationMatrix.m01() + ", " + rotationMatrix.m02() + ", " + rotationMatrix.m03() + "}");
				System.out.println("[MATRIX] {" + rotationMatrix.m10() + ", " + rotationMatrix.m11() + ", " + rotationMatrix.m12() + ", " + rotationMatrix.m13() + "}");
				System.out.println("[MATRIX] {" + rotationMatrix.m20() + ", " + rotationMatrix.m21() + ", " + rotationMatrix.m22() + ", " + rotationMatrix.m23() + "}");
				System.out.println("[MATRIX] {" + rotationMatrix.m30() + ", " + rotationMatrix.m31() + ", " + rotationMatrix.m32() + ", " + rotationMatrix.m33() + "}");
				System.out.println("																																");
				System.out.println("																																");
				System.out.println("																																");

				System.out.println("[MATRIX] updating world matrix for scaleMatrix");
				System.out.println("[MATRIX] {" + scaleMatrix.m00() + ", " + scaleMatrix.m01() + ", " + scaleMatrix.m02() + ", " + scaleMatrix.m03() + "}");
				System.out.println("[MATRIX] {" + scaleMatrix.m10() + ", " + scaleMatrix.m11() + ", " + scaleMatrix.m12() + ", " + scaleMatrix.m13() + "}");
				System.out.println("[MATRIX] {" + scaleMatrix.m20() + ", " + scaleMatrix.m21() + ", " + scaleMatrix.m22() + ", " + scaleMatrix.m23() + "}");
				System.out.println("[MATRIX] {" + scaleMatrix.m30() + ", " + scaleMatrix.m31() + ", " + scaleMatrix.m32() + ", " + scaleMatrix.m33() + "}");
				System.out.println("																													");
				System.out.println("																													");
				System.out.println("																													");
			}
			
			uiGrapphicElement.getMesh().bind();
						
			if (obj.hasBackgroundImage()) {
				glActiveTexture(GL_TEXTURE0);
				obj.getBackgroundImage().bind();
			}


			glDrawElements(GL_TRIANGLES, uiGrapphicElement.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);
			
		}
		
	    shaderProgram.unbind();


		
	}
	

	@Override
	public void cleanup() {
		shaderProgram.cleanup();			
	}



}
