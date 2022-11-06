#version 150

#define SPEED 2000
#define STRETCH 1

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;

in vec2 texCoord0;
out vec4 fragColor;

vec3 hsv2rgb(in vec3 c) {
	vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0),6.0)-3.0)-1.0, 0.0, 1.0);
	rgb = rgb*rgb*(3.0-2.0*rgb);
	return c.z * mix(vec3(1.0), rgb, c.y);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;

 	vec2 sv = vec2(0.5, 0.9);

    float hue = (cos(GameTime * SPEED + texCoord0.x + texCoord0.y * STRETCH) + 1) / 12 + 0.66;
    fragColor = vec4(hsv2rgb(vec3(hue, sv)), color.a);
t}
