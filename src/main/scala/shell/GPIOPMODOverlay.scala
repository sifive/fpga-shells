// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.gpio._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import chisel3.experimental._

case class GPIOPMODOverlayParams()(implicit val p: Parameters)
case object GPIOPMODOverlayKey extends Field[Seq[DesignOverlay[GPIOPMODOverlayParams, ModuleValue[GPIOPMODPortIO]]]](Nil)

class GPIOPMODPortIO extends Bundle {
  val gpio_pmod_0 = Analog(1.W)
  val gpio_pmod_1 = Analog(1.W)
  val gpio_pmod_2 = Analog(1.W)
  val gpio_pmod_3 = Analog(1.W)
  val gpio_pmod_4 = Analog(1.W)
  val gpio_pmod_5 = Analog(1.W)
  val gpio_pmod_6 = Analog(1.W)
  val gpio_pmod_7 = Analog(1.W)
}

abstract class GPIOPMODOverlay(
  val params: GPIOPMODOverlayParams)
    extends IOOverlay[GPIOPMODPortIO, ModuleValue[GPIOPMODPortIO]]
{
  implicit val p = params.p

  def ioFactory = new GPIOPMODPortIO

  val pmodgpioSource = BundleBridgeSource(() => new GPIOPMODPortIO)
  val pmodgpioSink = shell { pmodgpioSource.makeSink }

  val designOutput = InModuleBody { pmodgpioSource.bundle }

  shell { InModuleBody {
    io <> pmodgpioSink.bundle
  }}
/*
  shell { InModuleBody {
    UIntToAnalog(tlgpioSink.bundle.pins(0).o.oval, io.gpio_pmod_0, tlgpioSink.bundle.pins(0).o.oe)
    UIntToAnalog(tlgpioSink.bundle.pins(1).o.oval, io.gpio_pmod_1, tlgpioSink.bundle.pins(1).o.oe)
    UIntToAnalog(tlgpioSink.bundle.pins(2).o.oval, io.gpio_pmod_2, tlgpioSink.bundle.pins(2).o.oe)
    UIntToAnalog(tlgpioSink.bundle.pins(3).o.oval, io.gpio_pmod_3, tlgpioSink.bundle.pins(3).o.oe)
    UIntToAnalog(tlgpioSink.bundle.pins(4).o.oval, io.gpio_pmod_4, tlgpioSink.bundle.pins(4).o.oe)
    UIntToAnalog(tlgpioSink.bundle.pins(5).o.oval, io.gpio_pmod_5, tlgpioSink.bundle.pins(5).o.oe)
    UIntToAnalog(tlgpioSink.bundle.pins(6).o.oval, io.gpio_pmod_6, tlgpioSink.bundle.pins(6).o.oe)
    UIntToAnalog(tlgpioSink.bundle.pins(7).o.oval, io.gpio_pmod_7, tlgpioSink.bundle.pins(7).o.oe)

    tlgpioSink.bundle.pins(0).i.ival := AnalogToUInt(io.gpio_pmod_0)
    tlgpioSink.bundle.pins(1).i.ival := AnalogToUInt(io.gpio_pmod_1)
    tlgpioSink.bundle.pins(2).i.ival := AnalogToUInt(io.gpio_pmod_2)
    tlgpioSink.bundle.pins(3).i.ival := AnalogToUInt(io.gpio_pmod_3)
    tlgpioSink.bundle.pins(4).i.ival := AnalogToUInt(io.gpio_pmod_4)
    tlgpioSink.bundle.pins(5).i.ival := AnalogToUInt(io.gpio_pmod_5)
    tlgpioSink.bundle.pins(6).i.ival := AnalogToUInt(io.gpio_pmod_6)
    tlgpioSink.bundle.pins(7).i.ival := AnalogToUInt(io.gpio_pmod_7)
  } }
*/
}
