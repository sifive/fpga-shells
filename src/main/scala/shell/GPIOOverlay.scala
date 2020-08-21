// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog

import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.LogicalTreeNode

import sifive.blocks.devices.gpio._
import sifive.fpgashells.shell.xilinx._

case class GPIOShellInput() extends ShellInput
case class GPIODesignInput(gpioParams: GPIOParams, node: BundleBridgeSource[GPIOPortIO])(implicit val p: Parameters)
case class GPIOOverlayOutput() extends OverlayOutput
case object GPIOOverlayKey extends Field[Seq[TestDesignPlacer[DesignInput, GPIOShellInput, GPIOOverlayOutput]]](Seq.empty)
trait GPIOShellPlacer[Shell] extends ShellPlacer[DesignInput, GPIOShellInput, GPIOOverlayOutput]

class ShellGPIOPortIO(width: Int = 4) extends Bundle {
  val gpio = Vec(width, Analog(1.W))
}

abstract class GPIOPlacedOverlay(
  val name: String, val di: DesignInput, si: GPIOShellInput)
    extends IOPlacedOverlay[ShellGPIOPortIO, DesignInput, GPIOShellInput, GPIOOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new ShellGPIOPortIO(di.deviceAttachParams.device.asInstanceOf[GPIOParams].width)

  val tlgpioSink = shell { di.node.asInstanceOf[BundleBridgeSource[GPIOPortIO]].makeSink }
  def overlayOutput = GPIOOverlayOutput()
}
