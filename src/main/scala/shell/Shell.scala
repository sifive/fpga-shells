// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

import chisel3.experimental.ChiselAnnotation
import firrtl._
import firrtl.analyses._
import firrtl.annotations._
//import firrtl.ir._
import freechips.rocketchip.util.DontTouch

case object DesignKey extends Field[Parameters => LazyModule]

case object DesignKeyWithTestHarness extends Field[(Option[LazyScope], Parameters) => LazyModule]

// Overlays are declared by the Shell and placed somewhere by the Design
// ... they inject diplomatic code both where they were placed and in the shell
// ... they are instantiated with DesignInput and return DesignOutput
// placed overlay has been invoked by the design
trait PlacedOverlay[DesignInput, ShellInput, OverlayOutput] {
  def name: String
  def designInput: DesignInput
  def shellInput: ShellInput
  def overlayOutput: OverlayOutput
}

trait ShellPlacer[DesignInput, ShellInput, OverlayOutput] {
  def valName: ValName
  def shellInput: ShellInput
  def place(di: DesignInput): PlacedOverlay[DesignInput, ShellInput, OverlayOutput]
}

trait DesignPlacer[DesignInput, ShellInput, OverlayOutput] {
  def isPlaced: Boolean
  def name: String
  def shellInput: ShellInput
  def place(di: DesignInput): PlacedOverlay[DesignInput, ShellInput, OverlayOutput]
}

trait ShellOverlayAccessor[DesignInput, ShellInput, OverlayOutput] {
  def get(): Option[PlacedOverlay[DesignInput, ShellInput, OverlayOutput]]
}

abstract class Shell()(implicit p: Parameters) extends LazyModule with LazyScope
{
  private var overlays = Parameters.empty
  def designParameters: Parameters = overlays ++ p

  def Overlay[DesignInput, ShellInput, OverlayOutput](
      key: Field[Seq[DesignPlacer[DesignInput, ShellInput, OverlayOutput]]],
      placer: ShellPlacer[DesignInput, ShellInput, OverlayOutput]): 
    ShellOverlayAccessor[DesignInput, ShellInput, OverlayOutput] = {
    val thunk = new Object
        with ShellOverlayAccessor[DesignInput, ShellInput, OverlayOutput]
        with DesignPlacer[DesignInput, ShellInput, OverlayOutput] {
      var placedOverlay: Option[PlacedOverlay[DesignInput, ShellInput, OverlayOutput]] = None
      def get() = placedOverlay
      def isPlaced = !placedOverlay.isEmpty
      def name = placer.valName.name
      def shellInput = placer.shellInput
      def place(input: DesignInput): PlacedOverlay[DesignInput, ShellInput, OverlayOutput] = {
        require (!isPlaced, s"Overlay ${name} has already been placed by the design; cannot place again")
        val it = placer.place(input)
        placedOverlay = Some(it)
        it
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
