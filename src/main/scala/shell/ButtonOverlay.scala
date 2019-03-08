// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

case class ButtonOverlayParams()(implicit val p: Parameters)
case object ButtonOverlayKey extends Field[Seq[DesignOverlay[ButtonOverlayParams, ModuleValue[UInt]]]](Nil)

abstract class ButtonOverlay(
  val params: ButtonOverlayParams)
    extends IOOverlay[UInt, ModuleValue[UInt]]
{
  implicit val p = params.p

  def width: Int
  def ioFactory = Input(UInt(width.W))

  val buttonSource = shell { BundleBridgeSource(() => UInt(width.W)) }
  val buttonSink = buttonSource.makeSink()
  val designOutput = InModuleBody { buttonSink.bundle }
}
