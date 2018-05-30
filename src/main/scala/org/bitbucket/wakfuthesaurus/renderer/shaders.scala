package org.bitbucket.wakfuthesaurus.renderer

object shaders {
  val spriteTransformVertex =
    """
attribute vec2 a_position;
attribute vec2 a_texcoord;

uniform mat3 u_matrix;

varying vec2 v_texcoord;

void main() {
  gl_Position = vec4((u_matrix * vec3(a_position, 1)).xy, 0, 1);
  v_texcoord = a_texcoord;
}
"""

  val spriteTransformFragment =
    """
precision mediump float;

varying vec2 v_texcoord;

uniform sampler2D texture;

uniform vec4 u_color;

void main() {
  gl_FragColor = texture2D(texture, v_texcoord) * u_color;
}"""
}
