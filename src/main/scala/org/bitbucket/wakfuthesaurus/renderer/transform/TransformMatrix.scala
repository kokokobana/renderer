package org.bitbucket.wakfuthesaurus.renderer.transform

import scala.scalajs.js.typedarray.Float32Array

/**
  *  3x3 matrix class for efficient 2d transformation calculations
  *  backed by raw js.Array to avoid indirection
  */
private[renderer] final case class TransformMatrix private[TransformMatrix] (
    buffer: Float32Array)
    extends AnyVal {
  @inline def get(x: Int, y: Int): Float = buffer(x * 3 + y)

  def *(other: TransformMatrix): TransformMatrix = {
    val a00 = buffer(0 * 3 + 0)
    val a01 = buffer(0 * 3 + 1)
    val a02 = buffer(0 * 3 + 2)
    val a10 = buffer(1 * 3 + 0)
    val a11 = buffer(1 * 3 + 1)
    val a12 = buffer(1 * 3 + 2)
    val a20 = buffer(2 * 3 + 0)
    val a21 = buffer(2 * 3 + 1)
    val a22 = buffer(2 * 3 + 2)
    val b00 = other.buffer(0 * 3 + 0)
    val b01 = other.buffer(0 * 3 + 1)
    val b02 = other.buffer(0 * 3 + 2)
    val b10 = other.buffer(1 * 3 + 0)
    val b11 = other.buffer(1 * 3 + 1)
    val b12 = other.buffer(1 * 3 + 2)
    val b20 = other.buffer(2 * 3 + 0)
    val b21 = other.buffer(2 * 3 + 1)
    val b22 = other.buffer(2 * 3 + 2)
    TransformMatrix.create(
      a00 * b00 + a01 * b10 + a02 * b20,
      a00 * b01 + a01 * b11 + a02 * b21,
      a00 * b02 + a01 * b12 + a02 * b22,
      a10 * b00 + a11 * b10 + a12 * b20,
      a10 * b01 + a11 * b11 + a12 * b21,
      a10 * b02 + a11 * b12 + a12 * b22,
      a20 * b00 + a21 * b10 + a22 * b20,
      a20 * b01 + a21 * b11 + a22 * b21,
      a20 * b02 + a21 * b12 + a22 * b22
    )
  }

  @inline def rotate(angle: Float): TransformMatrix =
    this * TransformMatrix.rotation(angle)

  @inline
  def rotate(rx0: Float,
             ry0: Float,
             rx1: Float,
             ry1: Float,
             rx2: Float = 0,
             ry2: Float = 0): TransformMatrix =
    this * TransformMatrix.rotation(rx0, ry0, rx1, ry1, rx2, ry2)

  @inline def translate(tx: Float, ty: Float): TransformMatrix =
    this * TransformMatrix.translation(tx, ty)

  @inline def scale(sx: Float, sy: Float): TransformMatrix =
    this * TransformMatrix.scale(sx, sy)

  @inline def toFloat32Array: Float32Array = buffer
}

private[renderer] object TransformMatrix {
  val identity: TransformMatrix =
    TransformMatrix.create(1, 0, 0, 0, 1, 0, 0, 0, 1)

  def create(x1: Float,
             y1: Float,
             z1: Float,
             x2: Float,
             y2: Float,
             z2: Float,
             x3: Float,
             y3: Float,
             z3: Float): TransformMatrix = {
    val buffer = new Float32Array(9)
    buffer(0) = x1
    buffer(1) = y1
    buffer(2) = z1
    buffer(3) = x2
    buffer(4) = y2
    buffer(5) = z2
    buffer(6) = x3
    buffer(7) = y3
    buffer(8) = z3
    TransformMatrix(buffer)
  }

  @inline def translation(tx: Float, ty: Float): TransformMatrix =
    TransformMatrix.create(1, 0, 0, 0, 1, 0, tx, ty, 1)

  @inline def rotation(angle: Float): TransformMatrix = {
    val c = Math.cos(angle).toFloat
    val s = Math.sin(angle).toFloat
    TransformMatrix.create(c, -s, 0, s, c, 0, 0, 0, 1)
  }

  @inline
  def rotation(
      rx0: Float,
      ry0: Float,
      rx1: Float,
      ry1: Float,
      rx2: Float = 0,
      ry2: Float = 0
  ): TransformMatrix =
    TransformMatrix.create(rx0, ry0, 0, rx1, ry1, 0, rx2, ry2, 1)

  @inline def scale(sx: Float, sy: Float): TransformMatrix =
    TransformMatrix.create(sx, 0, 0, 0, sy, 0, 0, 0, 1)
}
