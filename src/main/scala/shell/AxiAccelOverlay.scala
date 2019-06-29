// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import sifive.fpgashells.clocks._

case class AxiAccelOverlayParams(
  baseAddress: BigInt)(
  implicit val p: Parameters)

case object AxiAccelOverlayKey extends Field[Seq[DesignOverlay[AxiAccelOverlayParams, Unit]]](Nil)

abstract class AxiAccelOverlay[IO <: Data](val params: AxiAccelOverlayParams)
  extends IOOverlay[IO, Unit]
{
  implicit val p = params.p
}
