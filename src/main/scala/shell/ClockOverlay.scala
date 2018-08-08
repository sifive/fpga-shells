// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.clocks._

case class ClockInputOverlayParams()(implicit val p: Parameters)
case class ClockOutputOverlayParams()(implicit val p: Parameters)

case object ClockInputOverlayKey  extends Field[Seq[DesignOverlay[ClockInputOverlayParams, ClockSourceNode]]](Nil)
case object ClockOutputOverlayKey extends Field[Seq[DesignOverlay[ClockOutputOverlayParams, ClockSinkNode]]](Nil)

class LVDSClock extends Bundle
{
  val p = Clock()
  val n = Clock()
}

abstract class LVDSClockInputOverlay(
  val params: ClockInputOverlayParams)
    extends IOOverlay[LVDSClock, ClockSourceNode]
{
  implicit val p = params.p
  def node: ClockSourceNode

  def ioFactory = Input(new LVDSClock)
  def designOutput = node

  val clock = shell { InModuleBody {
    val (bundle, edge) = node.out.head
    shell.sdc.addClock(name, io.p, edge.clock.freqMHz)
    bundle.clock
  } }
}
