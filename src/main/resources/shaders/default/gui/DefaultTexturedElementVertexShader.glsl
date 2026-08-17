#version 330 core

layout (location = 0) in vec4 position;
layout (location = 1) in vec2 texCoord;

out vec2 outTexCoord;
out vec2 ndcPos;

uniform mat4 mvp;

void main() {

	gl_Position = mvp * position;
	outTexCoord = texCoord;
	ndcPos = gl_Position.xy / max(gl_Position.w, 1e-6);
}
