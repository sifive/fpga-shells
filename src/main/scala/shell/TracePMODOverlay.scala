// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.gpio._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import chisel3.experimental._

case class TracePMODOverlayParams()(implicit val p: Parameters)
case object TracePMODOverlayKey extends Field[Seq[DesignOverlay[TracePMODOverlayParams, ModuleValue[UInt]]]](Nil)

abstract class TracePMODOverlay(
  val params: TracePMODOverlayParams)
    extends IOOverlay[UInt, ModuleValue[UInt]]
{
  implicit val p = params.p

  def ioFactory = Output(UInt(8.W))

  val pmodTraceSource = BundleBridgeSource(() => UInt(8.W))
  val pmodTraceSink = shell { pmodTraceSource.makeSink }
  val designOutput = InModuleBody { pmodTraceSource.out(0)._1 }
}
