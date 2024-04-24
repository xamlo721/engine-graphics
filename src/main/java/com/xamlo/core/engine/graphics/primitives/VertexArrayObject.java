package com.xamlo.core.engine.graphics.primitives;

import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.HashMap;
import java.util.Map;

import com.xamlo.core.engine.graphics.api.primitives.IBufferObject;

/**
 * Согласно документации Opengl, под VAO понимают список VBO
 * Причём такой список, который относится к одному объекту
 * По сути, VBO в таком случае являются примитивными хранилищами атрибутов
 * таких как координаты, цвета, текстуры и т.д
 * А VAO интерпретирует VBO как буферы одного конкретного элемента
 * @author Satomi
 *
 */
public class VertexArrayObject extends AbstractVertexBuffer {
	/**
	 * Флаг, сообщающий о том, что данный буфер уже был размещён в VRAM
	 * Для такого объекта можно вызвать release, но нельзя вызвать bind
	 */
	private boolean isRegistred;
			
	/**
	 * Целочисленный ID области памяти (буфера) в VRAM видеокарты
	 * Устанавливается какое-то значение методом glBindVertexArray
	 */
	private int vaoId;
	/**
	 * Список VBO, сходящих в состав VAO
	 */
	private Map<Integer, AbstractVertexBuffer> vbos;

	private IndexBufferObject indexBufferObject;
	
	public VertexArrayObject()  {
		vaoId = glGenVertexArrays();
		vbos = new HashMap<Integer, AbstractVertexBuffer>();
	}
	
	public void addVertexData(int bufferLocation, AbstractVertexBuffer buffer) {
		this.vbos.put(bufferLocation, buffer);
	}
	
	public void addVertexData(AbstractVertexBuffer buffer) {
		this.vbos.put(vbos.size(), buffer);
	}
	
	public void addVertexData(int bufferLocation, float[] buffer, EnumMemoryType memoryType, EnumDimestionSize dataDimension) {
		this.vbos.put(bufferLocation, new VertexAttribBufferObject(buffer, memoryType, dataDimension));
	}
	
	public void addVertexData(float[] buffer, EnumMemoryType memoryType, EnumDimestionSize dataDimension) {
		this.vbos.put(vbos.size(), new VertexAttribBufferObject(buffer, memoryType, dataDimension));
	}
	
	public void setVertexOrder(int[] vertexOrder) {
		indexBufferObject = new IndexBufferObject(vertexOrder);
	}

	@Override
	public boolean allocMemory(int indexVBO) {
		if (isRegistred) {
			//FIXME: Наверное лучше кинуть своё исключение
			return false;
		}
		this.isRegistred = true;
		///
        glBindVertexArray(vaoId);
		indexBufferObject.allocMemory(0);
		
	    //FIME: Эти индексы не соответсвуют реальному расположению буферов на видеокарте
		for (int i = 0; i < vbos.size(); i ++) {
			AbstractVertexBuffer vbo = vbos.get(i);
			vbo.allocMemory(this.attribArrayIndex);
			this.attribArrayIndex += vbo.getAttribsCount();
		}
		glBindVertexArray(0);

		return true;
	}
	
	@Override
	public void bind() {
	    glBindVertexArray(vaoId);
		for (int i = 0; i < this.attribArrayIndex; i++) {
		    glEnableVertexAttribArray(i);
		}
	}
	
	@Override
	public void unbind() {
		for (int i = 0; i < this.attribArrayIndex; i++) {
			glDisableVertexAttribArray(i);
		}	
	    glBindVertexArray(0);
	}
	
	@Override
	public boolean releaseMemory() {
		if (!isRegistred) {
			//FIXME: Наверное лучше кинуть своё исключение
			return false;
		}
		this.isRegistred = false;
		
		glBindVertexArray(vaoId);
		indexBufferObject.releaseMemory();
		for (IBufferObject vbo : vbos.values()) {
			vbo.releaseMemory();
		}
		glDeleteVertexArrays(vaoId);
		this.attribArrayIndex = 0;
		return true;
	}



}
