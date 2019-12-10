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
case object JTAGDebugBScanOverlayKey extends Field[Seq[DesignPlacer[JTAGDebugDesignInput, JTAGDebugShellInput, JTAGDebugOverlayOutput]]](Nil)
trait JTAGDebugBScanShellPlacer[Shell] extends ShellPlacer[JTAGDebugDesignInput, JTAGDebugShellInput, JTAGDebugOverlayOutput]

case object JTAGDebugBScanOverlayKey extends Field[Seq[DesignOverlay[JTAGDebugBScanOverlayParams, ModuleValue[FlippedJTAGIO]]]](Nil)

abstract class JTAGDebugBScanPlacedOverlay(
  val name: String, val di: JTAGDebugBScanDesignInput, val si: JTAGDebugBScanShellInput)
    extends PlacedOverlay[ModuleValue[FlippedJTAGIO]]
{
  implicit val p = di.p

  val jtagDebugSource = BundleBridgeSource(() => new FlippedJTAGIO())
  val jtagDebugSink = shell { jtagDebugSource.makeSink }
  val jtout = InModuleBody { jtagDebugSource.bundle}
  def overlayOutput = JTAGDebugOverlayOutput(jtag = jtout)
}
