// Copyright (c) 2016-2020 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.geom

import gem.`enum`.{GmosNorthFpu, GmosSouthFpu}
import gsp.math.{Angle, Offset}
import gsp.math.geom._
import gsp.math.geom.syntax.all._
import gsp.math.syntax.int._

/**
 * GMOS science area geometry.
 */
object GmosScienceAreaGeometry {

  val imaging: ShapeExpression =
    imagingFov(330340.mas, 33840.mas)

  val mos: ShapeExpression =
    imagingFov(314240.mas, 17750.mas)

  def shapeAt(
    posAngle:    Angle,
    offsetPos:   Offset,
    fpu:         Option[Either[GmosNorthFpu, GmosSouthFpu]]
  ): ShapeExpression =
    shapeFromFpu(fpu) ↗ offsetPos ⟲ posAngle

  private def shapeFromFpu(fpu: Option[Either[GmosNorthFpu, GmosSouthFpu]]): ShapeExpression =
    fpu.fold(imaging) { f => f.fold(
      n => n match {
        case GmosNorthFpu.Ns0   |
             GmosNorthFpu.Ns1   |
             GmosNorthFpu.Ns2   |
             GmosNorthFpu.Ns3   |
             GmosNorthFpu.Ns4   |
             GmosNorthFpu.Ns5   |
             GmosNorthFpu.Ifu1  |
             GmosNorthFpu.Ifu2  |
             GmosNorthFpu.Ifu3          =>
          ShapeExpression.empty

        case GmosNorthFpu.LongSlit_0_25 |
             GmosNorthFpu.LongSlit_0_50 |
             GmosNorthFpu.LongSlit_0_75 |
             GmosNorthFpu.LongSlit_1_00 |
             GmosNorthFpu.LongSlit_1_50 |
             GmosNorthFpu.LongSlit_2_00 |
             GmosNorthFpu.LongSlit_5_00 =>
          n.slitWidth.fold(ShapeExpression.empty)(longSlitFov)
      },
      s => s match {
        case GmosSouthFpu.Bhros |
             GmosSouthFpu.Ns1   |
             GmosSouthFpu.Ns2   |
             GmosSouthFpu.Ns3   |
             GmosSouthFpu.Ns4   |
             GmosSouthFpu.Ns5   |
             GmosSouthFpu.Ifu1  |
             GmosSouthFpu.Ifu2  |
             GmosSouthFpu.Ifu3  |
             GmosSouthFpu.IfuN  |
             GmosSouthFpu.IfuNB |
             GmosSouthFpu.IfuNR         =>
          ShapeExpression.empty

        case GmosSouthFpu.LongSlit_0_25 |
             GmosSouthFpu.LongSlit_0_50 |
             GmosSouthFpu.LongSlit_0_75 |
             GmosSouthFpu.LongSlit_1_00 |
             GmosSouthFpu.LongSlit_1_50 |
             GmosSouthFpu.LongSlit_2_00 |
             GmosSouthFpu.LongSlit_5_00 =>
          s.slitWidth.fold(ShapeExpression.empty)(longSlitFov)
      }
    )}

  private def imagingFov(size: Angle, corner: Angle): ShapeExpression = {
    val centerCcdWidth: Angle = 165600.mas
    val gapWidth: Angle       =   3000.mas

    val z   = Angle.Angle0

    // Used to make a square rotated by 45 degrees with side length =
    // sqrt(2 * ((size/2) + (size/2 - corner))^2) = sqrt(2*(size - corner)^2)
    // n = distance from center to any vertex of square
    val n   = size - corner

    // `ccd` is a square with the corners cut such that each missing corner is a
    // right isosceles triangle with the equal sides of length `corner`.
    val ccd = ShapeExpression.centeredRectangle(size, size) ∩
                ShapeExpression.polygonAt((z.p, n.q), (n.p, z.q), (z.p, -n.q), (-n.p, z.q))

    // Detector gap at the origin.
    val gap  = ShapeExpression.centeredRectangle(gapWidth, size)

    // Offset of detector gap from the center
    val off  = (centerCcdWidth + gapWidth).bisect.offsetInP

    // There are two gaps so three disjoint CCDs.
    ccd - (gap ↗ off) - (gap ↗ -off)
  }

  def longSlitFov(width: Angle): ShapeExpression = {
    val h = 108000.mas
    val g =   3200.mas

    val x = width.bisect
    val d = h + g

    // Slit in three sections of length `h` separated by gaps `g`.
    (-1 to 1).foldLeft(ShapeExpression.empty) { (e, i) =>
      val y = h.bisect + Angle.fromMicroarcseconds(d.toMicroarcseconds * i)
      e ∪ ShapeExpression.rectangleAt((x.p, y.q), (-x.p, (y - h).q))
    }
  }

}
