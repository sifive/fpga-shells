// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import sifive.fpgashells.clocks._

case class DDROverlayParams(
  baseAddress: BigInt,
  wrangler: ClockAdapterNode)(
  implicit val p: Parameters)

case object DDROverlayKey extends Field[Seq[DesignOverlay[DDROverlayParams, TLInwardNode]]](Nil)

abstract class DDROverlay[IO <: Data](val params: DDROverlayParams)
  extends IOOverlay[IO, TLInwardNode]
{
  implicit val p = params.p
}
