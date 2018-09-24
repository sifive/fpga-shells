// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.uart._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode

case class EthernetOverlayParams()(implicit val p: Parameters)
case object EthernetOverlayKey extends Field[Seq[DesignOverlay[EthernetOverlayParams, ModuleValue[EthernetPads]]]](Nil)

class EthernetPads extends Bundle {
  val tx_p = Output(Bool())
  val tx_n = Output(Bool())
  val rx_p = Input(Bool())
  val rx_n = Input(Bool())
  val refclk_p = Input(Clock())
  val refclk_n = Input(Clock())
}

abstract class EthernetOverlay(val params: EthernetOverlayParams)
  extends IOOverlay[EthernetPads, ModuleValue[EthernetPads]]
{
  implicit val p = params.p

  def ioFactory = new EthernetPads
  val padSource = BundleBridgeSource(() => new EthernetPads)
  val padSink = shell { padSource.makeSink }

  val designOutput = InModuleBody { padSource.bundle }

  shell { InModuleBody {
    io <> padSink.bundle
  } }
}
