package com.xamlo.core.engine.graphics.renderers;

import org.joml.Vector4f;

import com.xamlo.core.engine.graphics.components.ShaderProgram;

/**
 * Полный набор общих uniform'ов GUI-шейдера для каждого прохода. Проход объявляет все
 * поля, которые читает фрагментный шейдер, чтобы состояние не перетекало от предыдущего
 * элемента (текст → фон, нажатая кнопка → текст).
 */
final class GuiShaderState {

    private static final Vector4f NO_TINT = new Vector4f(0f, 0f, 0f, 0f);

    private GuiShaderState() {
    }

    static void applyBackground(ShaderProgram shader, boolean useTexture, Vector4f backgroundColor,
                                Vector4f hoverColor, float hoverIntensity,
                                Vector4f pressedColor, float pressedIntensity) {
        applyBackground(new ShaderUniformSink(shader), useTexture, backgroundColor,
                hoverColor, hoverIntensity, pressedColor, pressedIntensity);
    }

    static void applyBackground(UniformSink sink, boolean useTexture, Vector4f backgroundColor,
                                Vector4f hoverColor, float hoverIntensity,
                                Vector4f pressedColor, float pressedIntensity) {
        sink.setUniform("useTexture", useTexture);
        sink.setUniform("useTextColor", false);
        sink.setUniform("backgroundColor", backgroundColor);
        sink.setUniform("hoverColor", hoverColor);
        sink.setUniform("hoverIntensity", hoverIntensity);
        sink.setUniform("pressedColor", pressedColor);
        sink.setUniform("pressedIntensity", pressedIntensity);
    }

    static void applyText(ShaderProgram shader, Vector4f backgroundColor, Vector4f textColor) {
        applyText(new ShaderUniformSink(shader), backgroundColor, textColor);
    }

    static void applyText(UniformSink sink, Vector4f backgroundColor, Vector4f textColor) {
        sink.setUniform("backgroundColor", backgroundColor);
        sink.setUniform("useTexture", true);
        sink.setUniform("hoverColor", backgroundColor);
        sink.setUniform("hoverIntensity", 0.0f);
        sink.setUniform("textColor", textColor);
        sink.setUniform("useTextColor", true);
        sink.setUniform("pressedColor", NO_TINT);
        sink.setUniform("pressedIntensity", 0.0f);
        sink.setUniform("useTexAffine", false);
    }
}
