// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

case class SwitchShellInput(number: Int = 0)
case class SwitchDesignInput()(implicit val p: Parameters)
case class SwitchOverlayOutput(sw: ModuleValue[Bool])
case object SwitchOverlayKey extends Field[Seq[DesignPlacer[SwitchDesignInput, SwitchShellInput, SwitchOverlayOutput]]](Nil)
trait SwitchShellPlacer[Shell] extends ShellPlacer[SwitchDesignInput, SwitchShellInput, SwitchOverlayOutput]

abstract class SwitchPlacedOverlay(
  val name: String, val di: SwitchDesignInput, val si: SwitchShellInput)
    extends IOPlacedOverlay[Bool, SwitchDesignInput, SwitchShellInput, SwitchOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = Input(Bool())

  val switchSource = shell { BundleBridgeSource(() => Bool()) }
  val switchSink = switchSource.makeSink()
  def overlayOutput = SwitchOverlayOutput(sw = InModuleBody { switchSink.bundle } )
}
