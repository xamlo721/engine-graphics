package com.xamlo.core.engine.graphics.components;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL45.*;

/**
 * 
 * @author Satomi
 * 
 * The constructor of the ShaderProgram creates a new program in OpenGL 
 * and provides methods to add vertex and fragment shaders. 
 * Those shaders are compiled and attached to the OpenGL program.
 *  When all shaders are attached the link method should be invoked which 
 *  links all the code and verifies that everything has been done correctly.
 *  
 *  https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter04/chapter4.html
 */
public class ShaderProgram {
	
	private int program;
	private int vertexShaderID;
	private int geometryShaderID;
	private int fragmentShaderID;
	private int tessellationControlShaderID;
	private int tesselationEvaluationShaderID;
	private int computeShaderID;

	private final Map<String, Integer> uniforms = new HashMap<>();
	
	public ShaderProgram() {
		program = glCreateProgram();
		
		if (program == 0) {
			System.err.println("Shader creation failed");
			System.exit(1);
		}	
		
		vertexShaderID = 0;
		geometryShaderID = 0;
		fragmentShaderID = 0;
		tessellationControlShaderID = 0;
		tesselationEvaluationShaderID = 0;
		computeShaderID = 0;
		System.out.println("Register Shader program id: " + program);

	}
	
	/**
	 * Привязывает текущий шейдер к контексту 
	 * (дальше рисоваться всё будет с этим шейдером)
	 */
	public void bind() {
		glUseProgram(program);
	}
	
	/**
	 * Отвязывает шейдер от текущего контекста
	 * Перед выходом из программы надо ещё вызвать cleanUP();
	 * для корректного завершения 
	 */
	public void unbind() {
		glUseProgram(0);
	}
	
	public void addVertexShader(String text) {
		vertexShaderID = addProgram(text, GL_VERTEX_SHADER);
	}
	
	public void addGeometryShader(String text) {
		geometryShaderID = addProgram(text, GL_GEOMETRY_SHADER);
	}
	
	public void addFragmentShader(String text) {
		fragmentShaderID = addProgram(text, GL_FRAGMENT_SHADER);
	}
	
	public void addTessellationControlShader(String text) {
		tessellationControlShaderID = addProgram(text, GL_TESS_CONTROL_SHADER);
	}
	
	public void addTessellationEvaluationShader(String text) {
		tesselationEvaluationShaderID = addProgram(text, GL_TESS_EVALUATION_SHADER);
	}
	
	public void addComputeShader(String text) {
		computeShaderID = addProgram(text, GL_COMPUTE_SHADER);
	}
	
	/**
	 * Приватный метод для регистрации этапов конвейера 
	 * Регистрирует отдельные шейдерные программы
	 * @param text Шейдер
	 * @param type Тип шейдера
	 * @return ID зарегистрированного шейдера в openGL
	 */
	private int addProgram(String text, int type) {
		
		int shader = glCreateShader(type);
		System.out.println("Register Shader part id : " + shader);

		if (shader == 0) {
			System.err.println(this.getClass().getName() + " Shader creation failed");
			System.exit(1);
		}	
		
		glShaderSource(shader, text);
		glCompileShader(shader);
		
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
			System.err.println(this.getClass().getName() + " " + glGetShaderInfoLog(shader, 1024));
			System.exit(1);
		}
		
