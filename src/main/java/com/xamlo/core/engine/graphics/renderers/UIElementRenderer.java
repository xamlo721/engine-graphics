package com.xamlo.core.engine.graphics.renderers;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.gui.AbstractUIElement;
import com.xamlo.core.engine.graphics.api.gui.IBackgroundSupport;
import com.xamlo.core.engine.graphics.api.gui.IBorderSupport;
import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.IFillIndicator;
import com.xamlo.core.engine.graphics.api.gui.IFocusable;
import com.xamlo.core.engine.graphics.api.gui.IHoverable;
import com.xamlo.core.engine.graphics.api.gui.IResizable;
import com.xamlo.core.engine.graphics.api.gui.IStretchedBackground;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.api.gui.IVisible;
import com.xamlo.core.engine.graphics.components.AbstractTexture;
import com.xamlo.core.engine.graphics.components.gui.Color;
import com.xamlo.core.engine.graphics.components.gui.EnumImageStretch;
import com.xamlo.core.engine.graphics.components.gui.EnumOrientation;
import com.xamlo.core.engine.graphics.components.gui.FrameRects;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.opengl.blend.EnumOpenglBlendMode;
import com.xamlo.core.engine.graphics.opengl.blend.OpenGLBlend;

public class UIElementRenderer {

	/** Толщина кольца-индикатора клавиатурного фокуса в пикселях. */
	public static final float FOCUS_RING_THICKNESS = 2f;

	/** Акцентный цвет кольца фокуса (тот же акцент, что у индикаторов чекбокса/radio). */
	static final Vector4f FOCUS_RING_COLOR = new Color(72, 140, 220).getColorVector();

	private Matrix4f mvpMatrix = new Matrix4f();

	/** Батч квадов для режимов растяжения фона, отличных от FILL (E3). */
	private final TexturedQuadBatch texturedBatch = new TexturedQuadBatch();

	AbstractUIElement debugUIElement;
	
	public void init() {
		this.debugUIElement = new AbstractUIElement() {
			//NO-OP DEBUG
		};
        debugUIElement.init();
        texturedBatch.init();
	}
	
