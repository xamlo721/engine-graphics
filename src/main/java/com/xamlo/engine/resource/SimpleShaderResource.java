package com.xamlo.engine.resource;

import com.xamlo.engine.api.resources.IShaderResource;

public class SimpleShaderResource implements IShaderResource<String> {

    private final String identifier;
    private final String shaderProgram;

    public SimpleShaderResource(String identifier, String shaderProgram) {
        this.identifier = identifier;
        this.shaderProgram = shaderProgram;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getShaderProgram() {
        return shaderProgram;
    }
}
