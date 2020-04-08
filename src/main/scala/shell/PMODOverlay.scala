// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.gpio._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import chisel3.experimental.Analog

case class PMODShellInput(index: Int)
case class PMODDesignInput()(implicit val p: Parameters)
case class PMODOverlayOutput(pin: ModuleValue[PMODPortIO])
case object PMODOverlayKey extends Field[Seq[DesignPlacer[PMODDesignInput, PMODShellInput, PMODOverlayOutput]]](Nil)
trait PMODShellPlacer[Shell] extends ShellPlacer[PMODDesignInput, PMODShellInput, PMODOverlayOutput]

class PMODPortIO extends Bundle {
  val pins = Vec(8, Analog(1.W))
}

abstract class PMODPlacedOverlay(
  val name: String, val di: PMODDesignInput, val si: PMODShellInput)
    extends IOPlacedOverlay[PMODPortIO, PMODDesignInput, PMODShellInput, PMODOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new PMODPortIO

  val pinSource = BundleBridgeSource(() => new PMODPortIO)
  val pinSink = shell { pinSource.makeSink }

  def overlayOutput = PMODOverlayOutput(pin = InModuleBody { pinSource.bundle } )

  shell { InModuleBody {
    io <> pinSink.bundle
  }}
}
