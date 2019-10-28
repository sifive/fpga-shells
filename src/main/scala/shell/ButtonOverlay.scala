// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

case class ButtonShellInput(
  header: String = "",
  number: Int = 0)

case class ButtonDesignInput()(implicit val p: Parameters)
case class ButtonOverlayOutput(but: ModuleValue[Bool])
case object ButtonOverlayKey extends Field[Seq[DesignPlacer[ButtonDesignInput, ButtonShellInput, ButtonOverlayOutput]]](Nil)
trait ButtonShellPlacer[Shell] extends ShellPlacer[ButtonDesignInput, ButtonShellInput, ButtonOverlayOutput]

abstract class ButtonPlacedOverlay(
  val name: String, val di: ButtonDesignInput, si:ButtonShellInput)
    extends IOPlacedOverlay[Bool, ButtonDesignInput, ButtonShellInput, ButtonOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = Input(Bool())

  val buttonSource = shell { BundleBridgeSource(() => Bool()) }
  val buttonSink = buttonSource.makeSink()
  def overlayOutput = ButtonOverlayOutput(but = InModuleBody { buttonSink.bundle })
}
