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
