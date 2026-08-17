#version 330 core

in vec2 outTexCoord;
in vec2 ndcPos;

out vec4 outColor;

uniform sampler2D texture_sampler;
uniform bool useTexture;
uniform bool useTextColor;
uniform vec4 textColor;
uniform vec4 hoverColor;
uniform float hoverIntensity;
uniform vec4 backgroundColor;

// Клиппинг по прямоугольнику предка-клипера в NDC координатах всего окна
// (xy — левый нижний угол, zw — размер). См. ClipContexts.
uniform bool useClip;
uniform vec4 clipRectNdc;

void main() {
    if(useClip && !(ndcPos.x >= clipRectNdc.x && ndcPos.x <= clipRectNdc.x + clipRectNdc.z
              && ndcPos.y >= clipRectNdc.y && ndcPos.y <= clipRectNdc.y + clipRectNdc.w)) discard;

    vec4 baseColor;

    if(useTextColor) {
        vec4 tex = texture(texture_sampler, outTexCoord);
        if(tex.a < 0.01) discard;
        baseColor = vec4(textColor.rgb, textColor.a * tex.a);
    } else if(useTexture) {
        baseColor = texture(texture_sampler, outTexCoord);
        if(baseColor.a < 0.01) discard;
    } else {
        baseColor = backgroundColor;
    }

    outColor = mix(baseColor, hoverColor, hoverIntensity);
}
