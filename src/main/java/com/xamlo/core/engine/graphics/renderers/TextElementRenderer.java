package com.xamlo.core.engine.graphics.renderers;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.gui.AbstractUIElement;
import com.xamlo.core.engine.graphics.api.gui.IBackgroundSupport;
import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.IResizable;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.api.gui.elements.ILabel;
import com.xamlo.core.engine.graphics.api.gui.font.IFont;
import com.xamlo.core.engine.graphics.api.gui.font.IFontSupport;
import com.xamlo.core.engine.graphics.components.GraphicalMesh;
import com.xamlo.core.engine.graphics.components.ShaderProgram;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.font.CharacterData;
import com.xamlo.core.engine.graphics.font.FontAtlas;
import com.xamlo.core.engine.graphics.font.GlyphPage;
import com.xamlo.core.engine.graphics.font.ApplicationFont;
import com.xamlo.core.engine.graphics.font.UnicodeGlyphFont;
import com.xamlo.core.engine.graphics.fontsystem.FontSystem;
import com.xamlo.core.engine.graphics.opengl.blend.EnumOpenglBlendMode;
import com.xamlo.core.engine.graphics.opengl.blend.OpenGLBlend;

public class TextElementRenderer {

    private UnicodeGlyphFont custom;
    private FontAtlas atlas;
    private ShaderProgram textShader;
	private Matrix4f mvpMatrix;
	
    private FontSystem fontSystem;

    static final ApplicationFont DEFAULT_FONT_KEY = new ApplicationFont("Arial", 12, false, false);
    private final Map<String, GlyphSet> glyphSets = new HashMap<>();
    private GlyphSet defaultGlyphSet;
    private boolean debugWidgetNames = false;

    public void setDebugWidgetNames(boolean enabled) {
    	this.debugWidgetNames = enabled;
    }

    public boolean isDebugWidgetNames() {
    	return this.debugWidgetNames;
    }

    AbstractUIElement debugUIElement;

    private static final class GlyphSet {
        final UnicodeGlyphFont font;
        final FontAtlas atlas;

        GlyphSet(UnicodeGlyphFont font, FontAtlas atlas) {
            this.font = font;
            this.atlas = atlas;
        }
    }
	
	public void init() {
		
		this.debugUIElement = new AbstractUIElement() {
			//NO-OP DEBUG
		};
        debugUIElement.init();
        
        this.fontSystem = FontSystem.getInstance();

        this.initFonts();
        
	}
	
	public void release() {
		debugUIElement.release();		
	}

	
	private void initFonts() {
		
		mvpMatrix = new Matrix4f();
        
        
        // Register fonts through FontSystem
        ApplicationFont fontKey = new ApplicationFont("Arial", 12, false, false);
        if (!fontSystem.isFontRegistered(fontKey)) {
            Font awtFont = new Font("Arial", Font.PLAIN, 12);
            this.custom = fontSystem.registerFont(fontKey, awtFont);
        } else {
            this.custom = fontSystem.getFont(fontKey);
        }
        
		this.atlas = new FontAtlas(this.custom);
        this.defaultGlyphSet = new GlyphSet(this.custom, this.atlas);
        glyphSets.put(describe(fontKey), defaultGlyphSet);
    }

    private UnicodeGlyphFont ensureFont(ApplicationFont key) {
    	if (!fontSystem.isFontRegistered(key)) {
            int style = Font.PLAIN | (key.isBold() ? Font.BOLD : 0) | (key.isItalic() ? Font.ITALIC : 0);
            return fontSystem.registerFont(key, new Font(key.getFontFamily(), style, key.getFontSize()));
        }
        return fontSystem.getFont(key);
    }

    private GlyphSet resolveGlyphSet(IUIElement element) {
    	ApplicationFont key = determineFontKey(element);
        String id = describe(key);
        GlyphSet set = glyphSets.get(id);
        if (set == null) {
        	UnicodeGlyphFont font = ensureFont(key);
            if (font == null) {
                set = defaultGlyphSet;
            } else {
                set = new GlyphSet(font, new FontAtlas(font));
                glyphSets.put(id, set);
            }
        }
        return set;
    }

    private static String describe(ApplicationFont key) {
    	return key.getFontFamily() + "|" + key.getFontSize() + "|" + key.isBold() + "|" + key.isItalic();
    }

    private ApplicationFont determineFontKey(IUIElement element) {
        IFont font = (element instanceof IFontSupport) ? ((IFontSupport) element).getFont() : null;
        if (font == null || font.getFontFamily() == null) {
            return DEFAULT_FONT_KEY;
        }
        return new ApplicationFont(font.getFontFamily(), font.getFontSize(), font.isBold(), font.isItalic());
    }
    
	public void draw(IUIElement element, IScene scene) {
		

        String text;
        if (debugWidgetNames) {
        	text = element.getWidgetName();
        } else {
        	text = (element instanceof ILabel) ? ((ILabel) element).getText() : null;
        }
        if (text == null || text.isEmpty()) {
            return;
        }
        GlyphSet glyphSet = resolveGlyphSet(element);
 	
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
	        float size = drawCharacter(glyphSet, c, xCoord + width, yCoord, screenWidth, screenHeight);
	        width += size;
	    }
	    
		OpenGLBlend.disable();
	    
	    textShader.unbind();
	    
	}
	
	private float drawCharacter(GlyphSet glyphSet, char character, float xCoord, float yCoord, final float screenWidth, final float screenHeight) {
		
 		UnicodeGlyphFont font = glyphSet.font;
        GlyphPage glyphPage = font.getGlyphPage(character);

	    CharacterData characterData = glyphPage.getCharacterData(character);

		GraphicalMesh mesh = glyphSet.atlas.getGlyphMesh(character);
		
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