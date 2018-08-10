// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import sifive.fpgashells.clocks._

case class PCIeOverlayParams(
  wrangler: ClockAdapterNode,
  bars: Seq[AddressSet] = Seq(AddressSet(0x40000000L, 0x1FFFFFFFL)),
  ecam: BigInt = 0x2000000000L)(
  implicit val p: Parameters)

case object PCIeOverlayKey extends Field[Seq[DesignOverlay[PCIeOverlayParams, (TLNode, IntOutwardNode)]]](Nil)

abstract class PCIeOverlay[IO <: Data](val params: PCIeOverlayParams)
  extends IOOverlay[IO, (TLNode, IntOutwardNode)]
{
  implicit val p = params.p
}
