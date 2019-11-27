// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.gpio._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import chisel3.experimental.Analog

case class TracePMODShellInput()
case class TracePMODDesignInput()(implicit val p: Parameters)
case class TracePMODOverlayOutput(trace: ModuleValue[UInt])
case object TracePMODOverlayKey extends Field[Seq[DesignPlacer[TracePMODDesignInput, TracePMODShellInput, TracePMODOverlayOutput]]](Nil)
trait TracePMODShellPlacer[Shell] extends ShellPlacer[TracePMODDesignInput, TracePMODShellInput, TracePMODOverlayOutput]

abstract class TracePMODPlacedOverlay(
  val name: String, val di: TracePMODDesignInput, val si: TracePMODShellInput)
    extends IOPlacedOverlay[UInt, TracePMODDesignInput, TracePMODShellInput, TracePMODOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = Output(UInt(8.W))

  val pmodTraceSource = BundleBridgeSource(() => UInt(8.W))
  val pmodTraceSink = shell { pmodTraceSource.makeSink }
  val traceout = InModuleBody { pmodTraceSource.out(0)._1 }
  def overlayOutput = TracePMODOverlayOutput(trace = traceout )
}
