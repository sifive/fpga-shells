// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.uart._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode

//TODO: Can this be combined with UARTAttachParams?
case class UARTOverlayParams(uartParams: UARTParams, divInit: Int, controlBus: TLBusWrapper, intNode: IntInwardNode)(implicit val p: Parameters)
case object UARTOverlayKey extends Field[Seq[DesignOverlay[UARTOverlayParams, TLUART]]](Nil)

// Tack on cts, rts signals available on some FPGAs. They are currently unused
// by our designs.
class FPGAUARTPortIO(flowControl: Boolean) extends UARTPortIO {
  val rtsn = if (flowControl) Some(Output(Bool())) else None
  val ctsn = if (flowControl) Some(Input(Bool())) else None
}

//class UARTReplacementBundle extends Bundle with HasUARTTopBundleContents

abstract class UARTOverlay(
  val params: UARTOverlayParams, flowControl: Boolean)
    extends IOOverlay[FPGAUARTPortIO, TLUART]
{
  implicit val p = params.p

  def ioFactory = new FPGAUARTPortIO(flowControl)

  val tluart = UART.attach(UARTAttachParams(params.uartParams, params.divInit, params.controlBus, params.intNode))
  val tluartSink = tluart.ioNode.makeSink
  val uartSource = BundleBridgeSource(() => new UARTPortIO())
  val uartSink = shell { uartSource.makeSink }

  val designOutput = tluart

  InModuleBody {
    val (io, _) = uartSource.out(0)
    val tluartport = tluartSink.bundle
    io <> tluartport
    tluartport.rxd := RegNext(RegNext(io.rxd))
  }

  shell { InModuleBody {
    io.txd := uartSink.bundle.txd
    uartSink.bundle.rxd := io.rxd
  } }
}
