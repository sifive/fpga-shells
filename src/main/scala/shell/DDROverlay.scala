// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import sifive.fpgashells.clocks._


case class DDRShellInput()
case class DDRDesignInput(
  baseAddress: BigInt,
  wrangler: ClockAdapterNode,
  corePLL: PLLNode,
  vc7074gbdimm: Boolean = false)(
  implicit val p: Parameters)
case class DDROverlayOutput(ddr: TLInwardNode)
trait DDRShellPlacer[Shell] extends ShellPlacer[DDRDesignInput, DDRShellInput, DDROverlayOutput]

case object DDROverlayKey extends Field[Seq[DesignPlacer[DDRDesignInput, DDRShellInput, DDROverlayOutput]]](Nil)

abstract class DDRPlacedOverlay[IO <: Data](val name: String, val di: DDRDesignInput, val si: DDRShellInput)
  extends IOPlacedOverlay[IO, DDRDesignInput, DDRShellInput, DDROverlayOutput]
{
  implicit val p = di.p
}
