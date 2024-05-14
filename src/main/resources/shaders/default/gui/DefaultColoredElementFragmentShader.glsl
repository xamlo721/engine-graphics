#version 330 core

in vec3 inputColor;

out vec4 color;

void main() {

	color = vec4(inputColor, 1.0f);

}
