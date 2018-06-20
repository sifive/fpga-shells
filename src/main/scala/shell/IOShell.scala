// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.IO
import chisel3.core.DataMirror
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

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

// An IOOverlay is an Overlay with a public shell-level IO
trait IOOverlay[IO <: Data, DesignOutput] extends Overlay[DesignOutput]
{
  def ioFactory: IO
  def shell: IOShell

  val io = shell { InModuleBody {
    val port = IO(ioFactory)
    port.suggestName(name)
    port
  } }
}

abstract class IOShell()(implicit p: Parameters) extends Shell
{
  def portOf(x: IOPin) = s"[ get_ports { ${x.name} } ]"
  def clockOf(x: IOPin) =  s"[ get_clocks -of_objects ${portOf(x)} ]"

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
