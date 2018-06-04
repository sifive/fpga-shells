// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.core.DataMirror
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import freechips.rocketchip.tilelink._
import sifive.fpgashells.clocks._

case object DesignKey extends Field[Parameters => LazyModule]
case object ClockInputKey extends Field[Seq[ClockSourceNode]](Nil)
case object ClockOutputKey extends Field[Seq[ClockSinkNode]](Nil)

class LVDSClock extends Bundle
{
  val p = Clock()
  val n = Clock()
}

case class IOTiming(
  minInput:  Double = 0,
  maxInput:  Double = 0,
  minOutput: Double = 0,
  maxOutput: Double = 0)

case class IOPin(element: Element, index: Int = 0)
{
  private val width = DataMirror.widthOf(element)
  require (width.known)
  require (index >= 0 && index < width.get)

  def name = {
    val stem = element.instanceName.map(c => if (c=='.') '_' else c)
    val elt = if (width.get > 1) s"[${index}]" else ""
    stem + elt
  }

  def isOutput = {
    import chisel3.core.ActualDirection._
    DataMirror.directionOf(element) match {
      case Output => true
      case Input => false
      case Bidirectional(_) => true
      case Unspecified => { require(false); false }
    }
  }

  def isInput = {
    import chisel3.core.ActualDirection._
    DataMirror.directionOf(element) match {
      case Output => false
      case Input => true
      case Bidirectional(_) => true
      case Unspecified => { require(false); false }
    }
  }
}

object IOPin
{
  def of(x: Data): Seq[IOPin] = {
    val elts = x match {
      case a: Aggregate => getDataElements(a)
      case e: Element => Seq(e)
    }
    elts.flatMap { elt =>
      val width = DataMirror.widthOf(elt)
      require (width.known)
      Seq.tabulate(width.get) { i => IOPin(elt, i) }
    }
  }
}

abstract class OverlayGenerator[Nodes, IO <: Data]
{
  def nodes: Nodes
  def io: IO
  def constrainIO(io: IO): Unit
}

trait DesignOverlay[P, Nodes] {
  def name: String
  def apply(params: P): Nodes
}

class ShellOverlay[P, Nodes, IO <: Data](gen: P => OverlayGenerator[Nodes,IO])(implicit valName: ValName)
  extends DesignOverlay[P,Nodes]
{
  private var haveGen: Option[OverlayGenerator[Nodes,IO]] = None
  def io = haveGen.map(_.io)
  def constrainIO(x: Option[IO]) {
    (x zip haveGen) foreach { case (io, gen) =>
      gen.constrainIO(io)
    }
  }

  /* Implement design-facing Overlay: */
  def name = valName.name
  def apply(params: P) = {
    require (haveGen.isEmpty, s"Overlay ${name} has already been applied to the shell; cannot apply again")
    val it = gen(params)
    haveGen = Some(it)
    it.nodes
  }
}

abstract class Shell()(implicit p: Parameters) extends LazyModule with LazyScope
{
  def Overlay[P, Nodes, IO <: Data](gen: P => OverlayGenerator[Nodes,IO])(implicit valName: ValName) =
    new ShellOverlay[P,Nodes,IO](gen)

  def portOf(x: IOPin) = s"[ get_ports { ${x.name} } ]"
  def clockOf(x: IOPin) =  s"[ get_clocks -of_objects ${portOf(x)} ]"

  private var constraints: Seq[() => String] = Nil
  ElaborationArtefacts.add("constraints.tcl", constraints.map(_()).reverse.mkString("\n") + "\n")

  def addConstraint(command: => String) {
    constraints = (() => command) +: constraints
  }

  def setIOTiming(io: IOPin, clock: => String, timing: IOTiming) {
    if (io.isInput) {
      addConstraint(f"set_input_delay  -min ${timing.minInput}% -5.2f -clock ${clock} ${portOf(io)}")
      addConstraint(f"set_input_delay  -max ${timing.maxInput}% -5.2f -clock ${clock} ${portOf(io)}")
    }
    if (io.isOutput) {
      addConstraint(f"set_output_delay -min ${timing.minOutput}% -5.2f -clock ${clock} ${portOf(io)}")
      addConstraint(f"set_output_delay -max ${timing.maxOutput}% -5.2f -clock ${clock} ${portOf(io)}")
    }
  }
}
