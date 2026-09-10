#version 330 core

in vec2 vTexCoord;

out vec4 outColor;

// Имя самплера по конвенции соседа DefaultTexturedElementFragmentShader (texture_sampler).
uniform sampler2D texture_sampler;

void main() {
	vec4 base = texture(texture_sampler, vTexCoord);
	if(base.a < 0.01) discard; // alpha-test как у GUI-set'а и legacy vanilla
	outColor = base;
}
