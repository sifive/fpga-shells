// See LICENSE for license details
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._

case class ArtyEthernetOverlayParams(
  address: BigInt,
  wrangler: ClockAdapterNode,
  corePLL: PLLNode,
  intNode: IntInwardNode)(
  implicit val p: Parameters)

case object ArtyEthernetOverlayKey extends Field[Seq[DesignOverlay[ArtyEthernetOverlayParams, TLInwardNode]]](Nil)

abstract class ArtyEthernetOverlay[IO <: Data](val params: ArtyEthernetOverlayParams)
  extends IOOverlay[IO, TLInwardNode]
{
  implicit val p = params.p
}
