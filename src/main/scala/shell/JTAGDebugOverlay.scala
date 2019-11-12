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

case class JTAGDebugOverlayParams()(implicit val p: Parameters)
case object JTAGDebugOverlayKey extends Field[Seq[DesignOverlay[JTAGDebugOverlayParams, ModuleValue[FlippedJTAGIO]]]](Nil)

class ShellJTAGIO extends Bundle { // st: should be redefined with an additoinal signal: tdo_en as it's in patch verilog 
  // JTAG
  val jtag_TCK = Analog(1.W)
  val jtag_TMS = Analog(1.W)
  val jtag_TDI = Analog(1.W)
  val jtag_TDO = Analog(1.W)
}

// TODO: Fix interaction of BundleBridge/Flipped to get rid of this Bundle
class FlippedJTAGIO extends Bundle {
  val TCK = Input(Clock())
  val TMS = Input(Bool())
  val TDI = Input(Bool())
  val TDO = Output(new Tristate())
}

abstract class JTAGDebugOverlay(
  val params: JTAGDebugOverlayParams)
    extends IOOverlay[ShellJTAGIO, ModuleValue[FlippedJTAGIO]]
{
  implicit val p = params.p

  def ioFactory = new ShellJTAGIO

  val jtagDebugSource = BundleBridgeSource(() => new FlippedJTAGIO())
  val jtagDebugSink = shell { jtagDebugSource.makeSink }

  val designOutput = InModuleBody { jtagDebugSource.bundle}
}
