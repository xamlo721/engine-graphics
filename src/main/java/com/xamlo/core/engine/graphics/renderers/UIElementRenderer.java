package com.xamlo.core.engine.graphics.renderers;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.util.List;

import com.xamlo.core.engine.graphics.api.gui.ITextureHandle;
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
import com.xamlo.core.engine.graphics.api.gui.IVerticalGradient;
import com.xamlo.core.engine.graphics.api.gui.IStretchedBackground;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.api.gui.IVisible;
import com.xamlo.core.engine.graphics.api.gui.elements.IImageView;
import com.xamlo.core.engine.graphics.components.gui.Color;
import com.xamlo.core.engine.graphics.components.gui.EnumImageStretch;
import com.xamlo.core.engine.graphics.components.gui.EnumOrientation;
import com.xamlo.core.engine.graphics.components.gui.FrameRects;
import com.xamlo.core.engine.graphics.components.gui.GradientStrips;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.opengl.blend.EnumOpenglBlendMode;
import com.xamlo.core.engine.graphics.opengl.blend.OpenGLBlend;
import com.xamlo.core.engine.graphics.viewport.ViewportSettings;

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
		
	    // E9: логическое пространство отрисовки — заданная сетка или legacy-константы при выключенном вьюпорте.
	    final float screenWidth = ViewportSettings.effectiveLogicalWidth();
	    final float screenHeight = ViewportSettings.effectiveLogicalHeight();

	    final float left = 0;
	    final float right =  screenWidth;

	    final float bottom = screenHeight;
	    final float top =  0.0f;

	    final float near = 0.2f;
	    final float far = 1000.0f;

	    float Tx = element.getAbsX() + geometry.getWidth()/2;
	    float Ty = element.getAbsY() + geometry.getHeight()/2;

		debugUIElement.getShader().bind();
		ClipBinder.apply(debugUIElement.getShader(), element);

		UiSpaceMatrices.quadMatrix(mvpMatrix, left, right, bottom, top, Tx, Ty,
				geometry.getWidth(), geometry.getHeight());

	    debugUIElement.getShader().setUniform("mvp", mvpMatrix);
	    
        final IBackgroundSupport background = (element instanceof IBackgroundSupport b) ? b : null;
        final ITextureHandle texture = (background != null && background.hasBackgroundImage())
                ? background.getBackgroundImage() : null;
        // E3: режим растяжения текстурированного фона. По умолчанию FILL — исторический путь unit-mesh'а не меняется.
        final IStretchedBackground stretched = (element instanceof IStretchedBackground s) ? s : null;
        List<ImageStretchLayout.StretchPiece> pieces = java.util.Collections.emptyList();
        boolean plainFillPath = true;
        if (texture != null) {
            EnumImageStretch mode = (stretched != null) ? stretched.getBackgroundStretchMode() : EnumImageStretch.FILL;
            ImageStretchLayout.NinePatchInsets insets = (stretched != null)
                    ? stretched.getNinePatchInsets() : ImageStretchLayout.NinePatchInsets.NONE;
            // Подрегион источника: при неполном регионе FILL уходит в CPU-side батч квадов (isPlainFill false),
            // исторический unit-mesh путь и шейдер не трогаются.
            final ImageStretchLayout.TextureRegion region = (stretched != null) ? stretched.getRegion()
                    : ((element instanceof IImageView view) ? view.getRegion() : ImageStretchLayout.TextureRegion.FULL);
            // Якорь обрезания/выравнивания (COVER/CENTER/FIT); дефолт — центр = историческое поведение.
            final ImageStretchLayout.StretchAnchor cropAnchor = (stretched != null) ? stretched.getCropAnchor()
                    : ((element instanceof IImageView view) ? view.getCropAnchor() : ImageStretchLayout.StretchAnchor.CENTER);
            pieces = ImageStretchLayout.layout(mode, region, texture.getWidth(), texture.getHeight(),
                    geometry.getWidth(), geometry.getHeight(), insets, cropAnchor.x(), cropAnchor.y());
            // Координаты s* фрагментов абсолютные (с учётом смещения региона) — сравнение против полного
            // размера текстуры корректно отправляет неполный регион в CPU-side батч.
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
        
        // Pressed state for push buttons mirrors the hover slot: tint with its own color when setPressed(true).
        if (element instanceof com.xamlo.core.engine.graphics.api.gui.elements.IPushButton pb && pb.isPressed()) {
            IColor pressedColorIface = pb.getButtonPressedColor();
            debugUIElement.getShader().setUniform("pressedColor", pressedColorIface != null ? pressedColorIface.getColorVector() : new Vector4f(0, 0, 0, 0));
            debugUIElement.getShader().setUniform("pressedIntensity", 0.35f);
        } else {
            debugUIElement.getShader().setUniform("pressedColor", new Vector4f(0, 0, 0, 0));
            debugUIElement.getShader().setUniform("pressedIntensity", 0.0f);
        }
        
		debugUIElement.getMesh().bind();

		if (texture != null) {
			glActiveTexture(GL_TEXTURE0);
			texture.bind();
		}

		OpenGLBlend.enable();
		OpenGLBlend.setMode(EnumOpenglBlendMode.SRC_ALPHA, EnumOpenglBlendMode.ONE_MINUS_SRC_ALPHA);


        // Одиночные нетривиальные режимы растяжения (кроп FILL/FIT/CENTER/COVER) рисуются тем же
		// рабочим unit-mesh'ом с аффинным преобразованием UV в шейдере: CPU-side батч квадов не
		// отрисовывается на этом окружении, поэтому мёртвый путь обходим. Мультифрагментные
		// режимы (nine-patch) остаются на батче как прежде.
		final boolean affineMode = texture != null && !plainFillPath && pieces.size() == 1;
		if (affineMode) {
			ImageStretchLayout.StretchPiece p = pieces.get(0);
			float texW = Math.max(1f, (float)texture.getWidth());
			float texH = Math.max(1f, (float)texture.getHeight());
			debugUIElement.getShader().setUniform("useTexAffine", true);
			debugUIElement.getShader().setUniform("uvMul",
			        new org.joml.Vector2f(p.sw() / texW, p.sh() / texH));
			debugUIElement.getShader().setUniform("uvAdd",
			        new org.joml.Vector2f(p.sx() / texW, p.sy() / texH));
			float gw = Math.max(1f, geometry.getWidth());
			float gh = Math.max(1f, geometry.getHeight());
			debugUIElement.getShader().setUniform("posScale",
			        new org.joml.Vector2f(Math.min(1f, p.dw() / gw), Math.min(1f, p.dh() / gh)));
			debugUIElement.getShader().setUniform("posOffset", new org.joml.Vector2f(0f, 0f));
		}
		else if (!affineMode && texture != null && !plainFillPath) {
			// Мультифрагментные режимы: сброс аффина перед CPU-side батчем.
			debugUIElement.getShader().setUniform("useTexAffine", false);
		}
        // Исторический путь: цветной квад или FILL-фон — побайтово то же самое, что до E3.
	// Временный механизм (сага «серый фон»): одиночные нетривиальные режимы растяжения рисуются
		// тем же рабочим unit-mesh'ом с аффинным преобразованием UV в шейдере.
		boolean useLegacyQuad = (texture == null) || plainFillPath || affineMode;
		if ((texture == null) || plainFillPath) {
			debugUIElement.getShader().setUniform("useTexAffine", false);
		}
		if (useLegacyQuad) {
			/**
			 * mode: Задает примитивы для рендеринга, в данном случае треугольники. Здесь никаких изменений.
			 * count: Указывает количество элементов, которые должны быть отрисованы.
			 * type: Указывает тип значения в данных индексов. В данном случае мы используем целые числа.
			 * indices: Задает смещение, которое необходимо применить к данным индексов для начала рендеринга.
			 */
			glDrawElements(GL_TRIANGLES, debugUIElement.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);
		} else {
			texturedBatch.draw(debugUIElement.getShader(), this.mvpMatrix,
					element.getAbsX(), element.getAbsY(), geometry.getWidth(), geometry.getHeight(),
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

                UiSpaceMatrices.quadMatrix(mvpMatrix, left, right, bottom, top, cx, cy, fillW, fillH);

                debugUIElement.getShader().bind();
                debugUIElement.getShader().setUniform("mvp", mvpMatrix);
                debugUIElement.getShader().setUniform("useTexture", false);
                debugUIElement.getShader().setUniform("backgroundColor", fillColor.getColorVector());

                glDrawElements(GL_TRIANGLES, debugUIElement.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);
            }
        }

        // Вертикальный двухцветный градиент внутри границ элемента (IVerticalGradient).
        if (element instanceof IVerticalGradient gradient && geometry.getWidth() > 0f && geometry.getHeight() > 0f) {
            drawGradientSlices(element.getAbsX(), element.getAbsY(), geometry, left, right, bottom, top, gradient);
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
	 * ортопрожекция логического пространства отрисовки + сдвиг в центр полосы + масштаб на её размеры.
	 */
	private void drawFrameStrips(List<FrameRects.Strip> strips, Vector4f color) {
        if (strips.isEmpty()) {
            return;
        }

        // E9: границы берутся из активного вьюпорта (по умолчанию — legacy-константы).
        final float left = 0;
        final float right = ViewportSettings.effectiveLogicalWidth();
        final float bottom = ViewportSettings.effectiveLogicalHeight();
        final float top = 0.0f;

        debugUIElement.getShader().setUniform("useTexture", false);
        debugUIElement.getShader().setUniform("backgroundColor", color);

        for (FrameRects.Strip strip : strips) {
            UiSpaceMatrices.quadMatrix(mvpMatrix, left, right, bottom, top,
                    strip.x() + strip.width() / 2f, strip.y() + strip.height() / 2f,
                    strip.width(), strip.height());
            debugUIElement.getShader().setUniform("mvp", mvpMatrix);
            glDrawElements(GL_TRIANGLES, debugUIElement.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);
        }
    }

    /**
     * Вертикальная рампа от цвета верхней кромки до цвета нижней ({@link IVerticalGradient}): набор
     * горизонтальных полос {@link GradientStrips}, каждая — тот же единичный меш и uniform'ы цвета/клипа,
     * что фон и полосы рамки; альфа-составляющая та же (SRC_ALPHA), что у рисования backgroundColor.
     */
    private void drawGradientSlices(float absX, float absY, UIElementGeometry geometry,
                                    float left, float right, float bottom, float top, IVerticalGradient gradient) {
        IColor topColor = gradient.getTopColor();
        IColor botColor = gradient.getBottomColor();
        if (topColor == null || botColor == null) {
            return; // градиент не задан полностью — ничего не рисуем
        }
        List<GradientStrips.Strip> strips = GradientStrips.vertical(geometry.getWidth(), geometry.getHeight());
        if (strips.isEmpty()) {
            return;
        }

        debugUIElement.getShader().setUniform("useTexture", false);
        for (int i = 0; i < strips.size(); i++) {
            GradientStrips.Strip strip = strips.get(i);
            float t = (strip.count() > 1) ? (float) strip.index() / (strip.count() - 1f) : 0f;

            UiSpaceMatrices.quadMatrix(mvpMatrix, left, right, bottom, top,
                    absX + geometry.getWidth() / 2f, absY + strip.y() + strip.height() / 2f,
                    geometry.getWidth(), strip.height());
            debugUIElement.getShader().setUniform("mvp", mvpMatrix);
            debugUIElement.getShader().setUniform("backgroundColor", mixedColorVector(topColor, botColor, t));
            glDrawElements(GL_TRIANGLES, debugUIElement.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);
        }
    }

    /** Линейная интерполяция двух цветов в векторе uniform'а backgroundColor. */
    private static Vector4f mixedColorVector(IColor a, IColor b, float t) {
        int ar = lerpChannel(a.getRed(), b.getRed(), t);
        int ag = lerpChannel(a.getGreen(), b.getGreen(), t);
        int ab = lerpChannel(a.getBlue(), b.getBlue(), t);
        int aa = lerpChannel(a.getAlpha(), b.getAlpha(), t);
        return new Vector4f(ar / 255f, ag / 255f, ab / 255f, aa / 255f);
    }

    private static int lerpChannel(int from, int to, float t) {
        int value = (int) Math.round(from + (to - from) * t);
        return Math.max(0, Math.min(255, value));
    }

}
