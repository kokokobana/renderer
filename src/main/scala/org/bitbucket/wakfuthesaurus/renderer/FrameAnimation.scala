package org.bitbucket.wakfuthesaurus.renderer

import cats.syntax.apply._
import org.bitbucket.wakfuthesaurus.renderer.transform.FrameDataReader
import org.bitbucket.wakfuthesaurus.renderer.transform.Transformation._
import org.bitbucket.wakfuthesaurus.renderer.uoption._
import org.bitbucket.wakfuthesaurus.shared.anm._

class FrameAnimation(
    val baseSet: AnimationSet,
    val definition: SpriteDefinitionIndexed,
    flipped: Boolean = false,
    finalTransform: UOption[TransformationFD] = UNone
) {

  private[this] var widthConst: Float = 0
  private[this] var heightConst: Float = 0

  @inline private[renderer] final def drawSprite(
      renderer: RenderingProgram,
      shape: ShapeDefinition,
      transform: TransformationFD,
      set: AnimationSet
  ): Unit = {

    @inline def applyFinalTransform(ot: TransformationFD): TransformationFD =
      finalTransform match {
        case USome(ct) ⇒ ot *> ct
        case UNone ⇒ ot
      }

    val sc = set.definition.scale.getOrElse(1.0f)

    val sx = shape.width / widthConst * sc
    val sy = shape.height / heightConst * sc

    val td = applyFinalTransform(
      transform *> scale(if (flipped) sx else -sx, -sy)
    ) foldMap
      compiler(shape)

    renderer.drawSprite(shape, td, set.texture)
  }

  private[renderer] def drawSprite(
      renderer: RenderingProgram,
      sprite: SpriteDefinition,
      transform: TransformationFD,
      set: AnimationSet,
      frame: Int
  ): Unit = {
    @inline def applyColorTransform(ot: TransformationFD): TransformationFD =
      baseSet.colorTransformMap.get(sprite.header.getColorIndex) match {
        case Some(ct) ⇒ ot *> ct
        case None ⇒ ot
      }
    sprite match {
      case s: SpriteDefinitionSingleFrame ⇒
        if (s.frameData.length <= 1) {
          for (spriteId ← s.spriteIds) {
            drawSprite(renderer,
                       spriteId,
                       applyColorTransform(transform),
                       set,
                       frame)
          }
        } else {
          val reader = new FrameDataReader(s.frameData, set.transformTable)
          for (spriteId ← s.spriteIds) {
            drawSprite(renderer,
                       spriteId,
                       reader.run() *> applyColorTransform(transform),
                       set,
                       frame)
          }
        }
      case s: SpriteDefinitionSingle ⇒
        val t =
          if (s.frameData.length <= 1)
            transform
          else {
            val reader = new FrameDataReader(s.frameData, set.transformTable)
            reader.run() *> transform
          }

        drawSprite(renderer, s.spriteId, applyColorTransform(t), set, frame)
      case s: SpriteDefinitionSingleNoAction ⇒
        val t =
          if (s.frameData.length <= 1)
            transform
          else {
            val reader = new FrameDataReader(s.frameData, set.transformTable)
            reader.run() *> transform
          }

        drawSprite(renderer, s.spriteId, applyColorTransform(t), set, frame)
      case s: SpriteDefinitionIndexed ⇒
        drawIndexed(renderer, s, transform, set, frame)
    }
  }

  private[renderer] final def drawSprite(
      renderer: RenderingProgram,
      id: Short,
      transform: TransformationFD,
      set: AnimationSet,
      frame: Int
  ): Unit = {
    val sprite = set.spritesById.getOrNull(id)
    if (sprite != null) {
      drawSprite(renderer, sprite, transform, set, frame)
    } else {
      val shape = set.shapesById.getOrNull(id)
      if (shape != null)
        drawSprite(renderer, shape, transform, set)
    }
  }

  private[this] final def drawIndexed(
      renderer: RenderingProgram,
      sprite: SpriteDefinitionIndexed,
      transform: TransformationFD,
      set: AnimationSet,
      frame: Int
  ): Unit = {
    val reader = new FrameDataReader(sprite.frameData, set.transformTable)
    val idx =
      if (set.forceStatic) 0
      else
        (frame % sprite.getFrameCount) * (if (sprite.actionInfo.isEmpty) 2
                                          else 3)
    reader.setFramePosition(sprite.framePos(idx))
    var currentSprite = sprite.framePos(idx + 1)
    val count = sprite.spriteInfo(currentSprite)
    val limit = currentSprite + count
    while (currentSprite < limit) {
      currentSprite += 1
      val spriteId = sprite.spriteInfo(currentSprite)
      drawSprite(renderer, spriteId, reader.run() *> transform, set, frame)
    }
  }

  private[renderer] final def draw(
      renderer: RenderingProgram,
      frame: Int
  ): Unit = {

    widthConst = renderer.gl.canvas.clientWidth / 3.0f
    heightConst = renderer.gl.canvas.clientHeight / 3.0f

    drawIndexed(renderer, definition, noop(), baseSet, frame)
  }
}
