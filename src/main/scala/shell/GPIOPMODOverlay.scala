// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.gpio._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import chisel3.experimental._

case class GPIOPMODOverlayParams(gpioParam: GPIOParams, controlBus: TLBusWrapper, intNode: IntInwardNode)(implicit val p: Parameters)
case object GPIOPMODOverlayKey extends Field[Seq[DesignOverlay[GPIOPMODOverlayParams, TLGPIO]]](Nil)

class GPIOPMODPortIO extends Bundle {
  val gpio_pmod_0 = Analog(1.W)
  val gpio_pmod_1 = Analog(1.W)
  val gpio_pmod_2 = Analog(1.W)
  val gpio_pmod_3 = Analog(1.W)
}

abstract class GPIOPMODOverlay(
  val params: GPIOPMODOverlayParams)
    extends IOOverlay[GPIOPMODPortIO, TLGPIO]
{
  implicit val p = params.p

  def ioFactory = new GPIOPMODPortIO
  val tlgpio = GPIO.attach(GPIOAttachParams(params.gpioParam, params.controlBus, params.intNode))

  val tlgpioSink = shell { tlgpio.ioNode.makeSink }
  val designOutput = tlgpio

  shell { InModuleBody {
    UIntToAnalog(tlgpioSink.bundle.pins(0).o.oval, io.gpio_pmod_0, tlgpioSink.bundle.pins(0).o.oe)
    UIntToAnalog(tlgpioSink.bundle.pins(1).o.oval, io.gpio_pmod_1, tlgpioSink.bundle.pins(1).o.oe)
    UIntToAnalog(tlgpioSink.bundle.pins(2).o.oval, io.gpio_pmod_2, tlgpioSink.bundle.pins(2).o.oe)
    UIntToAnalog(tlgpioSink.bundle.pins(3).o.oval, io.gpio_pmod_3, tlgpioSink.bundle.pins(3).o.oe)

    tlgpioSink.bundle.pins(0).i.ival := AnalogToUInt(io.gpio_pmod_0)
    tlgpioSink.bundle.pins(1).i.ival := AnalogToUInt(io.gpio_pmod_1)
    tlgpioSink.bundle.pins(2).i.ival := AnalogToUInt(io.gpio_pmod_2)
    tlgpioSink.bundle.pins(3).i.ival := AnalogToUInt(io.gpio_pmod_3)
  } }
}
