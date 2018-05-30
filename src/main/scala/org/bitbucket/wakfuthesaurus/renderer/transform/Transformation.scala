package org.bitbucket.wakfuthesaurus.renderer.transform

import cats.free.Free
import cats.free.Free.liftF
import cats.{Id, ~>}
import org.bitbucket.wakfuthesaurus.renderer.transform.Transformation._
import org.bitbucket.wakfuthesaurus.shared.anm.ShapeDefinition

private[renderer] sealed trait TransformationA[A]

private[renderer] final case class Translate(tx: Float, ty: Float)
    extends TransformationA[TransformDefinition]

private[renderer] final case class AbsoluteTranslate(tx: Float, ty: Float)
    extends TransformationA[TransformDefinition]

private[renderer] final case class Rotate(angle: Float)
    extends TransformationA[TransformDefinition]

private[renderer] final case class RotateMatrix(rx0: Float,
                                                ry0: Float,
                                                rx: Float,
                                                ry: Float)
    extends TransformationA[TransformDefinition]

private[renderer] final case class Scale(sx: Float, sy: Float)
    extends TransformationA[TransformDefinition]

private[renderer] final case class ColorAdd(red: Float,
                                            green: Float,
                                            blue: Float,
                                            alpha: Float)
    extends TransformationA[TransformDefinition]

private[renderer] final case class ColorMultiply(red: Float,
                                                 green: Float,
                                                 blue: Float,
                                                 alpha: Float)
    extends TransformationA[TransformDefinition]

private[renderer] case object Noop extends TransformationA[TransformDefinition]

object Transformation {
  type Transformation[A] = Free[TransformationA, A]

  type TransformationFD = Transformation[TransformDefinition]

  @inline def translate(tx: Float, ty: Float): TransformationFD =
    liftF(Translate(tx, ty))

  @inline def absoluteTranslate(tx: Float, ty: Float): TransformationFD =
    liftF(AbsoluteTranslate(tx, ty))

  @inline def rotate(angle: Float): TransformationFD =
    liftF(Rotate(angle))

  @inline
  def rotate(rx0: Float, ry0: Float, rx: Float, ry: Float): TransformationFD =
    liftF(RotateMatrix(rx0, ry0, rx, ry))

  @inline def scale(sx: Float, sy: Float): TransformationFD =
    liftF(Scale(sx, sy))

  @inline
  def colorAdd(r: Float, g: Float, b: Float, a: Float): TransformationFD =
    liftF(ColorAdd(r, g, b, a))

  @inline
  def colorMultiply(r: Float, g: Float, b: Float, a: Float): TransformationFD =
    liftF(ColorMultiply(r, g, b, a))

  @inline def noop(): TransformationFD =
    liftF(Noop)

  private[renderer] def compiler(
      shape: ShapeDefinition
  ): TransformationA ~> Id =
    new (TransformationA ~> Id) {
      val origin: TransformMatrix =
        TransformMatrix.translation(
          shape.offsetX / shape.width,
          shape.offsetY / shape.height
        )

      val transform = new TransformDefinition(origin)

      override def apply[A](fa: TransformationA[A]): Id[A] =
        fa match {
          case Translate(tx, ty) ⇒
            transform.mat =
              transform.mat.translate(tx / shape.width, ty / shape.height)
            transform
          case AbsoluteTranslate(tx, ty) =>
            transform.mat = transform.mat.translate(tx, ty)
            transform
          case Rotate(angle) ⇒
            transform.mat = transform.mat.rotate(angle)
            transform
          case RotateMatrix(rx0, ry0, rx, ry) ⇒
            transform.mat = transform.mat.rotate(rx0, ry0, rx, ry)
            transform
          case Scale(sx, sy) ⇒
            transform.mat = transform.mat.scale(sx, sy)
            transform
          case ColorAdd(r, g, b, a) ⇒
            transform.r += r
            transform.g += g
            transform.b += b
            transform.a += a
            transform
          case ColorMultiply(r, g, b, a) ⇒
            transform.r *= r
            transform.g *= g
            transform.b *= b
            transform.a *= a
            transform
          case Noop ⇒
            transform
        }
    }

  final class TransformDefinition private[Transformation] (
      private[Transformation] var mat: TransformMatrix =
        TransformMatrix.identity
  ) {
    private[Transformation] var r: Float = 1
    private[Transformation] var g: Float = 1
    private[Transformation] var b: Float = 1
    private[Transformation] var a: Float = 1

    @inline def matrix: TransformMatrix = mat

    @inline def red: Float = r

    @inline def green: Float = g

    @inline def blue: Float = b

    @inline def alpha: Float = a
  }

}