		glAttachShader(program, shader);
		return shader;
	}

	/**
	 * Когда все этапы шейдера добавлены в конвейер
	 * шейдерная программа может быть скомпилирована
	 * (В разных источниках называется линковка)
	 * 
	 * После чего она не может быть изменена, но может быть удалена
	 * 
	 * Если нужно изменить этап конвейера, нужно создать новый ShaderProgram объект
	 * 
	 * Чистит за собой все отдельно лежащие шейдеры
	 * 
	 */
	public void compileShader() {
		
		glLinkProgram(program);

		if(glGetProgrami(program, GL_LINK_STATUS) == 0) {
			System.out.println(this.getClass().getName() + " " + glGetProgramInfoLog(program, 1024));
			System.exit(1);
		}
		
	    if (vertexShaderID != 0) {
	    	  glDetachShader(program, vertexShaderID);
		}
		if (geometryShaderID != 0) {
		      glDetachShader(program, geometryShaderID);
		}
		if (fragmentShaderID != 0) {
		      glDetachShader(program, fragmentShaderID);
		}		  
		if (tessellationControlShaderID != 0) {
		      glDetachShader(program, tessellationControlShaderID);
		}		  
		if (tesselationEvaluationShaderID != 0) {
		      glDetachShader(program, tesselationEvaluationShaderID);
		}		  
		if (computeShaderID != 0) {
		      glDetachShader(program, computeShaderID);
		}
		
		glValidateProgram(program);
		
		if(glGetProgrami(program, GL_VALIDATE_STATUS) == 0) {
			System.err.println(this.getClass().getName() +  " " + glGetProgramInfoLog(program, 1024));
			System.exit(1);
		}
		
		System.out.println("Compile Shader program id: " + program);

	}
	

	public void createUniform(String uniformName) throws Exception {
	    int uniformLocation = glGetUniformLocation(program, uniformName);
	    if (uniformLocation < 0) {
	        throw new Exception("Could not find uniform:" + uniformName);
	    }
	    uniforms.put(uniformName, uniformLocation);
	}

	public void setUniform(String uniformName, Matrix4f value) {
		
	    // Dump the matrix into a float buffer
	    try (MemoryStack stack = MemoryStack.stackPush()) {
	        FloatBuffer fb = stack.mallocFloat(16);
	        value.get(fb);
	        
			
//			System.out.println("FloatBuffer matrix " + uniformName);
//			System.out.println("{" + fb.get(0) + ", " + fb.get(1) + ", " + fb.get(2) + ", " + fb.get(3) + "}");
//			System.out.println("{" + fb.get(4) + ", " + fb.get(5) + ", " + fb.get(6) + ", " + fb.get(7) + "}");
//			System.out.println("{" + fb.get(8) + ", " + fb.get(9) + ", " + fb.get(10) + ", " + fb.get(11) + "}");
//			System.out.println("{" + fb.get(12) + ", " + fb.get(13) + ", " + fb.get(14) + ", " + fb.get(15) + "}");

	        glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
	        
//			System.out.println("updating matrix " + uniformName);
//			System.out.println("{" + value.m00() + ", " + value.m01() + ", " + value.m02() + ", " + value.m03() + "}");
//			System.out.println("{" + value.m10() + ", " + value.m11() + ", " + value.m12() + ", " + value.m13() + "}");
//			System.out.println("{" + value.m20() + ", " + value.m21() + ", " + value.m22() + ", " + value.m23() + "}");
//			System.out.println("{" + value.m30() + ", " + value.m31() + ", " + value.m32() + ", " + value.m33() + "}");

//			float[] arr = new float[16];
//			glUseProgram(program);
//			glGetUniformfv(this.program, uniforms.get(uniformName), arr);
//			
//			System.out.println("returned matrix " + uniformName);
//			System.out.println("{" + arr[0] + ", " + arr[1] + ", " + arr[2] + ", " + arr[3] + "}");
//			System.out.println("{" + arr[4] + ", " + arr[5] + ", " + arr[6] + ", " + arr[7] + "}");
//			System.out.println("{" + arr[8] + ", " + arr[9] + ", " + arr[10] + ", " + arr[11] + "}");
//			System.out.println("{" + arr[12] + ", " + arr[13] + ", " + arr[14] + ", " + arr[15] + "}");

	    }
	    
	}

    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }
    
	
	/**
	 * Полная очистка шейдера, по сути деструктор
	 * Возможно в будущем переедет в него
	 * 
	 * После вызова cleanup() объект уже невозможно использовать
	 */
    public void cleanup() {
        unbind();
        if (program != 0) {
            glDeleteProgram(program);
        }
    }

	
}
