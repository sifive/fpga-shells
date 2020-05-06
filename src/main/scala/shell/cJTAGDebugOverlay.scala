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

case class cJTAGDebugShellInput(
  color: String = "",
  header: String = "",
  rgb: Bool = false.B,
  number: Int = 0)

case class cJTAGDebugDesignInput()(implicit val p: Parameters)
case class cJTAGDebugOverlayOutput(cjtag: FPGAcJTAGSignals)
trait cJTAGDebugShellPlacer[Shell] extends ShellPlacer[cJTAGDebugDesignInput, cJTAGDebugShellInput, cJTAGDebugOverlayOutput]

case object cJTAGDebugOverlayKey extends Field[Seq[DesignPlacer[cJTAGDebugDesignInput, cJTAGDebugShellInput, cJTAGDebugOverlayOutput]]](Nil)

class FPGAcJTAGIO extends Bundle {
  // cJTAG
  val cjtag_TCKC = Analog(1.W)
  val cjtag_TMSC = Analog(1.W)
  val srst_n     = Analog(1.W)
}

class FPGAcJTAGSignals extends Bundle {
  val tckc_pin = Input(Clock())
  val tmsc_pin = new BasePin()
  val srst_n   = Input(Bool())
}

abstract class cJTAGDebugPlacedOverlay(
  val name: String, val di: cJTAGDebugDesignInput, val si: cJTAGDebugShellInput)
    extends IOPlacedOverlay[FPGAcJTAGIO, cJTAGDebugDesignInput, cJTAGDebugShellInput, cJTAGDebugOverlayOutput]
{
  implicit val p = di.p
  def ioFactory = new FPGAcJTAGIO

  val cjtagDebugSource = BundleBridgeSource(() => new FPGAcJTAGSignals)
  val cjtagDebugSink = shell { cjtagDebugSource.makeSink }

  def overlayOutput = cJTAGDebugOverlayOutput(cjtag = cjtagDebugSource.bundle )

  shell { InModuleBody {
    cjtagDebugSink.bundle.tckc_pin := AnalogToUInt(io.cjtag_TCKC).asBool.asClock
    IOBUF(io.cjtag_TMSC, cjtagDebugSink.bundle.tmsc_pin)
    KEEPER(io.cjtag_TMSC)
    cjtagDebugSink.bundle.srst_n := IOBUF(io.srst_n)
  } }
}
