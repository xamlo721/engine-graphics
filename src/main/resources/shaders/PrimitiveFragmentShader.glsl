#version 330 core

//in vec3 inputColor;
in  vec2 outTexCoord;

//out vec4 color;
out vec4 fragColor;

uniform sampler2D texture_sampler;

void main() {

//	color = vec4(inputColor, 1.0f);
    fragColor = texture(texture_sampler, outTexCoord);

}
