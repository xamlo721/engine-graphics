#version 330 core

in vec2 outTexCoord;

out vec4 outColor;

uniform sampler2D texture_sampler;
uniform bool useTexture;
uniform vec4 hoverColor;
uniform float hoverIntensity;
uniform vec4 backgroundColor;

void main() {
    vec4 baseColor;

    if(useTexture) {
        baseColor = texture(texture_sampler, outTexCoord);
        if(baseColor.a < 0.01) discard;
    } else {
        baseColor = backgroundColor;
    }

    outColor = mix(baseColor, hoverColor, hoverIntensity);
}
