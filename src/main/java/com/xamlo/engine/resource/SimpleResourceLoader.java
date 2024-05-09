package com.xamlo.engine.resource;

import com.xamlo.core.engine.graphics.components.Model;
import com.xamlo.core.engine.graphics.components.Texture;
import com.xamlo.core.engine.graphics.components.attribs.NormalAttribute;
import com.xamlo.core.engine.graphics.components.attribs.PositionAttribute;
import com.xamlo.core.engine.graphics.components.attribs.TexCoordAttribute;
import com.xamlo.core.engine.graphics.primitives.Vertex;
import com.xamlo.core.engine.graphics.primitives.VertexStructure;
import com.xamlo.engine.api.resources.IShaderResource;
import com.xamlo.engine.api.resources.IVertex;

import com.xamlo.engine.api.resources.IModelResource;
import com.xamlo.engine.api.resources.IResourceLoader;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.StringJoiner;

import static org.lwjgl.stb.STBImage.*;

public class SimpleResourceLoader implements IResourceLoader<String> {

    private final Properties resourceMap;

    public SimpleResourceLoader(Properties resourceMap) {
        this.resourceMap = resourceMap;
    }

    public static SimpleResourceLoader createFromBundledList(String bundledResourceListPath) {
        try (InputStream is = SimpleResourceLoader.class.getResourceAsStream(bundledResourceListPath)) {
            Properties properties = new Properties();
            properties.load(is);
            return new SimpleResourceLoader(properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Texture loadTexture(String identifier) {
        URI resourceUri = getResourceUriByIdentifier(identifier).orElseThrow();
        IntBuffer width = BufferUtils.createIntBuffer(Integer.BYTES);
        IntBuffer height = BufferUtils.createIntBuffer(Integer.BYTES);
        IntBuffer channels = BufferUtils.createIntBuffer(Integer.BYTES);

        ByteBuffer buf;
        try (InputStream is = getResourceStream(resourceUri)) {
            if (is == null) {
                throw new IOException("Texture resource not found");
            }
            byte[] bytes = is.readAllBytes();

            buf = stbi_load_from_memory(
                    BufferUtils.createByteBuffer(bytes.length)
                            .put(bytes)
                            .flip(),
                    width,
                    height,
                    channels,
                    4
            );

            if (buf == null) {
                throw new IOException("Texture [" + resourceUri + "] not loaded: " + stbi_failure_reason());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Texture(width.get(), height.get(), buf, identifier);
    }

    @Override
    public IShaderResource<String> loadShader(String identifier) {
        URI resourceUri = getResourceUriByIdentifier(identifier).orElseThrow();
        try (InputStream is = getResourceStream(resourceUri)) {
            if (is == null) {
                throw new IOException("Specified shader resource not found.");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringJoiner stringJoiner = new StringJoiner("\n");
            String line;
            while ((line = reader.readLine()) != null) {
                stringJoiner.add(line);
            }
            return new SimpleShaderResource(identifier, stringJoiner.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
	@Override
	public IModelResource<String> loadModel(String identifier) {
        URI resourceUri = getResourceUriByIdentifier(identifier).orElseThrow();
        try (InputStream is = getResourceStream(resourceUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
        	System.out.println("loading model....");
        	List<Vector3f> positions = new ArrayList<>();
        	List<Vector2f> texCoords = new ArrayList<>();
        	List<Vector3f> normals = new ArrayList<>();
        	List<Face> faces = new ArrayList<>();

        	String line;
        	while ((line = reader.readLine()) != null) {

        		line = line.trim();
        		if (line.startsWith("v ")) {
        			// Parse vertex position
        			String[] parts = line.split("\\s+");
        			float x = Float.parseFloat(parts[1]);
        			float y = Float.parseFloat(parts[2]);
        			float z = Float.parseFloat(parts[3]);
        			positions.add(new Vector3f(x, y, z));
        		} else if (line.startsWith("vt ")) {
        			// Parse texture coordinate
        			String[] parts = line.split("\\s+");
        			float u = Float.parseFloat(parts[1]);
        			float v = Float.parseFloat(parts[2]);
        			texCoords.add(new Vector2f(u, v));
        		} else if (line.startsWith("vn ")) {
        			// Parse normal
        			String[] parts = line.split("\\s+");
        			float x = Float.parseFloat(parts[1]);
        			float y = Float.parseFloat(parts[2]);
        			float z = Float.parseFloat(parts[3]);
        			normals.add(new Vector3f(x, y, z));
        		} else if (line.startsWith("f ")) {
        			// Parse face
        			faces.add(new Face(line.substring(2).trim()));
        		}

        	}
        	System.out.println("sucessfull!");

        	return createModelFromObjData(identifier, positions, texCoords, normals, faces);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

	}
	

    private IModelResource<String> createModelFromObjData(String identifier, 
            List<Vector3f> positions, List<Vector2f> texCoords, 
            List<Vector3f> normals, List<Face> faces) {
        
        // Determine vertex structure based on available data
        VertexStructure structure = new VertexStructure();
        
        structure.addAttribute(new PositionAttribute());

        boolean hasTexCoords = !texCoords.isEmpty();
        if (hasTexCoords) {
            structure.addAttribute(new TexCoordAttribute());
        }

        boolean hasNormals = !normals.isEmpty();
        if (hasNormals) {
            structure.addAttribute(new NormalAttribute());
        }
        
        // Create vertices and indices
        Map<String, Integer> vertexIndexMap = new HashMap<>();
        List<IVertex> vertexList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();
        
        // Process faces in order and preserve the original index sequence
        for (Face face : faces) {
            for (String vertexData : face.vertices) {
                Integer existingIndex = vertexIndexMap.get(vertexData);
                
                if (existingIndex == null) {
                    // Create new vertex and add to list
                    IVertex vertex = parseVertexData(vertexData, positions, texCoords, normals, hasTexCoords, hasNormals);
                    int newIndex = vertexList.size();
                    vertexIndexMap.put(vertexData, newIndex);
                    vertexList.add(vertex);
                    indexList.add(newIndex);
                } else {
                    // Use existing vertex index
                    indexList.add(existingIndex);
                }
            }
        }
        structure.setVertexCount(vertexList.size());

        // Convert to arrays
        IVertex[] vertices = vertexList.toArray(new IVertex[0]);
        int[] indices = indexList.stream().mapToInt(i -> i).toArray();
        
        // Create model
        Model model = new Model(identifier, structure, vertices, indices, faces.size());
        
        return model;
    }

    private IVertex parseVertexData(String vertexData, List<Vector3f> positions, List<Vector2f> texCoords, List<Vector3f> normals, boolean hasTexCoords, boolean hasNormals) {
        
        String[] parts = vertexData.split("/");

        // OBJ индексы начинаются с 1, а наши массивы с 0
        int posIndex = Integer.parseInt(parts[0]) - 1;
        
        int vertexSize = 3;
        if (hasTexCoords) {
        	vertexSize +=2;
        }
        if (hasNormals) {
        	vertexSize +=3;
        }
        // Capacity for position + texcoord + normal
        Vertex vertex = new Vertex(vertexSize); 
        
        // Add position
        Vector3f position = positions.get(posIndex);
        vertex.append(position);
        
        // Add texture coordinate if available
        if (hasTexCoords && parts.length > 1 && !parts[1].isEmpty()) {
            int texIndex = Integer.parseInt(parts[1]) - 1;
            Vector2f texCoord = texCoords.get(texIndex);
            vertex.append(texCoord);
        }
        
        // Add normal if available
        if (hasNormals && parts.length > 2 && !parts[2].isEmpty()) {
            int normIndex = Integer.parseInt(parts[2]) - 1;
            Vector3f normal = normals.get(normIndex);
            vertex.append(normal);
        }
        
        return vertex;
    }

    // Helper class to represent OBJ face data
    private static class Face {
        String[] vertices;
        
        Face(String faceData) {
            // Split on whitespace
            vertices = faceData.split("\\s+");
            
            // Handle different OBJ face formats: v, v/t, v/t/n, v//n
            for (int i = 0; i < vertices.length; i++) {
                String vertex = vertices[i];
                String[] parts = vertex.split("/");
                
                // Ensure we have exactly 3 parts (pad with empty strings if needed)
                if (parts.length < 3) {
                    String[] newParts = new String[3];
                    System.arraycopy(parts, 0, newParts, 0, parts.length);
                    for (int j = parts.length; j < 3; j++) {
                        newParts[j] = "";
                    }
                    vertices[i] = String.join("/", newParts);
                }
            }
        }
    }
	
    private Optional<URI> getResourceUriByIdentifier(String identifier) {
        return Optional.ofNullable(resourceMap.getProperty(identifier))
                .map(URI::create);
    }

    private static InputStream getResourceStream(URI uri) throws IOException {
        return switch (uri.getScheme()) {
            case "file" -> Files.newInputStream(Path.of(uri));
            case "resource" -> SimpleResourceLoader.class.getResourceAsStream(uri.getPath());
            default -> throw new IllegalStateException("Unexpected value: " + uri.getScheme());
        };
    }


}
