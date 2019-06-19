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
      UIntToAnalog(tlgpioSink.bundle.pins(7).o.oval, io.gpio_7, tlgpioSink.bundle.pins(7).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(8).o.oval, io.gpio_8, tlgpioSink.bundle.pins(8).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(9).o.oval, io.gpio_9, tlgpioSink.bundle.pins(9).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(10).o.oval, io.gpio_10, tlgpioSink.bundle.pins(10).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(11).o.oval, io.gpio_11, tlgpioSink.bundle.pins(11).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(12).o.oval, io.gpio_12, tlgpioSink.bundle.pins(12).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(13).o.oval, io.gpio_13, tlgpioSink.bundle.pins(13).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(14).o.oval, io.gpio_14, tlgpioSink.bundle.pins(14).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(15).o.oval, io.gpio_15, tlgpioSink.bundle.pins(15).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(16).o.oval, io.gpio_16, tlgpioSink.bundle.pins(16).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(17).o.oval, io.gpio_17, tlgpioSink.bundle.pins(17).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(18).o.oval, io.gpio_18, tlgpioSink.bundle.pins(18).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(19).o.oval, io.gpio_19, tlgpioSink.bundle.pins(19).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(20).o.oval, io.gpio_20, tlgpioSink.bundle.pins(20).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(21).o.oval, io.gpio_21, tlgpioSink.bundle.pins(21).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(22).o.oval, io.gpio_22, tlgpioSink.bundle.pins(22).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(23).o.oval, io.gpio_23, tlgpioSink.bundle.pins(23).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(24).o.oval, io.gpio_24, tlgpioSink.bundle.pins(24).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(25).o.oval, io.gpio_25, tlgpioSink.bundle.pins(25).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(26).o.oval, io.gpio_26, tlgpioSink.bundle.pins(26).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(27).o.oval, io.gpio_27, tlgpioSink.bundle.pins(27).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(28).o.oval, io.gpio_28, tlgpioSink.bundle.pins(28).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(29).o.oval, io.gpio_29, tlgpioSink.bundle.pins(29).o.oe)
      UIntToAnalog(tlgpioSink.bundle.pins(30).o.oval, io.gpio_30, tlgpioSink.bundle.pins(30).o.oe)
      tlgpioSink.bundle.pins(0).i.ival := AnalogToUInt(io.gpio_0)
      tlgpioSink.bundle.pins(1).i.ival := AnalogToUInt(io.gpio_1)
      tlgpioSink.bundle.pins(2).i.ival := AnalogToUInt(io.gpio_2)
      tlgpioSink.bundle.pins(3).i.ival := AnalogToUInt(io.gpio_3)
      tlgpioSink.bundle.pins(4).i.ival := AnalogToUInt(io.gpio_4)
      tlgpioSink.bundle.pins(5).i.ival := AnalogToUInt(io.gpio_5)
      tlgpioSink.bundle.pins(6).i.ival := AnalogToUInt(io.gpio_6)
      tlgpioSink.bundle.pins(7).i.ival := AnalogToUInt(io.gpio_7)
      tlgpioSink.bundle.pins(8).i.ival := AnalogToUInt(io.gpio_8)
      tlgpioSink.bundle.pins(9).i.ival := AnalogToUInt(io.gpio_9)
      tlgpioSink.bundle.pins(10).i.ival := AnalogToUInt(io.gpio_10)
      tlgpioSink.bundle.pins(11).i.ival := AnalogToUInt(io.gpio_11)
      tlgpioSink.bundle.pins(12).i.ival := AnalogToUInt(io.gpio_12)
      tlgpioSink.bundle.pins(13).i.ival := AnalogToUInt(io.gpio_13)
      tlgpioSink.bundle.pins(14).i.ival := AnalogToUInt(io.gpio_14)
      tlgpioSink.bundle.pins(15).i.ival := AnalogToUInt(io.gpio_15)
      tlgpioSink.bundle.pins(16).i.ival := AnalogToUInt(io.gpio_16)
      tlgpioSink.bundle.pins(17).i.ival := AnalogToUInt(io.gpio_17)
      tlgpioSink.bundle.pins(18).i.ival := AnalogToUInt(io.gpio_18)
      tlgpioSink.bundle.pins(19).i.ival := AnalogToUInt(io.gpio_19)
      tlgpioSink.bundle.pins(20).i.ival := AnalogToUInt(io.gpio_20)
      tlgpioSink.bundle.pins(21).i.ival := AnalogToUInt(io.gpio_21)
      tlgpioSink.bundle.pins(22).i.ival := AnalogToUInt(io.gpio_22)
      tlgpioSink.bundle.pins(23).i.ival := AnalogToUInt(io.gpio_23)
      tlgpioSink.bundle.pins(24).i.ival := AnalogToUInt(io.gpio_24)
      tlgpioSink.bundle.pins(25).i.ival := AnalogToUInt(io.gpio_25)
      tlgpioSink.bundle.pins(26).i.ival := AnalogToUInt(io.gpio_26)
      tlgpioSink.bundle.pins(27).i.ival := AnalogToUInt(io.gpio_27)
      tlgpioSink.bundle.pins(28).i.ival := AnalogToUInt(io.gpio_28)
      tlgpioSink.bundle.pins(29).i.ival := AnalogToUInt(io.gpio_29)
      tlgpioSink.bundle.pins(30).i.ival := AnalogToUInt(io.gpio_30)
  } }
}
