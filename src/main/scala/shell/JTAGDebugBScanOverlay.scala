// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental._
import freechips.rocketchip.config._
import freechips.rocketchip.util._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.jtag._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import sifive.fpgashells.ip.xilinx._

case class JTAGDebugBScanShellInput()
case class JTAGDebugBScanDesignInput()(implicit val p: Parameters)
case class JTAGDebugBScanOverlayOutput(jtag: ModuleValue[FlippedJTAGIO])
case object JTAGDebugBScanOverlayKey extends Field[Seq[DesignPlacer[JTAGDebugBScanDesignInput, JTAGDebugBScanShellInput, JTAGDebugBScanOverlayOutput]]](Nil)
trait JTAGDebugBScanShellPlacer[Shell] extends ShellPlacer[JTAGDebugBScanDesignInput, JTAGDebugBScanShellInput, JTAGDebugBScanOverlayOutput]

abstract class JTAGDebugBScanPlacedOverlay(
  val name: String, val di: JTAGDebugBScanDesignInput, val si: JTAGDebugBScanShellInput)
    extends PlacedOverlay[JTAGDebugBScanDesignInput, JTAGDebugBScanShellInput, JTAGDebugBScanOverlayOutput]
{
  implicit val p = di.p
  def shell: Shell

  val jtagDebugSource = BundleBridgeSource(() => new FlippedJTAGIO())
  val jtagDebugSink = shell { jtagDebugSource.makeSink }
  val jtout = InModuleBody { jtagDebugSource.bundle}
  def overlayOutput = JTAGDebugBScanOverlayOutput(jtag = jtout)
}
