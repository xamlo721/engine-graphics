package com.xamlo.core.engine.graphics.renderers;

import org.joml.Vector4f;

import com.xamlo.core.engine.graphics.components.ShaderProgram;

final class ShaderUniformSink implements UniformSink {

    private final ShaderProgram shader;

    ShaderUniformSink(ShaderProgram shader) {
        this.shader = shader;
    }

    @Override
    public void setUniform(String name, boolean value) {
        shader.setUniform(name, value);
    }

    @Override
    public void setUniform(String name, float value) {
        shader.setUniform(name, value);
    }

    @Override
    public void setUniform(String name, Vector4f value) {
        shader.setUniform(name, value);
    }
}
