package com.xamlo.core.engine.graphics.api.primitives;

import com.xamlo.core.engine.graphics.components.attribs.EnumAttributeType;
import com.xamlo.core.engine.graphics.primitives.EnumDimestionSize;

/** 
 * Определяет общие методы для атрибутов вершин.
 */ 
public interface IVertexAttribute {

	/**
	 * Получает размерность атрибута в байтах.
	 * (количество переменных в атрибуте)
	 * @return размер атрибута типа int 
	 */ 
	EnumDimestionSize getDimensionSize();
	
	/** 
	 * Получает тип атрибута. 
	 * @return тип атрибута типа EnumAttributeType 
	 */ 
	EnumAttributeType getType();
	
	/** 
	 * Определяет, является ли атрибут нормализованным. 
	 * Если атрибут нормализован, то его значения будут приведены к диапазону от -1 до 1 или от 0 до 1.
	 * @return флаг, указывающий на нормализацию типа boolean 
	*/ 
	boolean isNormalized();
	
	/** 
	 * Получает смещение (offset) атрибута от начала массива вершинных данных в байтах. 
	 * @return смещение атрибута типа long 
	 */ 
	int getOffset();
	
}
