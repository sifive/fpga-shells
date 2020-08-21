// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog

import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.LogicalTreeNode

import sifive.blocks.devices.uart._
import sifive.fpgashells.shell.xilinx._

case class UARTShellInput(index: Int = 0) extends ShellInput
case class UARTDesignInput(node: BundleBridgeSource[UARTPortIO])(implicit val p: Parameters)
case class UARTOverlayOutput() extends OverlayOutput
case object UARTOverlayKey extends Field[Seq[TestDesignPlacer[DesignInput, UARTShellInput, UARTOverlayOutput]]](Nil)
trait UARTShellPlacer[Shell] extends ShellPlacer[DesignInput, UARTShellInput, UARTOverlayOutput]

// Tack on cts, rts signals available on some FPGAs. They are currently unused
// by our designs.
class ShellUARTPortIO(val flowControl: Boolean) extends Bundle {
  val txd = Analog(1.W)
  val rxd = Analog(1.W)
  val rtsn = if (flowControl) Some(Analog(1.W)) else None
  val ctsn = if (flowControl) Some(Analog(1.W)) else None
}

abstract class UARTPlacedOverlay(
  val name: String, val di: DesignInput, val si: UARTShellInput, val flowControl: Boolean)
    extends IOPlacedOverlay[ShellUARTPortIO, DesignInput, UARTShellInput, UARTOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new ShellUARTPortIO(flowControl)

  val tluartSink = shell { di.node.asInstanceOf[BundleBridgeSource[UARTPortIO]].makeSink }

  def overlayOutput = UARTOverlayOutput()
}
