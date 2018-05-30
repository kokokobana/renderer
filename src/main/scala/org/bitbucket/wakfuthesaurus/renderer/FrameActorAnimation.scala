package org.bitbucket.wakfuthesaurus.renderer

import org.bitbucket.wakfuthesaurus.renderer.FrameActorAnimation.PartDefinition
import org.bitbucket.wakfuthesaurus.renderer.transform.Transformation._
import org.bitbucket.wakfuthesaurus.renderer.uoption._
import org.bitbucket.wakfuthesaurus.shared.anm._

import scala.collection.mutable

object FrameActorAnimation {
  private[FrameActorAnimation] final case class PartDefinition(
      sprite: SpriteDefinition,
      set: AnimationSet
  )
}

final class FrameActorAnimation(
    baseSet: AnimationSet,
    definition: SpriteDefinitionIndexed,
    flipped: Boolean = false,
    finalTransform: UOption[TransformationFD] = UNone
) extends FrameAnimation(baseSet, definition, flipped, finalTransform) {

  private[this] var canHidePartItems: Set[Int] = Set()
  private[this] var gearSetsByCrc: mutable.LongMap[PartDefinition] =
    mutable.LongMap()

  private[this] def isPartHidden(
      set: AnimationSet,
      crc: Int
  ): Boolean =
    set.definition.partsHiddenByItem.toIterator
      .filter(_.crcToHide == crc)
      .exists(part => canHidePartItems.contains(part.crcKey))

  private[renderer] override def drawSprite(
      renderer: RenderingProgram,
      sprite: SpriteDefinition,
      transform: TransformationFD,
      set: AnimationSet,
      frame: Int
  ): Unit = {
    if (sprite.header.name.isDefined) {
      val name = sprite.header.name.get
      if (!isPartHidden(set, name.baseNameCrc)) {
        val part = gearSetsByCrc.getOrNull(name.nameCrc)
        if (part != null)
          super.drawSprite(renderer, part.sprite, transform, part.set, frame)
        else
          super.drawSprite(renderer, sprite, transform, set, frame)
      }
    }
  }

  def setElements(sets: Seq[AnimationSet], parts: Seq[String]): Unit = {

    def collectUnique[A, B](ls: List[A], fn: A => B) = {
      def loop(set: Set[B], ls: List[A]): List[A] = ls match {
        case hd :: tail if set.contains(fn(hd)) => loop(set, tail)
        case hd :: tail => hd :: loop(set + fn(hd), tail)
        case Nil => Nil
      }

      loop(Set(), ls)
    }

    val partsToHide = parts.map(_.hashCode).toSet

    canHidePartItems = {
      for {
        sprite <- baseSet.definition.spriteDefinitions
        name <- sprite.header.name
        set <- sets.find(_.getSpriteDefinitionByCrc(name.nameCrc).isDefined)
      } yield set
    }.flatMap(_.definition.canHidePartItems.map(_.crcKey)).toSet

    val visibleGearParts =
      {
        for {
          set <- sets.toIterator
          sd <- set.definition.spriteDefinitions.toIterator
          name <- sd.header.name
        } yield (name, PartDefinition(sd, set))
      }.filter(Function.tupled { (n, part) =>
          !partsToHide.contains(n.baseNameCrc) && part.set.partsFilter(
            n.baseNameCrc
          )
        })
        .map(Function.tupled((k, v) => k.nameCrc.toLong -> v))
        .toList

    gearSetsByCrc = mutable.LongMap(
      collectUnique[(Long, PartDefinition), Long](visibleGearParts, _._1): _*
    )
  }
}
