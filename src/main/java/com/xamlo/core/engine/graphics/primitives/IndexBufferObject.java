package com.xamlo.core.engine.graphics.primitives;

import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryUtil;

public class IndexBufferObject extends AbstractVertexBuffer {

	/**
	 * Копия данных, которые должны быть положены в VRAM
	 * Возможно их стоило убрать из озу совсем, но ладно
	 */
	
	public int[] indexData;
	
	public IndexBufferObject(int[] indexData) {
		this(indexData, EnumMemoryType.STATIC);
	}

	public IndexBufferObject(int[] indexData, EnumMemoryType memoryType) {
		this.indexData = indexData;
		this.bufferType = memoryType;
		bufferID = glGenBuffers();
	}
	
	@Override
	public boolean allocMemory(int indexVBO) {
		if (isRegistred) {
			//FIXME: Наверное лучше кинуть своё исключение
			return false;
		}
		this.isRegistred = true;
		
	    IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indexData.length);
	    indicesBuffer.put(indexData);
	    indicesBuffer.flip();

	    /**
		  * Указываем OpenGL, что нужно переключиться на область памяти с индексом bufferID
		  */
	    glBindBuffer(EnumBufferType.ElementBuffer.getOpenGLValue(), bufferID);
		/**
		 * Перемещаем в выбранную область памяти массив из ОЗУ
		 */
		glBufferData(EnumBufferType.ElementBuffer.getOpenGLValue(), indicesBuffer, bufferType.getOpenGLValue());
		
//		glEnableVertexAttribArray(0);
		
		MemoryUtil.memFree(indicesBuffer);


		return true;
	}


}
