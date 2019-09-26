// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.gpio._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import chisel3.experimental._

case class GPIOOverlayParams(gpioParam: GPIOParams, controlBus: TLBusWrapper, intNode: IntInwardNode)(implicit val p: Parameters)
case object GPIOOverlayKey extends Field[Seq[DesignOverlay[GPIOOverlayParams, TLGPIO]]](Nil)

class ShellGPIOPortIO(width: Int = 4) extends Bundle {
  val gpio = Vec(width, Analog(1.W))
}

abstract class GPIOOverlay(
  val params: GPIOOverlayParams)
    extends IOOverlay[ShellGPIOPortIO, TLGPIO]
{
  implicit val p = params.p

  def ioFactory = new ShellGPIOPortIO(params.gpioParam.width)
  val tlgpio = GPIO.attach(GPIOAttachParams(params.gpioParam, params.controlBus, params.intNode))

  val tlgpioSink = shell { tlgpio.ioNode.makeSink }
  val designOutput = tlgpio
}
