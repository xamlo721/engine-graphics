package com.xamlo.core.engine.graphics.primitives;

import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import java.nio.FloatBuffer;

import com.xamlo.core.engine.graphics.api.primitives.IVertexAttribute;
import com.xamlo.core.engine.graphics.api.primitives.IVertexStructure;

import static org.lwjgl.opengl.GL15.glBufferData;


/**
 * Объект вершинного буфера, который может хранить в VRAM
 * данные одного типа об одной или нескольки вершинах
 * Класс является отображением примитивного фрагмента сырой памяти VRAM/
 * хранит указатель и копию памяти VRAM
 * @author Satomi
 *
 */
public class VertexBufferObject extends AbstractVertexBuffer {
	
	/**
	 * Копия данных, которые должны быть положены в VRAM
	 * Возможно их стоило убрать из озу совсем, но ладно
	 */
	private FloatBuffer vertices;
	
	private IVertexStructure structure;
	
	public VertexBufferObject(FloatBuffer vertices, IVertexStructure struct, EnumMemoryType memoryType) {
		
    	if (vertices.capacity() == 0) {
    		//TODO: Охх я вам как дааам!
    		return;
    	}
		this.vertices = vertices;
		this.structure = struct;
		this.bufferType = memoryType;
		bufferID = glGenBuffers();
		
	}
	
	@Override
	public boolean allocMemory(int indexVBO) {
		if (isRegistred) {
			//FIXME: Наверное лучше кинуть своё исключение
			return false;
		}

		/**
		 * Указываем OpenGL, что нужно переключиться на область памяти с индексом bufferID для дальнейшего заполнения
		 */
		glBindBuffer(EnumBufferType.VertexBuffer.getOpenGLValue(), bufferID);
		/**
		 * Перемещаем в выбранную область памяти массив из ОЗУ
		 */
		glBufferData(EnumBufferType.VertexBuffer.getOpenGLValue(), vertices, bufferType.getOpenGLValue());
		

		/**
		 * Для того, чтобы не мешать другим буферам, устанавливаем глобальный для VAO индекс аттрибута
		 * Т.к мы не единственный VBO в входящий в VAO, то нужно учитывать индекс последнего помеченного аттрибута
		 * Контроль за этим возложен на VAO. Сюда должен приехать актуальный индекс
		 */
		this.attribArrayIndex = indexVBO;
		//Разметим для каждого аттрибута
	    for (IVertexAttribute attr : structure.getAttributes()) {
	    	/**
	    	 * Помечаем для OpenGL, что мы собираемся работать с аттрибутом №attribArrayIndex по счёту
	    	 */
			glEnableVertexAttribArray(attribArrayIndex);
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
			glVertexAttribPointer(attribArrayIndex, attr.getDimensionSize().value(), attr.getType().getOpenGLValue(), attr.isNormalized(), structure.getVertexStride(), offset);
			System.out.println("Register VOBAttib : " + attribArrayIndex + " " + attr.getDimensionSize().value() + " " + attr.getType().getOpenGLValue() + " " + attr.isNormalized() + " " + structure.getVertexStride() + " " + offset);
			/**
			 * Учитываем смещение данных в байтах для следующего аттрибута
			 */
			//TODO: Кажется нужно передавать этот оффсет между VBO, так как этотоже глобальное смещение. Оттестировать.
			offset += attr.getOffset();
			/**
			 * Обновляем счётчик количества активных вершин
			 */
			attribArrayIndex++;
		}
		this.isRegistred = true;

		return true;
	}
	
}
