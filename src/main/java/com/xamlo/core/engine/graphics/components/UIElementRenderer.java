package com.xamlo.core.engine.graphics.components;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.gui.AbstractUIElement;
import com.xamlo.core.engine.graphics.api.gui.IBackgroundSupport;
import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.IHoverable;
import com.xamlo.core.engine.graphics.api.gui.IResizable;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.font.CharacterData;
import com.xamlo.core.engine.graphics.font.FontAtlas;
import com.xamlo.core.engine.graphics.font.GlyphPage;
import com.xamlo.core.engine.graphics.font.UnicodeGlyphFont;
import com.xamlo.core.engine.graphics.opengl.blend.EnumOpenglBlendMode;
import com.xamlo.core.engine.graphics.opengl.blend.OpenGLBlend;
import com.xamlo.core.engine.graphics.opengl.depth.EnumOpenGLDepthMode;
import com.xamlo.core.engine.graphics.opengl.depth.OpenGLDepth;



public class UIElementRenderer {
	
	private Matrix4f mvpMatrix = new Matrix4f();
    private UnicodeGlyphFont custom;
    private FontAtlas atlas;
    private ShaderProgram textShader;

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
	    
        boolean hasTexture = element instanceof IBackgroundSupport && ((IBackgroundSupport)element).hasBackgroundImage();
        debugUIElement.getShader().setUniform("useTexture", hasTexture);

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
	    
		if (element instanceof IBackgroundSupport && ((IBackgroundSupport)element).hasBackgroundImage()) {
			glActiveTexture(GL_TEXTURE0);
			((IBackgroundSupport)element).getBackgroundImage().bind();
		}

		OpenGLBlend.enable();
		OpenGLBlend.setMode(EnumOpenglBlendMode.SRC_ALPHA, EnumOpenglBlendMode.ONE_MINUS_SRC_ALPHA);
        
		
		/**
		 * mode: Задает примитивы для рендеринга, в данном случае треугольники. Здесь никаких изменений.
		 * count: Указывает количество элементов, которые должны быть отрисованы.
		 * type: Указывает тип значения в данных индексов. В данном случае мы используем целые числа.
		 * indices: Задает смещение, которое необходимо применить к данным индексов для начала рендеринга.
		 */
		glDrawElements(GL_TRIANGLES, debugUIElement.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);

		OpenGLBlend.disable();
		
		// На самом деле разбинживать меш вовсе не обязательно, но я так хочу
		debugUIElement.getMesh().unbind();

		// На самом деле разбинживать шейдер вовсе не обязательно, но я так хочу
		debugUIElement.getShader().unbind();

	}
	
	public void initFonts() {
        
        File fontFile = new File("C:\\workspace\\eclipse\\gamedev\\engine-graphics\\src\\main\\resources\\fonts\\Roboto-Bold.ttf");
        Font baseFont;
		try {
			
			baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
			baseFont = baseFont.deriveFont(24f);
			this.custom = new UnicodeGlyphFont(baseFont);

            
		} catch (FontFormatException e) {
			e.printStackTrace();
			this.custom = new UnicodeGlyphFont(new Font("Times New Roman", Font.PLAIN, 30));
		} catch (IOException e) {
			e.printStackTrace();
			this.custom = new UnicodeGlyphFont(new Font("Times New Roman", Font.PLAIN, 30));
		}

		
        
    	
        GlyphPage glyphPage = this.custom.getGlyphPage('A');
        
        CharacterData characterData = glyphPage.getCharacterData('A');
        
        System.out.println("size A: " +  characterData.getWidth() + "|" + characterData.getHeight());
        
		this.atlas = new FontAtlas(this.custom);
    }
	
	public void renderText(AbstractUIElement debugUIElement, IUIElement element, IScene scene) {
		
	    if (element.getWidgetName() == null || element.getWidgetName().isEmpty()) {
	        return;
	    }
	    
	    String text = element.getWidgetName();

	    
	    this.textShader = debugUIElement.getShader();

	    UIElementGeometry geometry = ((IResizable)element).getGeometry();
		
	    final float screenWidth = 1920.0f;
	    final float screenHeight = 1080.0f;

        // Активируем шейдер текста
        textShader.bind();

        
        Vector4f bgColorVector;
        if (element instanceof IBackgroundSupport) {
            IColor bgColor = ((IBackgroundSupport)element).getBackgroundColor();
            if (bgColor != null) {
                bgColorVector = bgColor.getColorVector();
            } else {
                bgColorVector = new Vector4f(1, 1, 1, 1);
            }
        } else {
            bgColorVector = new Vector4f(1, 1, 1, 1);
        }
        
        textShader.setUniform("backgroundColor", bgColorVector);
        textShader.setUniform("useTexture", true);
        textShader.setUniform("hoverColor", bgColorVector);
        textShader.setUniform("hoverIntensity", 0.3f);
        
        float xCoord = geometry.getXCoord() + 40;
        float yCoord = geometry.getYCoord() + geometry.getHeight() - 5;
        
		OpenGLBlend.enable();
		OpenGLBlend.setMode(EnumOpenglBlendMode.SRC_ALPHA, EnumOpenglBlendMode.ONE_MINUS_SRC_ALPHA);
        
        float width = 0;
        
        for (char c : text.toCharArray()) {
            float size = drawCharacter(this.custom, c, xCoord + width, yCoord, screenWidth, screenHeight);
            width += size;
        }
        
		OpenGLBlend.disable();
        
        textShader.unbind();
        
	}

    private float drawCharacter(UnicodeGlyphFont font, char character, float xCoord, float yCoord, final float screenWidth, final float screenHeight) {
    	
        GlyphPage glyphPage = font.getGlyphPage(character);
        
        CharacterData characterData = glyphPage.getCharacterData(character);
	    
    	GraphicalMesh mesh = this.atlas.getGlyphMesh(character);
    	
	    final float left = 0;
	    final float right =  screenWidth;
	    
	    final float bottom = screenHeight;
	    final float top =  0.0f;
	    
	    final float near = 0.2f;
	    final float far = 1000.0f;
	    
	    float Tx = xCoord;
	    float Ty = yCoord;
	    float Tz = -1.0f;

		mvpMatrix.zero()
				.setOrtho(left, right, bottom, top, near, far)
				.translate(Tx, Ty, Tz)
				.scale(characterData.getWidth(), -characterData.getHeight(), 1.0f);

        System.out.println("render glyph: " + character + ", "
        		+ "\t pos: (" + xCoord + ": " + yCoord + "),"
                + "\t size: " + characterData.getWidth() + "\t" +(int)  characterData.getHeight());
        
        textShader.setUniform("mvp", mvpMatrix);
    	
        mesh.bind();

        glActiveTexture(GL_TEXTURE0);
        glyphPage.getTexture().bind();

		glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
		
        mesh.unbind();
        return characterData.getWidth();
    }

}
