// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.util._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import sifive.fpgashells.ip.xilinx._

case class JTAGDebugOverlayParams()(implicit val p: Parameters)
case object JTAGDebugOverlayKey extends Field[Seq[DesignOverlay[JTAGDebugOverlayParams, ModuleValue[FPGAJTAGIO]]]](Nil)

class FPGAJTAGIO extends Bundle {
  // JTAG
  val jtag_TCK = Input(Clock())
  val jtag_TMS = Input(Bool())
  val jtag_TDI = Input(Bool())
  val jtag_TDO = Output(Bool())
}

abstract class JTAGDebugOverlay(
  val params: JTAGDebugOverlayParams)
    extends IOOverlay[FPGAJTAGIO, ModuleValue[FPGAJTAGIO]]
{
  implicit val p = params.p

  def ioFactory = new FPGAJTAGIO

  val jtagDebugSource = BundleBridgeSource(() => new FPGAJTAGIO)
  val jtagDebugSink = shell { jtagDebugSource.makeSink }

  val designOutput = InModuleBody { jtagDebugSource.bundle}

  shell { InModuleBody {
    io <> jtagDebugSink.bundle
  } }
}
