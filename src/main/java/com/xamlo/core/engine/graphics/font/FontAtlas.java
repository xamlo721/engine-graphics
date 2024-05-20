package com.xamlo.core.engine.graphics.font;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.xamlo.core.engine.graphics.components.GraphicalMesh;
import com.xamlo.core.engine.graphics.components.attribs.PositionAttribute;
import com.xamlo.core.engine.graphics.components.attribs.TexCoordAttribute;
import com.xamlo.core.engine.graphics.primitives.Vertex;
import com.xamlo.core.engine.graphics.primitives.VertexStructure;
import com.xamlo.engine.api.resources.IVertex;

public class FontAtlas {
	
	private Map<Character, GraphicalMesh> glyphModels;
	
	private static int[] indices = {
			0, 1, 3,
			3, 1, 2
	};
	private static VertexStructure vertexScruct = new VertexStructure();

	static {
    	vertexScruct.addAttribute(new PositionAttribute());
    	vertexScruct.addAttribute(new TexCoordAttribute());
    	vertexScruct.setVertexCount(4);
	}
	
	public FontAtlas(UnicodeGlyphFont font) {
		this.glyphModels = new HashMap<Character, GraphicalMesh>();
		this.generateAtlasModels(font);
	}
	
	public GraphicalMesh getGlyphMesh(char c) {
		return this.glyphModels.get(c);
	}
	
	public void generateAtlasModels(UnicodeGlyphFont font) {
		
		String text = "!\"#$%&'()*+,-./0"
				+ "123456789:;<=>?@ABCDEFGHILJKLM"
				+ "NOPQRSTUVWXYZ[\\]^_`abcdefghijkl"
				+ "mnopqrstuvwxyz{|}~";
		
		for (int i = 0; i < text.length(); i++) {
			
			final char symbol = text.charAt(i);
			
			GraphicalMesh symbolMesh = createCharMesh(font, symbol);
			
			this.glyphModels.put(symbol, symbolMesh);
		}
		
	}
	
	private static GraphicalMesh createCharMesh(UnicodeGlyphFont font,  char character) {
    	
        GlyphPage glyphPage = font.getGlyphPage(character);
        
        CharacterData characterData = glyphPage.getCharacterData(character);

        float x = characterData.getX();
        float y = characterData.getY();
        float w = characterData.getWidth();
        float h = characterData.getHeight();
        
        float texX = characterData.getX() / (float) font.getImageSize();
        float texY = characterData.getY() / (float) font.getImageSize();
        float texW = w / (float) font.getImageSize();
        float texH = h / (float) font.getImageSize();
        
    	IVertex v1 = new Vertex(5).append(new Vector3f( 0,  1,  0.0f)).append(new Vector2f(texX,        texY)); //V1
    	IVertex v2 = new Vertex(5).append(new Vector3f( 0,  0,  0.0f)).append(new Vector2f(texX,        texY + texH));        //V2
    	IVertex v3 = new Vertex(5).append(new Vector3f( 1,  0,  0.0f)).append(new Vector2f(texX + texW, texY + texH));        //V3
    	IVertex v4 = new Vertex(5).append(new Vector3f( 1,  1,  0.0f)).append(new Vector2f(texX + texW, texY)); //V4
		
        System.out.println("Register glyph: " + character + ", "
        		+ "\t pos: (" + (int) x + ": " + (int) y + "),"
                + "\t size: " + (int) w + "x" +(int)  h +
                "),\t texture start [" +  x + ":" +  y + "]" +
                "  \t stop [" +(int) (x + w) + ":" + (int) (y + h) + "]");
    	
    	IVertex[] vertices = new Vertex[4];
    	vertices[0] = v1;
    	vertices[1] = v2;
    	vertices[2] = v3;
    	vertices[3] = v4;
    	
        GraphicalMesh mesh = new GraphicalMesh(vertices, vertexScruct, indices);

    	v1.release();
    	v2.release();
    	v3.release();
    	v4.release();
    	
		return mesh;
	}

}
