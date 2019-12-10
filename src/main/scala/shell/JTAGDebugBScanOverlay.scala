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

case class  JTAGDebugBScanOverlayParams()(implicit val p: Parameters)
case object JTAGDebugBScanOverlayKey extends Field[Seq[DesignOverlay[JTAGDebugBScanOverlayParams, ModuleValue[FlippedJTAGIO]]]](Nil)

abstract class JTAGDebugBScanOverlay(
  val params: JTAGDebugBScanOverlayParams)
    extends Overlay[ModuleValue[FlippedJTAGIO]]
{
  implicit val p = params.p

  val jtagDebugSource = BundleBridgeSource(() => new FlippedJTAGIO())
  val jtagDebugSink = shell { jtagDebugSource.makeSink }

  val designOutput = InModuleBody { jtagDebugSource.bundle}
}
