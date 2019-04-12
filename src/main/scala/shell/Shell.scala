// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

import chisel3.experimental.{ChiselAnnotation, RawModule}
import firrtl._
import firrtl.analyses._
import firrtl.annotations._
//import firrtl.ir._
import freechips.rocketchip.util.DontTouch
case object DesignKey extends Field[Parameters => LazyModule]

trait MarkDUT extends DontTouch {
  self: RawModule =>
  /** Marks this Module as the DUT
    *
    * @note This also marks all ports of the Module as don't touch
    * @note This method can only be called after the Module has been fully constructed
    *   (after Module(...))
    */
  def markDUT(): this.type = {
    self.dontTouchPorts()
    chisel3.experimental.annotate(new ChiselAnnotation { def toFirrtl = MarkDUTAnnotation(self.toNamed) })
    self
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
      case x: Field[_] if x eq key => {
        val tail = up(key)
        if (thunk.isPlaced) { tail } else { thunk +: tail }
      }
    })
    thunk
  }

  // feel free to override this if necessary
  lazy val module = new LazyRawModuleImp(this)
}

case class MarkDUTAnnotation(target: ModuleName) extends SingleTargetAnnotation[ModuleName] {
  def duplicate(n: ModuleName): MarkDUTAnnotation = MarkDUTAnnotation(n)
}

/** Contains helper methods for finding things relative to the DUT while running other transforms */
object MarkDUTAnnotation {
  /** Find names of all [[DefModule]]s in the DUT
    * Throws exception if no MarkDUTAnnotation found
    * @return (Name of DUT Top, names of other DefModules in the DUT)
    */
  def getDUTModules(state: CircuitState): (String, Set[String]) =
    getDUTModules(state, new InstanceGraph(state.circuit))
  def getDUTModules(state: CircuitState, igraph: => InstanceGraph): (String, Set[String]) =
    getDUTModulesOpt(state, igraph).getOrElse(
      throw new Exception("Attemping to find all Modules in DUT but no MarkDUTAnnotation found!")
    )

  /** Find names of all [[DefModule]]s in the DUT if annotation found, None otherwise
    * @return Option(Name of DUT Top, names of other DefModules in the DUT)
    */
  def getDUTModulesOpt(state: CircuitState): Option[(String, Set[String])] =
    getDUTModulesOpt(state, new InstanceGraph(state.circuit))
  def getDUTModulesOpt(state: CircuitState, igraph: => InstanceGraph): Option[(String, Set[String])] =
    state.annotations.collect { case MarkDUTAnnotation(ModuleName(dutTop, _)) => dutTop } match {
      case Seq() => None
      case Seq(dutTop) =>
        val mgraph = igraph.graph.transformNodes(_.module)
        require(mgraph.contains(dutTop),
          s"MarkDUTAnnotation found but $dutTop does not exist in circuit!")
        Some((dutTop, mgraph.reachableFrom(dutTop).toSet))
      case duts =>
        throw new Exception(s"Error! More than one DUT specified: " + duts.mkString(", "))
  }
}
