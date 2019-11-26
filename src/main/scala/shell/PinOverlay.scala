// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.gpio._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import chisel3.experimental.Analog

case class PinShellInput()
case class PinDesignInput()(implicit val p: Parameters)
case class PinOverlayOutput(pin: ModuleValue[PinPortIO])
case object PinOverlayKey extends Field[Seq[DesignPlacer[PinDesignInput, PinShellInput, PinOverlayOutput]]](Nil)
trait PinShellPlacer[Shell] extends ShellPlacer[PinDesignInput, PinShellInput, PinOverlayOutput]

class PinPortIO extends Bundle {
  val pins = Vec(8, Analog(1.W))
}

abstract class PinPlacedOverlay(
  val name: String, val di: PinDesignInput, val si: PinShellInput)
    extends IOPlacedOverlay[PinPortIO, PinDesignInput, PinShellInput, PinOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new PinPortIO

  val pinSource = BundleBridgeSource(() => new PinPortIO)
  val pinSink = shell { pinSource.makeSink }

  def overlayOutput = PinOverlayOutput(pin = InModuleBody { pinSource.bundle } )

  shell { InModuleBody {
    io <> pinSink.bundle
  }}
}
