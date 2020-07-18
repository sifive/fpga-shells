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

case class GPIOShellInput()
case class GPIODesignInput(gpioParams: GPIOParams, node: BundleBridgeSource[GPIOPortIO])(implicit val p: Parameters)
case class GPIOOverlayOutput()
case object GPIOOverlayKey extends Field[Seq[DesignPlacer[GPIODesignInput, GPIOShellInput, GPIOOverlayOutput]]](Nil)
trait GPIOShellPlacer[Shell] extends ShellPlacer[GPIODesignInput, GPIOShellInput, GPIOOverlayOutput]

class ShellGPIOPortIO(val numGPIOs: Int = 4) extends Bundle {
  val gpio = Vec(numGPIOs, Analog(1.W))
}

abstract class GPIOPlacedOverlay(
  val name: String, val di: GPIODesignInput, si: GPIOShellInput)
    extends IOPlacedOverlay[ShellGPIOPortIO, GPIODesignInput, GPIOShellInput, GPIOOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new ShellGPIOPortIO(di.gpioParams.width)

  val tlgpioSink = sinkScope { di.node.makeSink }
  def overlayOutput = GPIOOverlayOutput()
}
