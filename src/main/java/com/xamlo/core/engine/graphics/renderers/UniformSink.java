package com.xamlo.core.engine.graphics.renderers;

import org.joml.Vector4f;

interface UniformSink {

    void setUniform(String name, boolean value);

    void setUniform(String name, float value);

    void setUniform(String name, Vector4f value);
}
