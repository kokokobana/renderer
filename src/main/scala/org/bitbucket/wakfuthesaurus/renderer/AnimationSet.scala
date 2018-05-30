package org.bitbucket.wakfuthesaurus.renderer

import org.bitbucket.wakfuthesaurus.renderer.transform.Transformation._
import org.bitbucket.wakfuthesaurus.renderer.uoption._
import org.bitbucket.wakfuthesaurus.shared.anm._
import org.scalajs.dom.raw.WebGLTexture

import scala.collection.mutable

final class AnimationSet(
    val definition: AnimationDefinition,
    toTexture: ⇒ WebGLTexture,
    val forceStatic: Boolean = false,
    val colorTransformMap: mutable.Map[Int, TransformationFD] = mutable.Map(),
    val partsFilter: Int => Boolean = { _ => true }) {
  lazy val texture: WebGLTexture = toTexture

  private[renderer] val transformTable: TransformDataTableLight =
    definition.transformIndex.getOrElse(
      TransformDataTableLight(Array(), Array(), Array())
    )

  private[renderer] val spritesById: mutable.LongMap[SpriteDefinition] =
    mutable.LongMap(
      definition.spriteDefinitions.toSeq.map(s => (s.header.id.toLong, s)): _*
    )

  private[renderer] val shapesById: mutable.LongMap[ShapeDefinition] =
    mutable.LongMap(
      definition.shapeDefinitions.toSeq.map(s ⇒ (s.id.toLong, s)): _*
    )

  private[renderer] lazy val frameAnimations: Seq[SpriteDefinitionIndexed] =
    definition.spriteDefinitions.collect {
      case s: SpriteDefinitionIndexed ⇒ s
    }.toSeq

  val frameRate: Int = definition.header.frameRate.toInt

  def loadFrameAnimation(
      id: Short,
      flipped: Boolean,
      transform: UOption[TransformationFD]
  ): Option[FrameAnimation] =
    frameAnimations
      .find(_.header.id == id)
      .map(new FrameAnimation(this, _, flipped, transform))

  def loadFrameAnimation(
      name: String,
      flipped: Boolean,
      transform: UOption[TransformationFD]
  ): Option[FrameAnimation] =
    frameAnimations
      .find(_.header.name.exists(_.name == name))
      .map(new FrameAnimation(this, _, flipped, transform))

  def loadFrameActorAnimation(
      id: Short,
      flipped: Boolean,
      transform: UOption[TransformationFD]
  ): Option[FrameActorAnimation] =
    frameAnimations
      .find(_.header.id == id)
      .map(new FrameActorAnimation(this, _, flipped, transform))

  def loadFrameActorAnimation(
    name: String,
    flipped: Boolean,
    transform: UOption[TransformationFD]
  ): Option[FrameActorAnimation] =
    frameAnimations
      .find(_.header.name.exists(_.name == name))
      .map(new FrameActorAnimation(this, _, flipped, transform))

  private[renderer] def getSpriteDefinitionByCrc(crc: Int): Option[SpriteDefinition] =
    definition.spriteDefinitions.find(_.header.name.exists(n =>
      partsFilter(n.baseNameCrc) && n.nameCrc == crc))
}
