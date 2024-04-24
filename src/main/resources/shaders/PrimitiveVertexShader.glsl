#version 330 core

layout (location = 0) in vec3 position;
//layout (location = 1) in vec3 rawColor;
layout (location = 1) in vec2 texCoord;

//out vec3 inputColor;
out vec2 outTexCoord;

uniform mat4 projectionMatrix;
/**
 * {1.0, 0.0, 0.0,  0.0}
 * {0.0, 1.0, 0.0,  0.0}
 * {0.0, 0.0, 1.0,  0.0}
 * {  X,   Y,   Z,  1.0}
 *
 */
uniform mat4 positionMatrix;
uniform mat4 rotationMatrix;
uniform mat4 scaleMatrix;

void main() {

	mat4 cameraMatrix = projectionMatrix;
	mat4 T = positionMatrix;
	mat4 R = rotationMatrix;
	mat4 S = scaleMatrix;
	vec4 v = vec4(position.x, position.y, position.z, 1.0);

	gl_Position =
			cameraMatrix
			* T
			* R
			* S
			* v;

    outTexCoord = texCoord;


}
