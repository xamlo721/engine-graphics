package com.xamlo.core.engine.graphics.renderers;

import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.components.ShaderProgram;
import com.xamlo.core.engine.graphics.components.gui.ClipContexts;

/**
 * Ставит юниформы скисора GUI-шейдера для элемента перед его отрисовкой.
 * Вызывается каждый батч (квад/текст): состояние программы не должно
 * перетекать между соседними элементами без явного сброса.
 */
final class ClipBinder {

    private ClipBinder() {
    }

    static void apply(ShaderProgram shader, IUIElement element) {
        // Программа без юниформов клипа (например кастомный шейдер элемента) — просто пропускаем.
        if (!shader.hasUniform("useClip")) {
            return;
        }
        int[] rect = ClipContexts.effectiveClippedRect(element);
        if (rect == null) {
            shader.setUniform("useClip", false);
        } else {
            shader.setUniform("useClip", true);
            shader.setUniform("clipRectNdc", ClipContexts.rectToNdc(rect));
        }
    }

}
