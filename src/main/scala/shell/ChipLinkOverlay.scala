// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.clocks._

case class ChipLinkOverlayParams(
  params:   ChipLinkParams,
  txGroup:  ClockGroupNode,
  txData:   ClockSinkNode,
  wrangler: ClockAdapterNode)(
  implicit val p: Parameters)

case object ChipLinkOverlayKey extends Field[Seq[DesignOverlay[ChipLinkOverlayParams, TLNode]]](Nil)

abstract class ChipLinkOverlay(
  val params: ChipLinkOverlayParams,
  val rxPhase: Double,
  val txPhase: Double)
    extends IOOverlay[WideDataLayerPort, TLNode]
{
  implicit val p = params.p
  val freqMHz  = params.txData.portParams.head.take.get.freqMHz
  val phaseDeg = params.txData.portParams.head.phaseDeg

  val sdcRxClockName = s"${name}_b2c_clk"
  val sdcTxClockName = s"${name}_c2b_clk"

  def fpgaReset = false
  val link = LazyModule(new ChipLink(params.params.copy(fpgaReset = fpgaReset)))
  val rxPLL   = p(PLLFactoryKey)(feedback = true)
  val ioSink  = shell { link.ioNode.makeSink() }
  val rxI     = shell { ClockSourceNode(sdcRxClockName, freqMHz = freqMHz, jitterPS = 100) }
  val rxGroup = shell { ClockGroup() }
  val rxO     = shell { ClockSinkNode(freqMHz = freqMHz, phaseDeg = rxPhase) }
  val txClock = shell { ClockSinkNode(freqMHz = freqMHz, phaseDeg = phaseDeg + txPhase) }

  rxO := params.wrangler := rxGroup := rxPLL := rxI
  txClock := params.wrangler := params.txGroup

  def designOutput = link.node
  def ioFactory = new WideDataLayerPort(ChipLinkParams(Nil,Nil))

  shell { InModuleBody {
    val (rxOut, _) = rxO.in(0)
    val port = ioSink.bundle
    io <> port
    port.b2c.clk := rxOut.clock
  } }
}
