// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.gpio._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import chisel3.experimental._

case class GPIOOverlayParams(gpioParam: GPIOParams, controlBus: TLBusWrapper, intNode: IntInwardNode, width: Int)(implicit val p: Parameters)
case object GPIOOverlayKey extends Field[Seq[DesignOverlay[GPIOOverlayParams, TLGPIO]]](Nil)

class GPIOPortIO(width: Int = 4) extends Bundle {
  val gpio_0 = Analog(1.W)
  val gpio_1 = Analog(1.W)
  val gpio_2 = Analog(1.W)
  val gpio_3 = Analog(1.W)
  val gpio_4 = Analog(1.W)
  val gpio_5 = Analog(1.W)
  val gpio_6 = Analog(1.W)
  val gpio_7 = Analog(1.W)
  val gpio_8 = Analog(1.W)
  val gpio_9 = Analog(1.W)
  val gpio_10 = Analog(1.W)
  val gpio_11 = Analog(1.W)
  val gpio_12 = Analog(1.W)
  val gpio_13 = Analog(1.W)
  val gpio_14 = Analog(1.W)
  val gpio_15 = Analog(1.W)
  val gpio_16 = Analog(1.W)
  val gpio_17 = Analog(1.W)
  val gpio_18 = Analog(1.W)
  val gpio_19 = Analog(1.W)
  val gpio_20 = Analog(1.W)
  val gpio_21 = Analog(1.W)
  val gpio_22 = Analog(1.W)
  val gpio_23 = Analog(1.W)
  val gpio_24 = Analog(1.W)
  val gpio_25 = Analog(1.W)
  val gpio_26 = Analog(1.W)
  val gpio_27 = Analog(1.W)
  val gpio_28 = Analog(1.W)
  val gpio_29 = Analog(1.W)
  val gpio_30 = Analog(1.W)
}

abstract class GPIOOverlay(
  val params: GPIOOverlayParams)
    extends IOOverlay[GPIOPortIO, TLGPIO]
{
  implicit val p = params.p

  def ioFactory = new GPIOPortIO(params.width)
  val tlgpio = GPIO.attach(GPIOAttachParams(params.gpioParam, params.controlBus, params.intNode))

  val tlgpioSink = shell { tlgpio.ioNode.makeSink }
  val designOutput = tlgpio

  shell { InModuleBody {
      UIntToAnalog(tlgpioSink.bundle.pins(0).o.oval, io.gpio_0, tlgpioSink.bundle.pins(0).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(1).o.oval, io.gpio_1, tlgpioSink.bundle.pins(1).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(2).o.oval, io.gpio_2, tlgpioSink.bundle.pins(2).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(3).o.oval, io.gpio_3, tlgpioSink.bundle.pins(3).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(4).o.oval, io.gpio_4, tlgpioSink.bundle.pins(4).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(5).o.oval, io.gpio_5, tlgpioSink.bundle.pins(5).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(6).o.oval, io.gpio_6, tlgpioSink.bundle.pins(6).o.oe)
      tlgpioSink.bundle.pins(0).i.ival := AnalogToUInt(io.gpio_0)
      tlgpioSink.bundle.pins(1).i.ival := AnalogToUInt(io.gpio_1)
      tlgpioSink.bundle.pins(2).i.ival := AnalogToUInt(io.gpio_2)
      tlgpioSink.bundle.pins(3).i.ival := AnalogToUInt(io.gpio_3)
      tlgpioSink.bundle.pins(4).i.ival := AnalogToUInt(io.gpio_4)
      tlgpioSink.bundle.pins(5).i.ival := AnalogToUInt(io.gpio_5)
      tlgpioSink.bundle.pins(6).i.ival := AnalogToUInt(io.gpio_6)
  } }
}
