package com.xamlo.core.engine.graphics.renderers;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.components.AbstractRenderableObject;

public class SceneElementRenderer {

	public void init() {

	}
	
	public void release() {
		
	}

	
	
	public void draw(AbstractRenderableObject obj, IScene scene) {
		
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
	
}
