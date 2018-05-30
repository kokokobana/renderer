package org.bitbucket.wakfuthesaurus.renderer.transform

import org.bitbucket.wakfuthesaurus.renderer._
import org.bitbucket.wakfuthesaurus.renderer.transform.Transformation.TransformDefinition
import org.bitbucket.wakfuthesaurus.shared.anm.ShapeDefinition
import org.scalajs.dom.raw.{
  WebGLProgram,
  WebGLTexture,
  WebGLUniformLocation,
  WebGLRenderingContext => GL
}

import scala.scalajs.js.typedarray.Float32Array

final class TransformRenderingProgramDefinition(
    override val ctx: RenderingContext
) extends RenderingProgramDefinition {
  override val vertexShader: String = shaders.spriteTransformVertex

  override val fragmentShader: String = shaders.spriteTransformFragment

  override def load(program: WebGLProgram): RenderingProgram =
    new RenderingProgram {
      override val gl: GL = ctx.gl

      private[this] val positionLocation = gl
        .getAttribLocation(program, "a_position")
        .asInstanceOf[WebGLUniformLocation]
      private[this] val texCoordLocation = gl
        .getAttribLocation(program, "a_texcoord")
        .asInstanceOf[WebGLUniformLocation]
      private[this] val matrixLocation = gl
        .getUniformLocation(program, "u_matrix")
        .asInstanceOf[WebGLUniformLocation]
      private[this] val colorLocation = gl
        .getUniformLocation(program, "u_color")
        .asInstanceOf[WebGLUniformLocation]

      private[this] val positionBuffer = gl.createBuffer()

      private[this] val texCoordsBuffer = gl.createBuffer()

      private[this] val texCoordsTmp = new Float32Array(12)

      initialise()

      private[this] def initialise() = {
        gl.bindBuffer(GL.ARRAY_BUFFER, positionBuffer)
        gl.enableVertexAttribArray(positionLocation.asInstanceOf[Int])
        gl.vertexAttribPointer(
          positionLocation.asInstanceOf[Int],
          2,
          GL.FLOAT,
          normalized = false,
          0,
          0
        )
        val positions = scalajs.js.Array(
          0, 0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1
        )
        gl.bufferData(
          GL.ARRAY_BUFFER,
          new Float32Array(positions),
          GL.STATIC_DRAW
        )

        gl.bindBuffer(GL.ARRAY_BUFFER, texCoordsBuffer)
        gl.bufferData(
          GL.ARRAY_BUFFER,
          48,
          GL.DYNAMIC_DRAW
        )
      }

      override def use(): Unit = {
        gl.enable(GL.BLEND)
        gl.blendFunc(GL.ONE, GL.ONE_MINUS_SRC_ALPHA)
        gl.useProgram(program)
      }

      @inline override def drawSprite(
          shape: ShapeDefinition,
          transform: TransformDefinition,
          texture: ⇒ WebGLTexture
      ): Unit = {

        util.setRectTexCoords(
          texCoordsTmp,
          shape.left,
          shape.right,
          shape.top,
          shape.bottom
        )

        gl.bindTexture(GL.TEXTURE_2D, texture)

        gl.uniformMatrix3fv(
          matrixLocation,
          transpose = false,
          transform.matrix.toFloat32Array
        )

        gl.uniform4f(
          colorLocation,
          transform.red,
          transform.green,
          transform.blue,
          transform.alpha
        )

        gl.bindBuffer(GL.ARRAY_BUFFER, texCoordsBuffer)
        gl.enableVertexAttribArray(texCoordLocation.asInstanceOf[Int])
        gl.vertexAttribPointer(
          texCoordLocation.asInstanceOf[Int],
          2,
          GL.FLOAT,
          normalized = false,
          0,
          0
        )

        gl.bufferSubData(GL.ARRAY_BUFFER, 0, texCoordsTmp)

        gl.drawArrays(GL.TRIANGLES, 0, 6)
      }
    }
}
