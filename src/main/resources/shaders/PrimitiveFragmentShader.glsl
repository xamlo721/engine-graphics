#version 330 core

//in vec3 inputColor;
in  vec2 outTexCoord;

//out vec4 color;
out vec4 fragColor;

uniform sampler2D texture_sampler;

void main() {

//	color = vec4(inputColor, 1.0f);
	vec4 texColor = texture(texture_sampler, outTexCoord);

	//Штука для смешивания
	//проверяем значение альфа-компоненты текстурного элемента и, если он меньше некого порога, отбрасываем его
    if(texColor.a < 0.01) {
        discard;
    }

    fragColor = texColor;
}
