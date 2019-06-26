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
import sifive.blocks.devices.pinctrl._

case class cJTAGDebugOverlayParams()(implicit val p: Parameters)
case object cJTAGDebugOverlayKey extends Field[Seq[DesignOverlay[cJTAGDebugOverlayParams, ModuleValue[FPGAcJTAGSignals]]]](Nil)

class FPGAcJTAGIO extends Bundle {
  // cJTAG
  val cjtag_TCKC = Analog(1.W)
  val cjtag_TMSC = Analog(1.W)
  val srst_n     = Analog(1.W)
}

class FPGAcJTAGSignals extends Bundle {
  val tckc_pin = Input(Clock())
  val tmsc_pin = new BasePin()
}

abstract class cJTAGDebugOverlay(
  val params: cJTAGDebugOverlayParams)
    extends IOOverlay[FPGAcJTAGIO, ModuleValue[FPGAcJTAGSignals]]
{
  implicit val p = params.p
  def ioFactory = new FPGAcJTAGIO

  val cjtagDebugSource = BundleBridgeSource(() => new FPGAcJTAGSignals)
  val cjtagDebugSink = shell { cjtagDebugSource.makeSink }

  val designOutput = InModuleBody { cjtagDebugSource.bundle}

  shell { InModuleBody {
    cjtagDebugSink.bundle.tckc_pin := IOBUF(io.cjtag_TCKC).asClock
    IOBUF(io.cjtag_TMSC, cjtagDebugSink.bundle.tmsc_pin)
    KEEPER(io.cjtag_TMSC)
  } }
}
