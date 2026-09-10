#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoord;
// Layout батча DynamicQuadBatch держит lightmap на loc 3.
layout (location = 3) in vec2 lightMapCoord;

out vec2 vTexCoord;
out vec2 vLightMap;

uniform mat4 mvp;

void main() {
	gl_Position = mvp * vec4(position, 1.0);
	vTexCoord = texCoord;
	vLightMap = lightMapCoord;
}
