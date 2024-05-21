package com.xamlo.core.engine.graphics.renderers;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

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
import com.xamlo.core.engine.graphics.api.gui.IResizable;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.components.GraphicalMesh;
import com.xamlo.core.engine.graphics.components.ShaderProgram;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.font.CharacterData;
import com.xamlo.core.engine.graphics.font.FontAtlas;
import com.xamlo.core.engine.graphics.font.GlyphPage;
import com.xamlo.core.engine.graphics.font.UnicodeGlyphFont;
import com.xamlo.core.engine.graphics.opengl.blend.EnumOpenglBlendMode;
import com.xamlo.core.engine.graphics.opengl.blend.OpenGLBlend;

public class TextElementRenderer {

    private UnicodeGlyphFont custom;
    private FontAtlas atlas;
    private ShaderProgram textShader;
	private Matrix4f mvpMatrix;
	
	public void initFonts() {
		
		mvpMatrix = new Matrix4f();
        
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

	
	public void draw(AbstractUIElement debugUIElement, IUIElement element, IScene scene) {
		
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