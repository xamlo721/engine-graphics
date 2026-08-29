#version 330 core

in vec4 vColor;
in vec2 vUv;
in vec3 vWorldPos;

out vec4 fragColor;

#ifdef USE_TEXTURE
uniform sampler2D texture_sampler;
#endif

// Fog: linear fade into uFogColor starting at 75% of the configured far distance,
// fully blended once past it. uFogFar <= 0 disables fog entirely.
uniform vec4 uFogColor;
uniform float uFogFar;
uniform vec3 uFogCenter;

void main() {
	vec4 base;
#ifdef USE_TEXTURE
	base = texture(texture_sampler, vUv);
#else
	base = vColor;
#endif

	if (uFogFar > 0.0) {
		float d = distance(vWorldPos, uFogCenter);
		float start = uFogFar * 0.75f;
		float f = clamp((d - start) / max(uFogFar - start, 1e-6), 0.0, 1.0);
		base.rgb = mix(base.rgb, uFogColor.rgb, f);
	}

	fragColor = base;
}
