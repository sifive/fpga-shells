// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

case class SwitchOverlayParams()(implicit val p: Parameters)
case object SwitchOverlayKey extends Field[Seq[DesignOverlay[SwitchOverlayParams, ModuleValue[UInt]]]](Nil)

abstract class SwitchOverlay(
  val params: SwitchOverlayParams)
    extends IOOverlay[UInt, ModuleValue[UInt]]
{
  implicit val p = params.p

  def width: Int
  def ioFactory = Input(UInt(width.W))

  val switchSource = shell { BundleBridgeSource(() => UInt(width.W)) }
  val switchSink = switchSource.makeSink()
  val designOutput = InModuleBody { switchSink.bundle }
}
