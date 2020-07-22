// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import sifive.fpgashells.clocks._

case class PCIeShellInput()
case class PCIeDesignInput(
  wrangler: ClockAdapterNode,
  bars: Seq[AddressSet] = Seq(AddressSet(0x40000000L, 0x1FFFFFFFL)),
  ecam: BigInt = 0x2000000000L,
  bases: Seq[BigInt] = Nil, // remap bars to these PCIe base addresses
  corePLL: PLLNode)(
  implicit val p: Parameters)

case class PCIeOverlayOutput(
  pcieNode: TLNode,
  intNode: IntOutwardNode)
trait PCIeShellPlacer[Shell] extends ShellPlacer[PCIeDesignInput, PCIeShellInput, PCIeOverlayOutput]

case object PCIeOverlayKey extends Field[Seq[DesignPlacer[PCIeDesignInput, PCIeShellInput, PCIeOverlayOutput]]](Nil)

abstract class PCIePlacedOverlay[IO <: Data](
  val name: String, val di: PCIeDesignInput, val si: PCIeShellInput)
    extends IOPlacedOverlay[IO, PCIeDesignInput, PCIeShellInput, PCIeOverlayOutput]
{
  implicit val p = di.p
}
