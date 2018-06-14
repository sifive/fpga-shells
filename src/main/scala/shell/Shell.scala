// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.core.DataMirror
import chisel3.experimental.IO
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

// Overlays are declared by the Shell and placed somewhere by the Design
// ... they inject diplomatic code both where they were placed and in the shell
// ... they are instantiated with DesignInput and return DesignOutput
trait Overlay[DesignOutput]
{
  def designOutput: DesignOutput
  def name: String
  def shell: Shell
}

// An IOOverlay is an Overlay with a public shell-level IO
trait IOOverlay[IO <: Data, DesignOutput] extends Overlay[DesignOutput]
{
  def ioFactory: IO

  val io = shell { InModuleBody {
    val port = IO(ioFactory)
    port.suggestName(name)
    port
  } }
}

// DesignOverlays provide the method used to instantiate and place an Overlay
trait DesignOverlay[DesignInput, DesignOutput] {
  def isPlaced: Boolean
  def name: String
  def apply(input: DesignInput): DesignOutput
}

abstract class Shell()(implicit p: Parameters) extends LazyModule with LazyScope
{
  private var overlays = Parameters.empty
  def designParameters: Parameters = overlays ++ p

  def Overlay[DesignInput, DesignOutput, T <: Overlay[DesignOutput]](
    key: Field[Seq[DesignOverlay[DesignInput, DesignOutput]]])(
    gen: (this.type, String, DesignInput) => T)(
    implicit valName: ValName): ModuleValue[Option[T]] =
  {
    val self = this.asInstanceOf[this.type]
    val thunk = new ModuleValue[Option[T]] with DesignOverlay[DesignInput, DesignOutput] {
      var placement: Option[T] = None
      def getWrappedValue = placement
      def isPlaced = !placement.isEmpty
      def name = valName.name
      def apply(input: DesignInput): DesignOutput = {
        require (placement.isEmpty, s"Overlay ${name} has already been placed by the design; cannot place again")
        val it = gen(self, valName.name, input)
        placement = Some(it)
        it.designOutput
      }
    }
    overlays = overlays ++ Parameters((site, here, up) => {
      case x: Field[_] if x eq key => thunk +: up(key)
    })
    thunk
  }

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
