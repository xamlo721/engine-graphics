package com.xamlo.core.engine.graphics.components;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

import org.joml.Matrix4f;
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
	
	private Matrix4f mvpMatrix = new Matrix4f();
	
	public void renderElement(AbstractUIElement debugUIElement, IUIElement element, IScene scene) {
		
		if (!(element instanceof IResizable)) {
			return;
		}
		
	    UIElementGeometry geometry = ((IResizable)element).getGeometry();
		
	    final float screenWidth = 1920.0f;
	    final float screenHeight = 1080.0f;
		
	    final float left = 0;
	    final float right =  screenWidth;
	    
	    final float bottom = screenHeight;
	    final float top =  0.0f;
	    
	    final float near = 0.2f;
	    final float far = 1000.0f;
	    
	    float Tx = geometry.getXCoord() + geometry.getWidth()/2;
	    float Ty = geometry.getYCoord() + geometry.getHeight()/2;
	    float Tz = -1.0f; // вместо 0.0f

		debugUIElement.getShader().bind();
	    
		mvpMatrix.zero()
				.setOrtho(left, right, bottom, top, near, far)
				.translate(Tx, Ty, Tz)
				.scale(geometry.getWidth(), -geometry.getHeight(), 1.0f);

	    debugUIElement.getShader().setUniform("mvp", mvpMatrix);
	    
        if (element instanceof IHoverable && ((IHoverable)element).isHovered()) {
            debugUIElement.getShader().setUniform("hoverColor", ((IHoverable)element).getHoverColor().getColorVector());
            debugUIElement.getShader().setUniform("hoverIntensity", 0.001f);
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
