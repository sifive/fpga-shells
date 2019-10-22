// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.clocks._

case class ChipLinkShellInput(
  fmc: String = "")

case class ChipLinkDesignInput(
  di:   ChipLinkParams,
  txGroup:  ClockGroupNode,
  txData:   ClockSinkNode,
  wrangler: ClockAdapterNode)(
  implicit val p: Parameters)

case class ChipLinkOverlayOutput(node: TLNode)
case object ChipLinkOverlayKey extends Field[Seq[DesignPlacer[ChipLinkDesignInput, ChipLinkShellInput, ChipLinkOverlayOutput]]](Nil)
trait ChipLinkShellPlacer[Shell] extends ShellPlacer[ChipLinkDesignInput, ChipLinkShellInput, ChipLinkOverlayOutput]

abstract class ChipLinkPlacedOverlay(
  val name: String,
  val di: ChipLinkDesignInput,
  val si: ChipLinkShellInput,
  val rxPhase: Double,
  val txPhase: Double)
    extends IOPlacedOverlay[WideDataLayerPort, ChipLinkDesignInput, ChipLinkShellInput, ChipLinkOverlayOutput]
{
  implicit val p = di.p
  val freqMHz  = di.txData.portParams.head.take.get.freqMHz
  val phaseDeg = di.txData.portParams.head.phaseDeg

  def fpgaReset = false
  val link = LazyModule(new ChipLink(di.di.copy(fpgaReset = fpgaReset)))
  val rxPLL   = p(PLLFactoryKey)(feedback = true)
  val ioSink  = shell { link.ioNode.makeSink() }
  val rxI     = shell { ClockSourceNode(freqMHz = freqMHz, jitterPS = 100) }
  val rxGroup = shell { ClockGroup() }
  val rxO     = shell { ClockSinkNode(freqMHz = freqMHz, phaseDeg = rxPhase) }
  val txClock = shell { ClockSinkNode(freqMHz = freqMHz, phaseDeg = phaseDeg + txPhase) }

  rxO := di.wrangler := rxGroup := rxPLL := rxI
  txClock := di.wrangler := di.txGroup

  def overlayOutput = ChipLinkOverlayOutput(node = link.node)
  def ioFactory = new WideDataLayerPort(ChipLinkParams(Nil,Nil))

  shell { InModuleBody {
    val (rxOut, _) = rxO.in(0)
    val port = ioSink.bundle
    io <> port
    port.b2c.clk := rxOut.clock
  } }
}
