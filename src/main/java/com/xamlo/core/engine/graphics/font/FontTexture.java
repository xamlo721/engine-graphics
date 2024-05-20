package com.xamlo.core.engine.graphics.font;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import static org.lwjgl.stb.STBImage.*;

import com.xamlo.core.engine.graphics.components.Texture;

public class FontTexture extends Texture {


    public FontTexture(BufferedImage image) {
    	
    	super(image.getWidth(), image.getHeight(), convertImageToByteBuffer(image), "font");
    }
    
    
    private static ByteBuffer convertImageToByteBuffer(BufferedImage image) {
        try {
            // Конвертируем BufferedImage в массив байтов PNG формата
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageData = baos.toByteArray();
            
            // Создаем ByteBuffer из массива байтов
            ByteBuffer imageBuffer = BufferUtils.createByteBuffer(imageData.length);
            imageBuffer.put(imageData);
            imageBuffer.flip();
            
            // Загружаем изображение через STBImage
            IntBuffer width = BufferUtils.createIntBuffer(Integer.BYTES);
            IntBuffer height = BufferUtils.createIntBuffer(Integer.BYTES);
            IntBuffer channels = BufferUtils.createIntBuffer(Integer.BYTES);
            
            ByteBuffer buf = stbi_load_from_memory(
                imageBuffer,
                width,
                height,
                channels,
                STBI_rgb_alpha
            );
            
            if (buf == null) {
                throw new RuntimeException("Failed to load image from memory: " + stbi_failure_reason());
            }
            
            return buf;
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert image to PNG format", e);
        }
    }

}