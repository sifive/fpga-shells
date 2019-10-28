// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

case class LEDShellInput(
  color: String = "",
  header: String = "",
  rgb: Boolean = false,
  number: Int = 0)

case class LEDDesignInput()(implicit val p: Parameters)
case class LEDOverlayOutput(led: ModuleValue[Bool])
case object LEDOverlayKey extends Field[Seq[DesignPlacer[LEDDesignInput, LEDShellInput, LEDOverlayOutput]]](Nil)
trait LEDShellPlacer[Shell] extends ShellPlacer[LEDDesignInput, LEDShellInput, LEDOverlayOutput]

abstract class LEDPlacedOverlay(
  val name: String, val di: LEDDesignInput, si: LEDShellInput)
    extends IOPlacedOverlay[Bool, LEDDesignInput, LEDShellInput, LEDOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = Output(Bool())

  val ledSource = BundleBridgeSource(() => Bool())
  val ledSink = shell { ledSource.makeSink() }
  def overlayOutput = LEDOverlayOutput(InModuleBody { ledSource.out(0)._1 })
}
