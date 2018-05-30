package org.bitbucket.wakfuthesaurus.renderer.transform

import cats.syntax.apply._
import org.bitbucket.wakfuthesaurus.renderer.transform.Transformation._
import org.bitbucket.wakfuthesaurus.shared.anm._

/**
  * Decoder for the Ankama animation format
  */
final class FrameDataReader(frameData: FrameData,
                            transformTable: TransformDataTableLight) {
  private trait IndexedColl[T] {
    def apply(idx: Int): T
  }

  private[this] var framePosition = 0

  private[this] val rawFrameData: IndexedColl[Int] = {
    frameData match {
      case ByteFrameData(data) ⇒
        (idx: Int) =>
          data(idx) & 0xFF
      case ShortFrameData(data) ⇒
        (idx: Int) =>
          data(idx) & 0xFFFF
      case IntFrameData(data) ⇒
        (idx: Int) =>
          data(idx)
    }
  }

  @inline private[this] def readData(): Int = {
    val value = rawFrameData(framePosition)
    framePosition += 1
    value
  }

  def setFramePosition(idx: Int): Unit =
    framePosition = idx

  private[this] def processTranslation(): TransformationFD = {
    val idx = readData()
    translate(
      transformTable.translations(idx),
      transformTable.translations(idx + 1)
    )
  }

  private[this] def processRotation(): TransformationFD = {
    val idx = readData()
    rotate(
      transformTable.rotations(idx),
      transformTable.rotations(idx + 1),
      transformTable.rotations(idx + 2),
      transformTable.rotations(idx + 3)
    )
  }

  private[this] def processColorMult(): TransformationFD = {
    val idx = readData()
    colorMultiply(
      transformTable.colors(idx),
      transformTable.colors(idx + 1),
      transformTable.colors(idx + 2),
      transformTable.colors(idx + 3)
    )
  }

  private[this] def processColorAdd(): TransformationFD = {
    val idx = readData()
    colorAdd(
      transformTable.colors(idx),
      transformTable.colors(idx + 1),
      transformTable.colors(idx + 2),
      transformTable.colors(idx + 3)
    )
  }

  def run(): TransformationFD = {
    val tpe = readData()
    tpe match {
      case 3 ⇒
        processRotation() *>
          processTranslation()
      case 2 ⇒
        processTranslation()
      case 0 ⇒
        noop()
      case 8 ⇒
        processColorAdd()
      case 12 ⇒
        processColorMult() *>
          processColorAdd()
      case 4 ⇒
        processColorMult()
      case 1 ⇒
        processRotation()
      case 9 ⇒
        processColorAdd() *>
          processRotation()
      case 13 ⇒
        processColorMult() *>
          processColorAdd() *>
          processRotation()
      case 5 ⇒
        processColorMult() *>
          processRotation()
      case 11 ⇒
        processColorAdd() *>
          processRotation() *>
          processTranslation()
      case 15 ⇒
        processColorMult() *>
          processColorAdd() *>
          processRotation() *>
          processTranslation()
      case 7 ⇒
        processColorMult() *>
          processRotation() *>
          processTranslation()
      case 10 ⇒
        processColorAdd() *>
          processTranslation()
      case 14 ⇒
        processColorMult() *>
          processColorAdd() *>
          processTranslation()
      case 6 ⇒
        processColorMult() *>
          processTranslation()
    }
  }
}
