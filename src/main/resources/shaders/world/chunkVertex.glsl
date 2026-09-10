#version 330 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec4 color;
layout(location = 2) in vec2 uv;

out vec4 vColor;
out vec2 vUv;
out vec3 vWorldPos;

// Pushed every frame by RenderEngine.transformScene() — already projection*view.
uniform mat4 projectionMatrix;
// Chunk-local -> world transform only (no camera part inside). Host sets it per chunk.
uniform mat4 modelMatrix;

void main() {
	vec4 w = modelMatrix * vec4(pos.xyz, 1.0);
	vWorldPos = w.xyz;
	gl_Position = projectionMatrix * w;
	vColor = color;
	vUv = uv;
}
