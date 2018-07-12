// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.uart._

case class UARTOverlayParams()(implicit val p: Parameters)
case object UARTOverlayKey extends Field[Seq[DesignOverlay[UARTOverlayParams, ModuleValue[UARTPortIO]]]](Nil)

// Tack on cts, rts signals available on some FPGAs. They are currently unused
// by our designs.
class FPGAUARTPortIO extends UARTPortIO {
  val rtsn = Output(Bool())
  val ctsn = Input(Bool())
}

abstract class UARTOverlay(
  val params: UARTOverlayParams)
    extends IOOverlay[FPGAUARTPortIO, ModuleValue[UARTPortIO]]
{
  implicit val p = params.p

  def ioFactory = new FPGAUARTPortIO

  val uartSource = BundleBridgeSource(() => new UARTPortIO)
  val uartSink = shell { uartSource.sink }

  val designOutput = InModuleBody { uartSource.out(0)._1 }

  shell { InModuleBody {
    io.txd := uartSink.io.txd
    uartSink.io.rxd := io.rxd

    // Some FPGAs have this, we don't use it.
    io.rtsn := false.B
  } }
}
