// Silk gradient background, drawn with a single WebGL2 fragment shader.
//
// The shader warps smooth noise into soft folds, maps it on a 5-stop color ramp
// (darkest to brightest), adds satin highlights and a film grain.

const VERT = `#version 300 es
in vec2 p;
out vec2 vUv;
void main() { vUv = p * 0.5 + 0.5; gl_Position = vec4(p, 0.0, 1.0); }`;

const FRAG = `#version 300 es
precision highp float;
in vec2 vUv;
out vec4 outColor;
uniform vec2 uRes;
uniform vec3 uStops[5];
uniform float uSeed;
uniform float uGrain;
uniform float uFocus;

uvec2 pcg2d(uvec2 v) {
  v = v * 1664525u + 1013904223u;
  v.x += v.y * 1664525u; v.y += v.x * 1664525u;
  v ^= v >> 16u;
  v.x += v.y * 1664525u; v.y += v.x * 1664525u;
  v ^= v >> 16u;
  return v;
}
float hash(vec2 p) { p = fract(p * vec2(123.34, 456.21)); p += dot(p, p + 45.32); return fract(p.x * p.y); }
float noise(vec2 p) {
  vec2 i = floor(p), f = fract(p);
  vec2 u = f * f * (3.0 - 2.0 * f);
  return mix(mix(hash(i), hash(i + vec2(1, 0)), u.x), mix(hash(i + vec2(0, 1)), hash(i + vec2(1, 1)), u.x), u.y);
}
float fbm(vec2 p) {
  float v = 0.0, a = 0.55;
  mat2 r = mat2(0.8, 0.6, -0.6, 0.8);
  for (int i = 0; i < 3; i++) { v += a * noise(p); p = r * p * 2.03; a *= 0.45; }
  return v / 0.9;
}
vec3 ramp(float t) {
  t = clamp(t, 0.0, 1.0) * 4.0;
  int i = int(min(floor(t), 3.0));
  return mix(uStops[i], uStops[i + 1], smoothstep(0.0, 1.0, t - float(i)));
}
void main() {
  vec2 uv = vUv;
  float aspect = uRes.x / uRes.y;
  // Normalise on width so every export size shows the same composition.
  vec2 p = vec2(uv.x, uv.y / aspect) * 2.0;
  vec2 q = p * 0.55 + uSeed;
  vec2 w = vec2(fbm(q + vec2(0.0, 1.7)), fbm(q + vec2(5.2, 1.3)));
  float f = fbm(q + 1.6 * w);
  float fold = sin((p.x * 0.9 - p.y * 1.6 + 4.0 * f) * 2.2);
  float t = 0.62 * f + 0.30 * (1.0 - uv.y) - 0.05 + 0.10 * fold;
  vec3 col = ramp(t);
  col += uStops[4] * 0.30 * pow(max(fold, 0.0), 10.0) * smoothstep(0.35, 0.75, f);
  // Optional darkening (focus > 0) or brightening (focus < 0) behind the title.
  float d = distance(uv * vec2(1.0, 1.0 / aspect * 2.0), vec2(0.5, 0.5 / aspect));
  col = mix(col, uFocus > 0.0 ? uStops[0] : uStops[2], abs(uFocus) * smoothstep(0.6, 0.0, d));
  // Film grain: two uniform samples summed for a softer, more photographic distribution.
  uvec2 h = pcg2d(uvec2(gl_FragCoord.xy) + uvec2(uint(uSeed * 1000.0)));
  float g = (float(h.x & 0xffffu) + float(h.y & 0xffffu)) / 65535.0 - 1.0;
  col += g * uGrain;
  outColor = vec4(col, 1.0);
}`;

const toRgb = (hex) => {
  const v = parseInt(hex.slice(1), 16);
  return [(v >> 16 & 255) / 255, (v >> 8 & 255) / 255, (v & 255) / 255];
};

/**
 * Paints the background into `canvas` at its CSS size times `devicePixelRatio`.
 * @param {HTMLCanvasElement} canvas
 * @param {{stops: string[], seed: number, grain: number, focus: number}} params
 */
export function paintBackground(canvas, { stops, seed, grain, focus = 0 }) {
  const dpr = window.devicePixelRatio || 1;
  const rect = canvas.getBoundingClientRect();
  canvas.width = Math.round(rect.width * dpr);
  canvas.height = Math.round(rect.height * dpr);
  const gl = canvas.getContext('webgl2', { preserveDrawingBuffer: true });
  const compile = (type, src) => {
    const s = gl.createShader(type);
    gl.shaderSource(s, src);
    gl.compileShader(s);
    if (!gl.getShaderParameter(s, gl.COMPILE_STATUS)) throw new Error(gl.getShaderInfoLog(s));
    return s;
  };
  const program = gl.createProgram();
  gl.attachShader(program, compile(gl.VERTEX_SHADER, VERT));
  gl.attachShader(program, compile(gl.FRAGMENT_SHADER, FRAG));
  gl.linkProgram(program);
  gl.useProgram(program);
  gl.bindBuffer(gl.ARRAY_BUFFER, gl.createBuffer());
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1, -1, 3, -1, -1, 3]), gl.STATIC_DRAW);
  const loc = gl.getAttribLocation(program, 'p');
  gl.enableVertexAttribArray(loc);
  gl.vertexAttribPointer(loc, 2, gl.FLOAT, false, 0, 0);
  gl.viewport(0, 0, canvas.width, canvas.height);
  const u = (name) => gl.getUniformLocation(program, name);
  gl.uniform2f(u('uRes'), canvas.width, canvas.height);
  gl.uniform3fv(u('uStops'), new Float32Array(stops.flatMap(toRgb)));
  gl.uniform1f(u('uSeed'), seed);
  gl.uniform1f(u('uGrain'), grain);
  gl.uniform1f(u('uFocus'), focus);
  gl.drawArrays(gl.TRIANGLES, 0, 3);
}
