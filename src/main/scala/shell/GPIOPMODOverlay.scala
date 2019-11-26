// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.gpio._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import chisel3.experimental.Analog

case class GPIOPMODShellInput()
case class GPIOPMODDesignInput()(implicit val p: Parameters)
case class GPIOPMODOverlayOutput(pmod: ModuleValue[GPIOPMODPortIO])
case object GPIOPMODOverlayKey extends Field[Seq[DesignPlacer[GPIOPMODDesignInput, GPIOPMODShellInput, GPIOPMODOverlayOutput]]](Nil)
trait GPIOPMODShellPlacer[Shell] extends ShellPlacer[GPIOPMODDesignInput, GPIOPMODShellInput, GPIOPMODOverlayOutput]

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

abstract class GPIOPMODPlacedOverlay(
  val name: String, val di: GPIOPMODDesignInput, val si: GPIOPMODShellInput)
    extends IOPlacedOverlay[GPIOPMODPortIO, GPIOPMODDesignInput, GPIOPMODShellInput, GPIOPMODOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new GPIOPMODPortIO

  val pmodgpioSource = BundleBridgeSource(() => new GPIOPMODPortIO)
  val pmodgpioSink = shell { pmodgpioSource.makeSink }

  def overlayOutput = GPIOPMODOverlayOutput(pmod = InModuleBody { pmodgpioSource.bundle } )

  shell { InModuleBody {
    io <> pmodgpioSink.bundle
  }}
}
