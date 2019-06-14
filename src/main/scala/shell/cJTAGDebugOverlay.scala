// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog
import freechips.rocketchip.config._
import freechips.rocketchip.util._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import sifive.fpgashells.ip.xilinx._

case class cJTAGDebugOverlayParams()(implicit val p: Parameters)
case object cJTAGDebugOverlayKey extends Field[Seq[DesignOverlay[cJTAGDebugOverlayParams, ModuleValue[FPGAcJTAGIO]]]](Nil)

class FPGAcJTAGIO extends Bundle {
  // cJTAG
  val cjtag_TCKC = Analog(1.W)
  val cjtag_TMSC = Analog(1.W)
  val srst_n     = Analog(1.W)
}

abstract class cJTAGDebugOverlay(
  val params: cJTAGDebugOverlayParams)
    extends IOOverlay[FPGAcJTAGIO, ModuleValue[FPGAcJTAGIO]]
{
  implicit val p = params.p
  def ioFactory = new FPGAcJTAGIO

  val cjtagDebugSource = BundleBridgeSource(() => new FPGAcJTAGIO)
  val cjtagDebugSink = shell { cjtagDebugSource.makeSink }

  val designOutput = InModuleBody { cjtagDebugSource.bundle}

  shell { InModuleBody {
    io <> cjtagDebugSink.bundle
  } }
}
