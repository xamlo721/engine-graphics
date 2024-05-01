package com.xamlo.core.engine.graphics.opengl.blend;

import static org.lwjgl.opengl.GL11.GL_BLEND;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 * Для работы со смешиванием в openGL необходимо иметь текстуры поддерживающие alpha-канал
 * 
 * Их загрузка выглядит примерно так:
 * glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data); 
 * 
 * После этого проверить, что в используемом шейдере не обрезается alpha-канал
 * 
 * Так же в шейдере можно настроить обрезание всего, что имеет низкий альфа канал, например
 * 
 * void main() {
 * 
 * 		vec4 texColor = texture(texture_sampler, inputTexCoord);
 * 		
 * 		if(texColor.a < 0.01) {
 * 			discard;
 * 		}
 * 		
 * 		fragColor = texColor;
 * }
 * 		 
 * При выборке на границах текстуры OpenGL выполняет интерполяцию значения на границе со значением из 
 * следующего за ним значения, полученным повторением текстуры (поскольку мы установили параметр 
 * повторения текстуры в GL_REPEAT). Для обычного применения текстур это нормально, но для текстуры с 
 * прозрачностью это не годится: 
 * полностью прозрачное значение текселей на верхней границе смешивается с полностью непрозрачными текселями нижней границы.
 * В результате вокруг квадрата с нашей текстурой может появится полупрозрачная цветная рамка. 
 * Для избежания этого артефакта нужно параметр повтора установить в GL_CLAMP_TO_EDGE при использовании текстур с прозрачностью.
 * 
 * glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);	
 * glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
 * 
 * 
 * @author Satomi
 *
 */
public class OpenGLBlend {
		
	/**
	 * Для рендера изображений с объектами, имеющими разную степень непрозрачности мы должны включить режим смешивания
	 * 
	 * Смешивание OpenGL выполняется по следующей формуле
	 * 
	 * C_result = C_src * F_src  +  C_dst * F_dst
	 * 
	 * где C_src – вектор цвета источника. Это значение цвета, полученное из текстуры.
     * C_dst – вектор цвета приемника. Это значение цвета, хранимое на данный момент в буфере цвета.
	 * F_src – множитель источника. Задает степень влияния альфа-компоненты на цвет источника.
	 * F_dst – множитель приемника. Задает степень влияния альфа-компоненты на цвет приемника.
	 * 
	 * Роли источника и приемника назначаются OpenGL автоматически, но множители для них мы можем задать сами
	 * 
	 * 
	 */
	public static void enable() {
		GL11.glEnable(GL_BLEND);
	}
	
	/**
	 * При наложении текстуры друг на друга, можно задать разные режимы работы смешивания альфа канала
	 * 
	 * Перечень режимов смешивания приведёт в EnumOpenglBlendMode.
	 * 
	 * Например, если А имеет a-channel 0.4, а B имеет a-channel 1.0,
	 * то 		
	 * GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	 * смешает цвета в пропорции 40 и 60 (1 - srcAlpha)
	 * 
	 * @param source
	 * @param destination
	 */
	public static void setMode(EnumOpenglBlendMode source, EnumOpenglBlendMode destination) {
		
		GL11.glBlendFunc(source.getOpenGLValue(), destination.getOpenGLValue());
		
	}
	
	
	/**
	 * В дополнение к setMode можно задавать раздельный режим смешивания для RGB и A каналов
	 * 
	 * @param sfactorRGB
	 * @param dfactorRGB
	 * @param sfactorAlpha
	 * @param dfactorAlpha
	 */
	public static void setSeparateMode(EnumOpenglBlendMode sfactorRGB, EnumOpenglBlendMode dfactorRGB, EnumOpenglBlendMode sfactorAlpha, EnumOpenglBlendMode dfactorAlpha) {
		
		GL15.glBlendFuncSeparate(sfactorRGB.getOpenGLValue(), dfactorRGB.getOpenGLValue(), sfactorAlpha.getOpenGLValue(), dfactorAlpha.getOpenGLValue());
		
	}
	
	
	
	/**
	 * OpenGL разрешает гибкую настройку формулы смешивания, разрешая выбор операции, производимой между компонентами формулы
	 * 
	 *  C_result = C_src * F_src | + | C_dst * F_dst
	 *  
	 * По умолчанию компоненты источника и приемника складываются. 
	 */
	public static void seBlendEquationMode(EnumOpenGLBlendOperationMode operationMode) {

		GL15.glBlendEquation(operationMode.getOpenGLValue());
		
	}
	
}
