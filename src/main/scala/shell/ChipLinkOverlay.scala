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

  val linkBridge = BundleBridge(new ChipLink(params.params))
  val rxPLL   = p(PLLFactoryKey)(feedback = true)
  val ioSink  = shell { linkBridge.ioNode.sink }
  val rxI     = shell { ClockSourceNode(freqMHz = freqMHz, jitterPS = 100) }
  val rxGroup = shell { ClockGroup() }
  val rxO     = shell { ClockSinkNode(freqMHz = freqMHz, phaseDeg = rxPhase) }
  val txTap   = shell { ClockIdentityNode() }
  val txClock = shell { ClockSinkNode(freqMHz = freqMHz, phaseDeg = phaseDeg + txPhase) }

  rxO := params.wrangler := rxGroup := rxPLL := rxI
  txClock := params.wrangler := txTap := params.txGroup

  def designOutput = linkBridge.child.node
  def ioFactory = new WideDataLayerPort(ChipLinkParams(Nil,Nil))

  shell { InModuleBody {
    val sink = ioSink.io
    val (tx, _) = txClock.in(0)
    val (rxIn, _) = rxI.out(0)
    val (rxOut, _) = rxO.in(0)
    io <> sink.port
    rxIn.clock := io.b2c.clk
    // reset definition is per-board
    sink.port.b2c.clk := rxOut.clock
    io.c2b.clk := tx.clock
  } }
}
