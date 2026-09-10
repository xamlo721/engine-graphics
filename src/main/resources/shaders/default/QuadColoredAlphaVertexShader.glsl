#version 330 core

layout (location = 0) in vec3 position;
// Layout батча DynamicQuadBatch держит uv на loc 1 — здесь он не используется.
layout (location = 2) in vec4 color;

out vec4 inputColor;

uniform mat4 mvp;

void main() {
	gl_Position = mvp * vec4(position, 1.0);
	inputColor = color;
}
