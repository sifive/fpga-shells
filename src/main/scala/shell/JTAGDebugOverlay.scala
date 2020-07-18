// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog
import freechips.rocketchip.config._
import freechips.rocketchip.util._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.jtag._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import sifive.fpgashells.ip.xilinx._

case class JTAGDebugShellInput(location: Option[String] = None)
case class JTAGDebugDesignInput()(implicit val p: Parameters)
case class JTAGDebugOverlayOutput(jtag: ModuleValue[FlippedJTAGIO])
case object JTAGDebugOverlayKey extends Field[Seq[DesignPlacer[JTAGDebugDesignInput, JTAGDebugShellInput, JTAGDebugOverlayOutput]]](Nil)
trait JTAGDebugShellPlacer[Shell] extends ShellPlacer[JTAGDebugDesignInput, JTAGDebugShellInput, JTAGDebugOverlayOutput]

class ShellJTAGIO extends Bundle {
  // JTAG
  val jtag_TCK = Analog(1.W)
  val jtag_TMS = Analog(1.W)
  val jtag_TDI = Analog(1.W)
  val jtag_TDO = Analog(1.W)
  val srst_n   = Analog(1.W)
}

// TODO: Fix interaction of BundleBridge/Flipped to get rid of this Bundle
class FlippedJTAGIO extends Bundle {
  val TCK = Input(Clock())
  val TMS = Input(Bool())
  val TDI = Input(Bool())
  val TDO = Output(new Tristate())
  val srst_n = Input(Bool())
}

abstract class JTAGDebugPlacedOverlay(
  val name: String, val di: JTAGDebugDesignInput, val si: JTAGDebugShellInput)
    extends IOPlacedOverlay[ShellJTAGIO, JTAGDebugDesignInput, JTAGDebugShellInput, JTAGDebugOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new ShellJTAGIO

  val jtagDebugSource = BundleBridgeSource(() => new FlippedJTAGIO())
  val jtagDebugSink = sinkScope { jtagDebugSource.makeSink }
  val jtout = InModuleBody { jtagDebugSource.bundle}
  def overlayOutput = JTAGDebugOverlayOutput(jtag = jtout)
}
