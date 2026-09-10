#version 330 core

in vec4 inputColor;

out vec4 outColor;

void main() {
	// Прозрачный pass-through: состояние блендинга владеет вызывающий проход (см. red-line).
	outColor = inputColor;
}
