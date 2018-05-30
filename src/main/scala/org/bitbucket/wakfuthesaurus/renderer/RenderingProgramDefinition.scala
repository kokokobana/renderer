package org.bitbucket.wakfuthesaurus.renderer

import org.bitbucket.wakfuthesaurus.renderer.transform.Transformation.TransformDefinition
import org.bitbucket.wakfuthesaurus.shared.anm.ShapeDefinition
import org.scalajs.dom.raw.{
  WebGLProgram,
  WebGLTexture,
  WebGLRenderingContext => GL
}

trait RenderingProgramDefinition {
  def ctx: RenderingContext

  def vertexShader: String

  def fragmentShader: String

  def load(program: WebGLProgram): RenderingProgram
}

trait RenderingProgram {
  def gl: GL

  def use(): Unit

  def drawSprite(
      shape: ShapeDefinition,
      transform: TransformDefinition,
      texture: ⇒ WebGLTexture
  ): Unit
}
