#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 rawColor;

out vec3 inputColor;

uniform mat4 projectionMatrix;
uniform mat4 objectMatrix;

uniform mat4 positionMatrix;
uniform mat4 rotationMatrix;
uniform mat4 scaleMatrix;

void main() {

	mat4 cameraMatrix = projectionMatrix;
	mat4 T = positionMatrix;
	mat4 R = rotationMatrix;
	mat4 S = scaleMatrix;
	vec4 v = vec4(position.x, position.y, position.z, 1.0);

	vec4 alternativeObjectPos = objectMatrix * vec4(1.0,  1.0,  1.0,  1.0);

	gl_Position =
			cameraMatrix
			* objectMatrix
			* v;

	inputColor = rawColor;


}
