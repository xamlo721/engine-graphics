package com.xamlo.core.engine.graphics.primitives;

import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;

import com.xamlo.core.engine.graphics.api.primitives.IBufferObject;

public abstract class AbstractVertexBuffer  implements IBufferObject {

	/**
	 * Флаг, сообщающий о том, что данный буфер уже был размещён в VRAM
	 * Для такого объекта можно вызвать release, но нельзя вызвать alloc
	 */
	protected boolean isRegistred;
	
	/**
	 * Целочисленный ID области памяти (буфера) в VRAM видеокарты
	 * Устанавливается какое-то значение методом glBindBuffer
	 */
	protected int bufferID;
	
	/**
	 * Тип области памяти, в которой мы хотим разместить буфер.
	 * Разные области памяти имеют разные размеры и разную скорость работы с GPU
	 * Существуют следующие типы:
	 * 		GL_STATIC_DRAW: данные либо никогда не будут изменяться, либо будут изменяться очень редко;
	 * 		GL_DYNAMIC_DRAW: данные будут меняться довольно часто;
	 * 		GL_STREAM_DRAW: данные будут меняться при каждой отрисовке.	 
	 * 
	 */
	protected EnumMemoryType bufferType;
	
	/**
	 * Переменная offset задаёт смещение, для укладки элементов
	 * через выхов функции glVertexAttribPointer.
	 * 
	 * Данное смещение показывает, на сколько байт (шт) должны 
	 * быть смещены записываемые текущим вызовом байты.
	 * Изначально смещения нет, но для VBO с несколькими аттрибутами
	 * это смещение должно быть учтено как offset += <Размер атрибута в байтах>
	 */
	protected int offset = 0;
	
	/**
	 * Индекс списка аттрибута показывает, какой по счёту
	 * атрибут используется буфером VBO-VAO 
	 * glEnableVertexAttribArray(attribArrayIndex);
	 * 
	 * Для каждого VAO нельзя нарушать порядок этих индексов,
	 * а так как VBO могут описывать произвольное количество
	 * аттрибутов вершин, то VAO должен вести учёт этих индексов
	 * 
	 */
	protected int attribArrayIndex = 0;
	
	/**
	 * Возвращает количество активных вершин буфера
	 */
	public int getAttribsCount() {
		return attribArrayIndex;
	}
	
	@Override
	public void bind() {
		glBindBuffer(EnumBufferType.VertexBuffer.getOpenGLValue(), bufferID);
	}

	@Override
	public void unbind() {
		glBindBuffer(EnumBufferType.VertexBuffer.getOpenGLValue(), 0);
	}
	
	@Override
	public boolean releaseMemory() {
		if (!isRegistred) {
			//FIXME: Наверное лучше кинуть своё исключение
			return false;
		}
		this.isRegistred = false;
		this.attribArrayIndex = 0;
		glDeleteBuffers(bufferID);

		return true;
	}

	
}
