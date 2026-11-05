package com.xamlo.core.engine.graphics.renderers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

class GuiShaderStateTest {

    private static final Set<String> BACKGROUND_UNIFORMS = Set.of(
            "useTexture", "useTextColor", "backgroundColor", "hoverColor",
            "hoverIntensity", "pressedColor", "pressedIntensity");

    private static final Set<String> TEXT_UNIFORMS = Set.of(
            "backgroundColor", "useTexture", "hoverColor", "hoverIntensity",
            "textColor", "useTextColor", "pressedColor", "pressedIntensity", "useTexAffine");

    private static final class RecordingSink implements UniformSink {
        final Map<String, Object> values = new HashMap<>();

        @Override
        public void setUniform(String name, boolean value) {
            values.put(name, value);
        }

        @Override
        public void setUniform(String name, float value) {
            values.put(name, value);
        }

        @Override
        public void setUniform(String name, Vector4f value) {
            values.put(name, value);
        }
    }

    @Test
    void backgroundPassClearsTextColorLeftByPreviousText() {
        RecordingSink sink = new RecordingSink();
        Vector4f background = new Vector4f(0.1f, 0.2f, 0.3f, 0.4f);
        Vector4f hover = new Vector4f(0.5f, 0.6f, 0.7f, 0.8f);
        Vector4f pressed = new Vector4f(0.9f, 0.1f, 0.2f, 0.3f);

        GuiShaderState.applyBackground(sink, true, background, hover, 0.1f, pressed, 0.35f);

        assertEquals(BACKGROUND_UNIFORMS, sink.values.keySet());
        assertEquals(false, sink.values.get("useTextColor"));
        assertEquals(true, sink.values.get("useTexture"));
        assertSame(background, sink.values.get("backgroundColor"));
        assertSame(hover, sink.values.get("hoverColor"));
        assertEquals(0.1f, (Float) sink.values.get("hoverIntensity"), 1e-6f);
        assertSame(pressed, sink.values.get("pressedColor"));
        assertEquals(0.35f, (Float) sink.values.get("pressedIntensity"), 1e-6f);
    }

    @Test
    void textPassResetsPressedTintAndAffineState() {
        RecordingSink sink = new RecordingSink();
        Vector4f background = new Vector4f(0.1f, 0.2f, 0.3f, 0.4f);
        Vector4f textColor = new Vector4f(0.7f, 0.8f, 0.9f, 1f);

        GuiShaderState.applyText(sink, background, textColor);

        assertEquals(TEXT_UNIFORMS, sink.values.keySet());
        assertEquals(true, sink.values.get("useTextColor"));
        assertEquals(true, sink.values.get("useTexture"));
        assertSame(background, sink.values.get("backgroundColor"));
        assertSame(background, sink.values.get("hoverColor"));
        assertEquals(0f, (Float) sink.values.get("hoverIntensity"), 1e-6f);
        assertSame(textColor, sink.values.get("textColor"));
        assertEquals(0f, (Float) sink.values.get("pressedIntensity"), 1e-6f);
        Vector4f pressed = (Vector4f) sink.values.get("pressedColor");
        assertEquals(0f, pressed.x, 1e-6f);
        assertEquals(0f, pressed.y, 1e-6f);
        assertEquals(0f, pressed.z, 1e-6f);
        assertEquals(0f, pressed.w, 1e-6f);
        assertEquals(false, sink.values.get("useTexAffine"));
    }
}
