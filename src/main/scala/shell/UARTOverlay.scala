// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.uart._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}

case class UARTOverlayParams(beatBytes: Int)(implicit val p: Parameters)
case object UARTOverlayKey extends Field[Seq[DesignOverlay[UARTOverlayParams, TLUART]]](Nil)

// Tack on cts, rts signals available on some FPGAs. They are currently unused
// by our designs.
class FPGAUARTPortIO extends UARTPortIO {
  val rtsn = Output(Bool())
  val ctsn = Input(Bool())
}

abstract class UARTOverlay(
  val params: UARTOverlayParams)
    extends IOOverlay[FPGAUARTPortIO, TLUART]
{
  implicit val p = params.p

  def ioFactory = new FPGAUARTPortIO

  private val divinit = (p(PeripheryBusKey).frequency / 115200).toInt
  val uartParam = p(PeripheryUARTKey).map(_.copy(divisorInit = divinit))
  val use_name = Some(s"uart_0")

//  val uartSource = BundleBridge()
//  val uartSink = shell { uartSource }

  val designOutput =   LazyModule(new TLUART(params.beatBytes, uartParam(0)).suggestName(use_name))









  shell { InModuleBody {
//    io.txd := designOutput.module.io.port.txd

    // Some FPGAs have this, we don't use it.
    io.rtsn := false.B
  } }
}
