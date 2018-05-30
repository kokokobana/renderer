package org.bitbucket.wakfuthesaurus.renderer

import org.scalajs.dom.html._
import org.scalajs.dom.raw.{
  WebGLProgram,
  WebGLShader,
  WebGLTexture,
  WebGLRenderingContext => GL
}

final class RenderingContext(val gl: GL) {
  def loadTexture(image: Image): WebGLTexture = {
    val tex = gl.createTexture()
    gl.bindTexture(GL.TEXTURE_2D, tex)
    gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_WRAP_S, GL.CLAMP_TO_EDGE)
    gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_WRAP_T, GL.CLAMP_TO_EDGE)
    gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MIN_FILTER, GL.LINEAR)
    gl.texImage2D(GL.TEXTURE_2D, 0, GL.RGBA, GL.RGBA, GL.UNSIGNED_BYTE, image)
    tex
  }

  def loadShader(tpe: Int, source: String): Either[String, WebGLShader] = {
    val shader = gl.createShader(tpe)
    gl.shaderSource(shader, source)
    gl.compileShader(shader)
    val result =
      gl.getShaderParameter(shader, GL.COMPILE_STATUS).asInstanceOf[Boolean]
    if (result) Right(shader)
    else {
      val error = gl.getShaderInfoLog(shader)
      gl.deleteShader(shader)
      Left(s"Failed to create shader: $error")
    }
  }

  def loadProgram(vertexShader: String,
                  fragmentShader: String): Either[String, WebGLProgram] = {
    for {
      vertexShader ← loadShader(GL.VERTEX_SHADER, vertexShader)
      fragmentShader ← loadShader(GL.FRAGMENT_SHADER, fragmentShader)
    } yield {
      val program = gl.createProgram()
      gl.attachShader(program, vertexShader)
      gl.attachShader(program, fragmentShader)
      gl.linkProgram(program)
      program
    }
  }.flatMap { program ⇒
    val result =
      gl.getProgramParameter(program, GL.LINK_STATUS).asInstanceOf[Boolean]
    if (result) Right(program)
    else {
      val error = gl.getProgramInfoLog(program)
      gl.deleteProgram(program)
      Left(s"Failed to link program: $error")
    }
  }

  def loadRenderingProgram(definition: RenderingProgramDefinition)
    : Either[String, RenderingProgram] =
    loadProgram(definition.vertexShader, definition.fragmentShader)
      .map(definition.load)
}
