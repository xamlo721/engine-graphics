package com.xamlo.core.engine.graphics.components;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL13;

import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.gui.AbstractUIElement;
import com.xamlo.core.engine.graphics.api.gui.IBackgroundSupport;
import com.xamlo.core.engine.graphics.api.gui.IHoverable;
import com.xamlo.core.engine.graphics.api.gui.IResizable;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;

public class UIElementRenderer {

	
	public void renderElement(AbstractUIElement debugUIElement, IUIElement element, IScene scene) {
		
		if (!(element instanceof IResizable)) {
			return;
		}
		
		UIElementGeometry geometry = ((IResizable)element).getGeometry();
	
		final float screenWidght = 1920.0f *1;
		final float screenheight = 1080.0f *1;
		
		//Координты начала отрисовки объекта
		float localScreenXCoord = (((float) geometry.getXCoord() - screenWidght/2)) / (screenWidght); 		// 0,052083332 --- 0,229166662
		float localScreenYCoord = (((float) geometry.getYCoord() - screenheight/2)) / screenheight;   		// 0,037037037 --- 0,962962937

		float displayedWidth = geometry.getWidth()  / screenWidght;							//0,17708333
		float displayedHeight= geometry.getHeight() / screenheight;							//0,9259259

		
		Matrix4f positionMatrix = new Matrix4f().identity().translate(localScreenXCoord, localScreenYCoord, 0.666f);

		
		debugUIElement.getShader().bind();
		
		debugUIElement.getShader().setUniform("projectionMatrix", scene.getProjectionMatrix());

		debugUIElement.getShader().setUniform("positionMatrix", positionMatrix);

		Matrix4f rotationMatrix = new Matrix4f().identity().rotateX(0.0f).rotateY(0.0f).rotateZ(0.0f);
		debugUIElement.getShader().setUniform("rotationMatrix", rotationMatrix);
		
		
		Matrix4f scaleMatrix = new Matrix4f().identity().scale(new Vector3f(displayedWidth, displayedHeight, 1.0f));
		debugUIElement.getShader().setUniform("scaleMatrix", scaleMatrix);

        if (element instanceof IHoverable && ((IHoverable)element).isHovered()) {
            debugUIElement.getShader().setUniform("hoverColor", ((IHoverable)element).getHoverColor().getColorVector());
            debugUIElement.getShader().setUniform("hoverIntensity", 0.5f); // Пример интенсивности
        } else {
            debugUIElement.getShader().setUniform("hoverColor", new Vector4f(0, 0, 0, 0));
            debugUIElement.getShader().setUniform("hoverIntensity", 0.0f);
        }
        
		debugUIElement.getMesh().bind();
				
		if (element instanceof IBackgroundSupport && ((IBackgroundSupport)element).hasBackgroundImage()) {
			GL13.glActiveTexture(GL_TEXTURE0);
			((IBackgroundSupport)element).getBackgroundImage().bind();
		}

		/**
		 * mode: Задает примитивы для рендеринга, в данном случае треугольники. Здесь никаких изменений.
		 * count: Указывает количество элементов, которые должны быть отрисованы.
		 * type: Указывает тип значения в данных индексов. В данном случае мы используем целые числа.
		 * indices: Задает смещение, которое необходимо применить к данным индексов для начала рендеринга.
		 */
		glDrawElements(GL_TRIANGLES, debugUIElement.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);
		
		
		// На самом деле разбинживать меш вовсе не обязательно, но я так хочу
		debugUIElement.getMesh().unbind();

		// На самом деле разбинживать шейдер вовсе не обязательно, но я так хочу
		debugUIElement.getShader().unbind();

		
	}
	
}
