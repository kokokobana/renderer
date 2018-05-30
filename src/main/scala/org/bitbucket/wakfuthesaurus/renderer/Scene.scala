package org.bitbucket.wakfuthesaurus.renderer

import org.scalajs.dom
import org.scalajs.dom.raw.{WebGLRenderingContext => GL}

import scala.collection.mutable

final class Scene(val gl: GL) {

  private[this] var lastProgram: RenderingProgram = _

  private[Scene] final class SceneAnimation(
      val animation: FrameAnimation,
      val program: RenderingProgram,
      var lastFrame: Int,
      var updateTime: Double
  ) {
    @inline def draw(frame: Int): Unit = {
      if (lastProgram != program) {
        lastProgram = program
        program.use()
      }
      animation.draw(program, frame)
    }
  }

  private[this] val animations: mutable.ArrayBuffer[SceneAnimation] =
    mutable.ArrayBuffer()

  private[this] var finish: Boolean = false

  def addAnimation(
      animation: FrameAnimation,
      program: RenderingProgram
  ): Unit = {
    animations.append(new SceneAnimation(animation, program, -1, 0))
  }

  def clear(): Unit = {
    animations.clear()
  }

  private[this] def process(
      timeInSeconds: Double,
      animation: SceneAnimation
  ): Unit = {
    if (animation.lastFrame == -1 ||
        timeInSeconds - animation.updateTime >= 1.0 / (animation.animation.baseSet.frameRate + 5.0)) {
      val nextFrame = animation.lastFrame + 1
      if (animation.animation.definition.getFrameCount == nextFrame) {
        if (animation.animation.definition.header.isLoop) {
          animation.draw(nextFrame)
          animation.lastFrame = nextFrame
          animation.updateTime = timeInSeconds
        } else {
          animation.draw(0)
          animation.updateTime = timeInSeconds
        }
      } else {
        animation.draw(nextFrame)
        animation.lastFrame = nextFrame
        animation.updateTime = timeInSeconds
      }
    } else {
      animation.draw(animation.lastFrame)
    }
  }

  def stop(): Unit =
    finish = true

  def play(): Unit = {
    finish = false
    dom.window.requestAnimationFrame(draw _)
  }

  private[this] def draw(time: Double): Unit = {
    val timeInSeconds = time * 0.001

    gl.clear(GL.COLOR_BUFFER_BIT)
    gl.viewport(0, 0, gl.canvas.width, gl.canvas.height)

    for (anim <- animations)
      process(timeInSeconds, anim)
    if (!finish)
      dom.window.requestAnimationFrame(draw _)
  }
}
