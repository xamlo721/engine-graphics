#version 330 core

layout (location = 0) in vec4 position;
layout (location = 1) in vec2 texCoord;

out vec2 outTexCoord;
out vec2 ndcPos;

uniform mat4 mvp;

// Временный механизм (сага «серый фон»): аффинное преобразование UV и локального размера для
// нетривиальных режимов растяжения через рабочий legacy-проход. По умолчанию — идентичность.
uniform bool useTexAffine;
uniform vec2 uvMul;
uniform vec2 uvAdd;
uniform vec2 posScale;
uniform vec2 posOffset;

void main() {

	vec4 pLocal = position;
	if(useTexAffine) {
		pLocal.xy = pLocal.xy * posScale + posOffset;
	}
	gl_Position = mvp * pLocal;
	outTexCoord = texCoord * (useTexAffine ? uvMul : vec2(1.0)) + (useTexAffine ? uvAdd : vec2(0.0));
	ndcPos = gl_Position.xy / max(gl_Position.w, 1e-6);
}
