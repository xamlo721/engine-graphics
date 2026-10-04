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
	vec4 texSampled = texture(texture_sampler, vUv);
	if (texSampled.a < 0.5) {
		discard; // vanilla-style alpha test for cutout art (plants/glass): true silhouettes without an extra blend pass
	}
	// Textured quads still modulate by the baked per-face color: this carries face brightness and runtime tints
	// (e.g. vanilla foliage colormap) exactly like legacy fixed-function multiply-blending did.
	base = vec4(texSampled.rgb * vColor.rgb, 1.0) * vColor.a;
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
