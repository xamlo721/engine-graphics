package com.xamlo.core.engine.graphics.components;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Класс Texture представляет текстуру OpenGL.
 * Он загружает изображение из файла и создает соответствующую текстуру OpenGL.
 */
public class Texture extends AbstractTexture {

    private ByteBuffer textureBuffer;

    public Texture(int width, int height, ByteBuffer buf, String identifier) {
        super(identifier, width, height);

        this.textureBuffer = buf;
    }
    
    /**
     * Привязывает текстуру OpenGL.
     */
    @Override
    public void bind() {
        if (glTextureID > 0) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureID);
        } else {
            generateImage();
        }
    }

    private void generateImage() {
    	
        glTextureID = glGenTextures();
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureID);
        
        //Указываем сколько байт использовалось для одного пикселя
        //R(8)+G(8)+B(8)+A(8) => 32bit => 1 int
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        
        /*
         * Этот параметр в основном говорит о том, что когда пиксель рисуется без
         * прямой однозначной привязки к текстурной координате, он выбирает ближайшую
         * точку текстурной координаты.
         */
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        /*
         * target - Тип текстуры, обычно GL_TEXTURE_2D
         *
         * level - Указывает номер уровня детализации.
         * 		Уровень 0 - это базовый уровень изображения.
         * 		Уровень n - это n-е уменьшенное изображение mipmap.
         *
         * internal format - Определяет количество и структуру цветовых компонентов в текстуре
         *
         * width - Ширина текстуры
         *
         * height - Высота текстуры
         *
         * border - Не знаю, но раз нет бордюра, то 0
         *
         * format - Структура расположения цветовых компонентов в пикселе.
         *
         * type - какой тип данных использовать для хранения пикселя
         * 		Обычно для RGBA хватит классического сишного GL_UNSIGNED_BYTE
         *
         * data - буфер, где лежит текстура
         */
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, textureBuffer);
        
        /*
         * Разрешить видеокарте сгенерировать mipmap для текстуры
         */
        glGenerateMipmap(GL_TEXTURE_2D);
        

        stbi_image_free(textureBuffer);
        textureBuffer = null;
    }

    /**
     * Отвязывает текстуру OpenGL.
     */
    @Override
    public void unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

}