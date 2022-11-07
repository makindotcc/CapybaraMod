#version 150

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

    float strength = 0.3;
    float t = ProgramTime / 3.0;

    vec3 col = vec3(0);
    vec2 pos = 4.0 * (vec2(0.5) - texCoord0);

    for(float k = 1.0; k < 7.0; k+=1.0){
        pos.x += strength * sin(2.0 * t + k * 1.5 * pos.y) + t * 0.5;
        pos.y += strength * cos(2.0 * t + k * 1.5 * pos.x);
    }

    //Time varying pixel color
    col += 0.5 + 0.5 * cos(ProgramTime + pos.xyx + vec3(0,2,4));
    //Gamma
    col = pow(col, vec3(0.4545));
    //Fragment color
    fragColor = vec4(col,color.a);
}
