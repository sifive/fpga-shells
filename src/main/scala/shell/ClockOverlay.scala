// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.clocks._

case class ClockInputShellInput()
case class ClockOutputShellInput()
case class ClockInputDesignInput()(implicit val p: Parameters)
case class ClockOutputDesignInput()(implicit val p: Parameters)
case class ClockInputOverlayOutput(node: ClockSourceNode)
case class ClockOutputOverlayOutput(clock: ClockSinkNode)

trait ClockInputShellPlacer[Shell] extends ShellPlacer[ClockInputDesignInput, ClockInputShellInput, ClockInputOverlayOutput]
trait ClockOutputShellPlacer[Shell] extends ShellPlacer[ClockOutputDesignInput, ClockOutputShellInput, ClockOutputOverlayOutput]

case object ClockInputOverlayKey  extends Field[Seq[DesignPlacer[ClockInputDesignInput, ClockInputShellInput, ClockInputOverlayOutput]]](Nil)
case object ClockOutputOverlayKey extends Field[Seq[DesignPlacer[ClockOutputDesignInput, ClockOutputShellInput, ClockOutputOverlayOutput]]](Nil)

class LVDSClock extends Bundle
{
  val p = Clock()
  val n = Clock()
}

abstract class LVDSClockInputPlacedOverlay(
  val name: String, val di: ClockInputDesignInput, val si: ClockInputShellInput)
    extends IOPlacedOverlay[LVDSClock, ClockInputDesignInput, ClockInputShellInput, ClockInputOverlayOutput]
{
  implicit val p = di.p
  def node: ClockSourceNode

  def ioFactory = Input(new LVDSClock)

  val clock = shell { InModuleBody {
    val (bundle, edge) = node.out.head
    shell.sdc.addClock(name, io.p, edge.clock.freqMHz)
    bundle.clock
  } }
  def overlayOutput = ClockInputOverlayOutput(node)
}


abstract class SingleEndedClockInputPlacedOverlay(
  val name: String, val di: ClockInputDesignInput, val si: ClockInputShellInput)
    extends IOPlacedOverlay[Clock, ClockInputDesignInput, ClockInputShellInput, ClockInputOverlayOutput]
{
  implicit val p = di.p
  def node: ClockSourceNode

  def ioFactory = Input(Clock())

  val clock = shell { InModuleBody {
    val (bundle, edge) = node.out.head
    shell.sdc.addClock(name, io:Clock, edge.clock.freqMHz)
    bundle.clock
  } }
  def overlayOutput = ClockInputOverlayOutput(node)
}

abstract class SingleEndedClockBundleInputPlacedOverlay(
  val name: String, val di: ClockInputDesignInput, val si: ClockInputShellInput)
    extends IOPlacedOverlay[ClockBundle, ClockInputDesignInput, ClockInputShellInput, ClockInputOverlayOutput]
{
  implicit val p = di.p
  def node: ClockSourceNode

  def ioFactory = Input(new ClockBundle(ClockBundleParameters()))

  val clock = shell { InModuleBody {
    val (bundle, edge) = node.out.head
    bundle.clock
  } }
  val reset = shell { InModuleBody {
    val (bundle, edge) = node.out.head
    bundle.reset
  } }
  def overlayOutput = ClockInputOverlayOutput(node)
}
