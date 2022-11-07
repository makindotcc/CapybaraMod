#version 150

#define SPEED 1000
#define STRETCH 1

uniform sampler2D Sampler0;
uniform float ProgramTime;

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

    float time = ProgramTime;
    vec2 uv = texCoord0.xy * 8.0;
    vec2 uv0 = uv;
    float i0=1.0;
    float i1=1.0;
    float i2=1.0;
    float i4=0.0;
    for (int s=0;s<7;s++)
    {
        vec2 r;
        r=vec2(cos(uv.y*i0-i4+time/i1), sin(uv.x*i0-i4+time/i1))/i2;
        r+=vec2(-r.y, r.x)*0.3;
        uv.xy+=r;

        i0*=1.93;
        i1*=1.15;
        i2*=1.7;
        i4+=0.05+0.1*time*i1;
    }
    float r=sin(uv.x-time)*0.2+0.7;
    float b=sin(uv.y+time)*0.2+0.7;
    float g=sin((uv.x+uv.y+sin(time*0.3))*0.5)*0.5+0.5;
    fragColor = vec4(r, g, b, color.a);
}
