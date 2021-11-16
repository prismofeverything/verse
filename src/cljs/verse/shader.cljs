(ns verse.shader)

(def base-vertex-shader
  "

")

(def hello-shader
  "
#include <common>

uniform float u_time;
uniform vec2 u_mouse;
uniform vec3 u_resolution;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
  // Normalized pixel coordinates (from 0 to 1)
  vec2 uv = fragCoord/u_resolution.xy;
  vec2 mouse = u_mouse/u_resolution.xy;
  mouse.y = 1.0 - mouse.y;
  float away = distance(uv, mouse);

  // Time varying pixel color
  vec3 col = 0.5 + 0.5*cos(u_time+uv.xyx*40.0*away+vec3(0,2,4));

  // Output to screen
  fragColor = vec4(col,1.0);
}

void main() {
  mainImage(gl_FragColor, gl_FragCoord.xy);
}
")

(def line-shader
  "
uniform vec2 u_resolution;
uniform vec2 u_mouse;
uniform float u_time;

float plot(vec2 st, float pct){
  return  smoothstep( pct-0.02, pct, st.y) -
          smoothstep( pct, pct+0.02, st.y);
}

void main() {
    vec2 st = gl_FragCoord.xy/u_resolution;

    float y = pow(st.x,1.0);
    // float y = log(st.x);

    vec3 color = vec3(y);

    float pct = plot(st,y);
    color = (1.0-pct)*color+pct*vec3(0.0,1.0,0.0);

    gl_FragColor = vec4(color,1.0);
}
")
