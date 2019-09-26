// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.gpio._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import chisel3.experimental._

case class PinOverlayParams()(implicit val p: Parameters)
case object PinOverlayKey extends Field[Seq[DesignOverlay[PinOverlayParams, ModuleValue[PinPortIO]]]](Nil)

class PinPortIO extends Bundle {
  val pins = Vec(8, Analog(1.W))
}

abstract class PinOverlay(
  val params: PinOverlayParams)
    extends IOOverlay[PinPortIO, ModuleValue[PinPortIO]]
{
  implicit val p = params.p

  def ioFactory = new PinPortIO

  val pinSource = BundleBridgeSource(() => new PinPortIO)
  val pinSink = shell { pinSource.makeSink }

  val designOutput = InModuleBody { pinSource.bundle }

  shell { InModuleBody {
    io <> pinSink.bundle
  }}
}
