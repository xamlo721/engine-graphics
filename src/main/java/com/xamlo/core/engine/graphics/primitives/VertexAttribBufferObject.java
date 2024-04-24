package com.xamlo.core.engine.graphics.primitives;

import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryUtil;


import static org.lwjgl.opengl.GL15.glBufferData;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL11.GL_FLOAT;


/**
 * Объект вершинного буфера, который может хранить в VRAM
 * данные одного типа об одной или нескольки вершинах
 * Класс является отображением примитивного фрагмента сырой памяти VRAM/
 * хранит указатель и копию памяти VRAM
 * @author Satomi
 *
 */
public class VertexAttribBufferObject extends AbstractVertexBuffer {
	
	/**
	 * Копия данных, которые должны быть положены в VRAM
	 * Возможно их стоило убрать из озу совсем, но ладно
	 */
	public float[] vertexData;

	/**
	 * Размерность пространства, координаты которого мы запоминаем в буфере.
	 * 		Для 2D - 2
	 * 		Для 3D - 3 
	 * и т.д
	 */
	private EnumDimestionSize dimensionSize;
	
	public VertexAttribBufferObject(float[] vertexData) {
		this(vertexData, EnumMemoryType.STATIC);
	}

	public VertexAttribBufferObject(float[] vertexData, EnumMemoryType memoryType) {
		this(vertexData, memoryType, EnumDimestionSize._3D);
	}
	
	public VertexAttribBufferObject(float[] vertexData, EnumDimestionSize dimensionSize) {
		this(vertexData, EnumMemoryType.STATIC, EnumDimestionSize._3D);
	}
	
	public VertexAttribBufferObject(float[] vertexData, EnumMemoryType memoryType, EnumDimestionSize dimensionSize) {
		this.vertexData = vertexData;
		this.bufferType = memoryType;
		this.dimensionSize = dimensionSize;
		this.bufferID = glGenBuffers();
	}
	
	@Override
	public boolean allocMemory(int indexVBO) {
		if (isRegistred) {
			//FIXME: Наверное лучше кинуть своё исключение
			return false;
		}
		this.isRegistred = true;
		
	    FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertexData.length);
	    vertexBuffer.put(vertexData);
	    vertexBuffer.flip();

		/**
		 * Указываем OpenGL, что нужно переключиться на область памяти с индексом bufferID
		 */
		glBindBuffer(EnumBufferType.VertexBuffer.getOpenGLValue(), bufferID);
		/**
		 * Перемещаем в выбранную область памяти массив из ОЗУ
		 */
		glBufferData(EnumBufferType.VertexBuffer.getOpenGLValue(), vertexData, bufferType.getOpenGLValue());
		/**
		 * Чистим за собой память. Она НЕ почистится gc, так как расположена не на стеке
		 */
	    MemoryUtil.memFree(vertexBuffer);

		/**
		 * Для того, чтобы не мешать другим буферам, устанавливаем глобальный для VAO индекс аттрибута
		 * Т.к мы не единственный VBO в входящий в VAO, то нужно учитывать индекс последнего помеченного аттрибута
		 * Контроль за этим возложен на VAO. Сюда должен приехать актуальный индекс
		 */
		this.attribArrayIndex = indexVBO;
		
        glEnableVertexAttribArray(this.attribArrayIndex);
	    
        /**
          *  index: Указывает местоположение, в котором шейдер ожидает эти данные.
          * 
          *  size: Задает количество компонентов для каждого атрибута вершины (от 1 до 4). 
          *  В данном случае мы передаем 3D-координаты, поэтому их должно быть 3.
          *  
          *  type: Указывает тип каждого компонента в массиве, в данном случае float.
          *  
          *  normalized: Указывает, должны ли значения быть нормализованы или нет.
          *  
          *  stride: Задает смещение в байтах между последовательными общими атрибутами вершины. 
          *  
          *  offset: Задает смещение по отношению к первому компоненту в буфере.
          */
	    glVertexAttribPointer(this.attribArrayIndex, dimensionSize.value(), GL_FLOAT, false, 0, this.offset);
	    
		/**
		 * Учитываем смещение данных в байтах для следующего аттрибута
		 */
		//TODO: Кажется нужно передавать этот оффсет между VBO, так как этотоже глобальное смещение. Оттестировать.
	    //TODO: Здесь даже не знаю как оффсет инкрементировать, так как мы не можем знать размерность врешины. Но да, её можно передавать сюда
		//offset += attr.getOffset();
		/**
		 * Обновляем счётчик количества активных вершин
		 */
		attribArrayIndex++;
		
	    
		return true;
	}
	
	@Override
	public boolean releaseMemory() {
		if (!isRegistred) {
			//FIXME: Наверное лучше кинуть своё исключение
			return false;
		}
		this.isRegistred = false;
		
		glDeleteBuffers(bufferID);
		return true;
	}


}
