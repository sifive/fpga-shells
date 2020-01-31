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

//Might delete later...
//Should GPIO be an overlay? Probably not, PinOverlay might take over this use case
//Plus this should NOT place the controller
case class GPIOShellInput()
case class GPIODesignInput(
  gpioParam: GPIOParams,
  controlBus: TLBusWrapper,
  intNode: IntInwardNode,
  parentLogicalTreeNode: Option[LogicalTreeNode] = None)(implicit val p: Parameters)
case class GPIOOverlayOutput(gpio: TLGPIO)
case object GPIOOverlayKey extends Field[Seq[DesignPlacer[GPIODesignInput, GPIOShellInput, GPIOOverlayOutput]]](Nil)
trait GPIOShellPlacer[Shell] extends ShellPlacer[GPIODesignInput, GPIOShellInput, GPIOOverlayOutput]

class ShellGPIOPortIO(width: Int = 4) extends Bundle {
  val gpio = Vec(width, Analog(1.W))
}

abstract class GPIOPlacedOverlay(
  val name: String, val di: GPIODesignInput, si: GPIOShellInput)
    extends IOPlacedOverlay[ShellGPIOPortIO, GPIODesignInput, GPIOShellInput, GPIOOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new ShellGPIOPortIO(di.gpioParam.width)
  val tlgpio = GPIO.attach(GPIOAttachParams(
    gpio = di.gpioParam,
    controlBus = di.controlBus,
    intNode = di.intNode,
    parentLogicalTreeNode = di.parentLogicalTreeNode))
  val tlgpioSink = shell { tlgpio.ioNode.makeSink }
  def overlayOutput = GPIOOverlayOutput(gpio = tlgpio)
}
