package com.xamlo.core.engine.graphics.components;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawElements;	

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.components.ISceneRenderer;
import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.core.engine.graphics.api.gui.AbstractUIElement;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.opengl.blend.EnumOpenglBlendMode;
import com.xamlo.core.engine.graphics.opengl.blend.OpenGLBlend;
import com.xamlo.core.engine.graphics.opengl.depth.EnumOpenGLDepthMode;
import com.xamlo.core.engine.graphics.opengl.depth.OpenGLDepth;

import com.xamlo.engine.api.resources.IResourceLoader;

public class DefaultSceneRenderer implements ISceneRenderer {
	
	AbstractUIElement debugUIElement = new AbstractUIElement() {
		//NO-OP DEBUG
	};
	private UIElementRenderer uiRenderer;
	
	public DefaultSceneRenderer(IResourceLoader<String> resourceLoader) {
		this.uiRenderer = new UIElementRenderer();
	}

	@Override
	public void init() {
        //Рисовать рамку или заливать цветом - закомментировать, если хотим цвет
		//GL13.glPolygonMode(GL13.GL_FRONT_AND_BACK, GL13.GL_LINE);
		
		OpenGLBlend.enable();
		OpenGLBlend.setMode(EnumOpenglBlendMode.SRC_ALPHA, EnumOpenglBlendMode.ONE_MINUS_SRC_ALPHA);
        
		
		OpenGLDepth.enable();
        OpenGLDepth.setDepthMode(EnumOpenGLDepthMode.ALWAYS);
        
        debugUIElement.init();
        
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
        
		
		// System.out.println("i render " + scene.getRenderableObject().size() + " objects");

		for (AbstractSceneElement obj : scene.getRenderableObject()) {
			
			obj.getShader().bind();
			
			obj.getShader().setUniform("projectionMatrix", scene.getProjectionMatrix());
			
			//obj.getShader().setUniform("texture_sampler", 0);
						
			//Теперь матрица преобразования обновляется каждый раз
			obj.getShader().setUniform("positionMatrix", obj.getPositionMatrix());
			
			obj.getShader().setUniform("rotationMatrix", obj.getRotationMatrix());
			
			obj.getShader().setUniform("scaleMatrix", obj.getScaleMatrix());
			
			obj.getShader().setUniform("objectMatrix", obj.getObjectMatrix());
			
			GL13.glActiveTexture(GL_TEXTURE0);
			
			obj.getMesh().bind();

		    // Draw the vertices
		    GL11.glDrawArrays(GL_TRIANGLES, 0, obj.getMesh().getVertexCount());
			
			/**
			 * mode: Задает примитивы для рендеринга, в данном случае треугольники. Здесь никаких изменений.
			 * count: Указывает количество элементов, которые должны быть отрисованы.
			 * type: Указывает тип значения в данных индексов. В данном случае мы используем целые числа.
			 * indices: Задает смещение, которое необходимо применить к данным индексов для начала рендеринга.
			 */
			glDrawElements(GL_TRIANGLES, obj.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);
			
			// На самом деле разбинживать меш вовсе не обязательно, но я так хочу
			obj.getMesh().unbind();

			// На самом деле разбинживать шейдер вовсе не обязательно, но я так хочу
			obj.getShader().unbind();

		}
		
		
		// System.out.println("i render " + scene.getGuiElements().size() + " ui elements");

		for (IUIElement element : scene.getGuiElements()) {
			
			uiRenderer.renderElement(debugUIElement, element, scene);

		}
		

	}
	

	@Override
	public void cleanup() {
		debugUIElement.release();		
	}

	
}