	public void release() {
		texturedBatch.release();
		debugUIElement.release();		
	}

	
	public void draw(IUIElement element, IScene scene) {

		// Невидимые элементы не рисуются (скрытые индикаторы, страницы табов, строки закрытого списка).
		if (element instanceof IVisible && !((IVisible) element).isVisible()) {
			return;
		}

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
	    
	    float Tx = element.getAbsX() + geometry.getWidth()/2;
	    float Ty = element.getAbsY() + geometry.getHeight()/2;
	    float Tz = -1.0f; // вместо 0.0f

		debugUIElement.getShader().bind();
		ClipBinder.apply(debugUIElement.getShader(), element);

		mvpMatrix.zero()
				.setOrtho(left, right, bottom, top, near, far)
				.translate(Tx, Ty, Tz)
				.scale(geometry.getWidth(), -geometry.getHeight(), 1.0f);

	    debugUIElement.getShader().setUniform("mvp", mvpMatrix);
	    
        final IBackgroundSupport background = (element instanceof IBackgroundSupport b) ? b : null;
        final AbstractTexture texture = (background != null && background.hasBackgroundImage())
                ? background.getBackgroundImage() : null;
        // E3: режим растяжения текстурированного фона. По умолчанию FILL — исторический путь unit-mesh'а не меняется.
        final IStretchedBackground stretched = (element instanceof IStretchedBackground s) ? s : null;
        List<ImageStretchLayout.StretchPiece> pieces = java.util.Collections.emptyList();
        boolean plainFillPath = true;
        if (texture != null) {
            EnumImageStretch mode = (stretched != null) ? stretched.getBackgroundStretchMode() : EnumImageStretch.FILL;
            ImageStretchLayout.NinePatchInsets insets = (stretched != null)
                    ? stretched.getNinePatchInsets() : ImageStretchLayout.NinePatchInsets.NONE;
            pieces = ImageStretchLayout.layout(mode, texture.getWidth(), texture.getHeight(),
                    geometry.getWidth(), geometry.getHeight(), insets);
            plainFillPath = ImageStretchLayout.isPlainFill(pieces, texture.getWidth(), texture.getHeight(),
                    geometry.getWidth(), geometry.getHeight());
        }

        debugUIElement.getShader().setUniform("useTexture", texture != null);

        Vector4f bgColorVector;
        if (element instanceof IBackgroundSupport) {
            IColor bgColor = ((IBackgroundSupport)element).getBackgroundColor();
            if (bgColor != null) {
                bgColorVector = bgColor.getColorVector();
            } else {
                bgColorVector = new Vector4f(1, 1, 1, 1); // Белый по умолчанию
            }
        } else {
            bgColorVector = new Vector4f(1, 1, 1, 1); // Белый по умолчанию
        }
        debugUIElement.getShader().setUniform("backgroundColor", bgColorVector);
        
        if (element instanceof IHoverable && ((IHoverable)element).isHovered()) {
        	
            Vector4f hoverColorVector;
            IColor hoverColor = ((IHoverable)element).getHoverColor();
            if (hoverColor != null) {
            	hoverColorVector = hoverColor.getColorVector();
            } else {
            	hoverColorVector = new Vector4f(0, 0, 0, 0);
            }
            
            debugUIElement.getShader().setUniform("hoverColor", hoverColorVector);
            debugUIElement.getShader().setUniform("hoverIntensity", 0.1f);
        } else {
            debugUIElement.getShader().setUniform("hoverColor", new Vector4f(0, 0, 0, 0));
            debugUIElement.getShader().setUniform("hoverIntensity", 0.0f);
        }
        
		debugUIElement.getMesh().bind();

		if (texture != null) {
			glActiveTexture(GL_TEXTURE0);
			texture.bind();
		}

		OpenGLBlend.enable();
		OpenGLBlend.setMode(EnumOpenglBlendMode.SRC_ALPHA, EnumOpenglBlendMode.ONE_MINUS_SRC_ALPHA);


        // Исторический путь: цветной квад или FILL-фон — побайтово то же самое, что до E3.
		boolean useLegacyQuad = (texture == null) || plainFillPath;
		if (useLegacyQuad) {
			/**
			 * mode: Задает примитивы для рендеринга, в данном случае треугольники. Здесь никаких изменений.
			 * count: Указывает количество элементов, которые должны быть отрисованы.
			 * type: Указывает тип значения в данных индексов. В данном случае мы используем целые числа.
			 * indices: Задает смещение, которое необходимо применить к данным индексов для начала рендеринга.
			 */
			glDrawElements(GL_TRIANGLES, debugUIElement.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);
		} else {
			texturedBatch.draw(debugUIElement.getShader(), left, right, bottom, top, near, far,
					element.getAbsX(), element.getAbsY(),
					texture.getWidth(), texture.getHeight(), pieces);
		}

        // Заполнение внутри границ элемента для IFillIndicator (progress bar / слайдер)
        if (element instanceof IFillIndicator) {
            IFillIndicator fill = (IFillIndicator) element;
            IColor fillColor = fill.getFillColor();
            float fraction = Math.max(0f, Math.min(1f, fill.getFillFraction()));
            if (fillColor != null && fraction > 0f) {
                boolean vertical = fill.getFillDirection() == EnumOrientation.VERTICAL;
                float fillW = vertical ? geometry.getWidth() : geometry.getWidth() * fraction;
                float fillH = vertical ? geometry.getHeight() * fraction : geometry.getHeight();
                float cx = element.getAbsX() + fillW / 2f;
                float cy = element.getAbsY() + fillH / 2f;

                mvpMatrix.zero()
                        .setOrtho(left, right, bottom, top, near, far)
                        .translate(cx, cy, Tz)
                        .scale(fillW, -fillH, 1.0f);

                debugUIElement.getShader().bind();
                debugUIElement.getShader().setUniform("mvp", mvpMatrix);
                debugUIElement.getShader().setUniform("useTexture", false);
                debugUIElement.getShader().setUniform("backgroundColor", fillColor.getColorVector());

                glDrawElements(GL_TRIANGLES, debugUIElement.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);
            }
        }

        // Рамка элемента (borderSize > 0) поверх фона. TODO(E3): скругление углов по cornerRadius.
        if (element instanceof IBorderSupport borderSupport && borderSupport.getBorderSize() > 0) {
            IColor borderColor = borderSupport.getBorderColor();
            Vector4f borderVector = (borderColor != null) ? borderColor.getColorVector() : new Vector4f(1, 1, 1, 1);
            List<FrameRects.Strip> strips = FrameRects.innerOutline(element.getAbsX(), element.getAbsY(),
                    geometry.getWidth(), geometry.getHeight(), borderSupport.getBorderSize());
            drawFrameStrips(strips, borderVector);
        }

        // Кольцо клавиатурного фокуса: вплотную снаружи сфокусированного элемента.
        if (element instanceof IFocusable focusableElement && focusableElement.isFocused()) {
            List<FrameRects.Strip> ring = FrameRects.outerRing(element.getAbsX(), element.getAbsY(),
                    geometry.getWidth(), geometry.getHeight(), FOCUS_RING_THICKNESS);
            drawFrameStrips(ring, FOCUS_RING_COLOR);
        }

		OpenGLBlend.disable();

		// На самом деле разбинживать меш вовсе не обязательно, но я так хочу
		debugUIElement.getMesh().unbind();

		// На самом деле разбинживать шейдер вовсе не обязательно, но я так хочу
		debugUIElement.getShader().unbind();

	}

	/**
	 * Каждая полоса рамки рисуется тем же единичным мешом, что фон и fill-индикатор:
	 * ортопрожекция 1920×1080 + сдвиг в центр полосы + масштаб на её размеры.
	 */
	private void drawFrameStrips(List<FrameRects.Strip> strips, Vector4f color) {
        if (strips.isEmpty()) {
            return;
        }

        final float left = 0;
        final float right = 1920.0f;
        final float bottom = 1080.0f;
        final float top = 0.0f;
        final float near = 0.2f;
        final float far = 1000.0f;
        final float Tz = -1.0f;

        debugUIElement.getShader().setUniform("useTexture", false);
        debugUIElement.getShader().setUniform("backgroundColor", color);

        for (FrameRects.Strip strip : strips) {
            mvpMatrix.zero()
                    .setOrtho(left, right, bottom, top, near, far)
                    .translate(strip.x() + strip.width() / 2f, strip.y() + strip.height() / 2f, Tz)
                    .scale(strip.width(), -strip.height(), 1.0f);
            debugUIElement.getShader().setUniform("mvp", mvpMatrix);
            glDrawElements(GL_TRIANGLES, debugUIElement.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);
        }
    }


}
