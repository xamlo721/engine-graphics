#version 330 core

in  vec2 outTexCoord;

out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec4 hoverColor;
uniform float hoverIntensity;

void main() {
    vec4 texColor = texture(texture_sampler, outTexCoord);

    if(texColor.a < 0.01) {
        discard;
    }

    fragColor = mix(texColor, hoverColor, hoverIntensity);
}
