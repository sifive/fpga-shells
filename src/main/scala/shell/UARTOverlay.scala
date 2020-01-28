// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.uart._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode

//dont make the controller here
//move flowcontrol to shell input?? 
case class UARTShellInput(index: Int = 0)
case class UARTDesignInput(uartParams: UARTParams, divInit: Int, controlBus: TLBusWrapper, intNode: IntInwardNode)(implicit val p: Parameters)
case class UARTOverlayOutput(uart: TLUART)
case object UARTOverlayKey extends Field[Seq[DesignPlacer[UARTDesignInput, UARTShellInput, UARTOverlayOutput]]](Nil)
trait UARTShellPlacer[Shell] extends ShellPlacer[UARTDesignInput, UARTShellInput, UARTOverlayOutput]

// Tack on cts, rts signals available on some FPGAs. They are currently unused
// by our designs.
class ShellUARTPortIO(val flowControl: Boolean) extends Bundle {
  val txd = Analog(1.W)
  val rxd = Analog(1.W)
  val rtsn = if (flowControl) Some(Analog(1.W)) else None
  val ctsn = if (flowControl) Some(Analog(1.W)) else None
}

//class UARTReplacementBundle extends Bundle with HasUARTTopBundleContents

abstract class UARTPlacedOverlay(
  val name: String, val di: UARTDesignInput, val si: UARTShellInput, val flowControl: Boolean)
    extends IOPlacedOverlay[ShellUARTPortIO, UARTDesignInput, UARTShellInput, UARTOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new ShellUARTPortIO(flowControl)

  val tluart = UART.attach(UARTAttachParams(di.uartParams, di.divInit, di.controlBus, di.intNode))
  val tluartSink = tluart.ioNode.makeSink
  val uartSource = BundleBridgeSource(() => new UARTPortIO())
  val uartSink = shell { uartSource.makeSink }

  def overlayOutput = UARTOverlayOutput(uart = tluart)
}
