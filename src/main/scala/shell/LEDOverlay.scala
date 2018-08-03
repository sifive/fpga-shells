// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

case class LEDOverlayParams()(implicit val p: Parameters)
case object LEDOverlayKey extends Field[Seq[DesignOverlay[LEDOverlayParams, ModuleValue[UInt]]]](Nil)

abstract class LEDOverlay(
  val params: LEDOverlayParams)
    extends IOOverlay[UInt, ModuleValue[UInt]]
{
  implicit val p = params.p

  def width: Int
  def ioFactory = Output(UInt(width.W))

  val ledSource = BundleBridgeSource(() => UInt(width.W))
  val ledSink = shell { ledSource.makeSink() }
  val designOutput = InModuleBody { ledSource.out(0)._1 }
}
