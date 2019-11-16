// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

//Core-To-Shell Reset Overlay: No IOs, but passes a Bool into the shell to be orred into the pllReset, allowing core signals to reset the shell

case class CTSResetShellInput()
case class CTSResetDesignInput(rst: Bool)(implicit val p: Parameters)
case class CTSResetOverlayOutput()
case object CTSResetOverlayKey extends Field[Seq[DesignPlacer[CTSResetDesignInput, CTSResetShellInput, CTSResetOverlayOutput]]](Nil)
trait CTSResetShellPlacer[Shell] extends ShellPlacer[CTSResetDesignInput, CTSResetShellInput, CTSResetOverlayOutput]

abstract class CTSResetPlacedOverlay(
  val name: String, val di: CTSResetDesignInput, si: CTSResetShellInput)
    extends PlacedOverlay[CTSResetDesignInput, CTSResetShellInput, CTSResetOverlayOutput]
{
  implicit val p = di.p

  def overlayOutput = CTSResetOverlayOutput()
}
