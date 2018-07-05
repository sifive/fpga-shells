// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

case object DesignKey extends Field[Parameters => LazyModule]

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
