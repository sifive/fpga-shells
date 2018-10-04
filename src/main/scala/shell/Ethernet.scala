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
case object EthernetOverlayKey extends Field[Seq[DesignOverlay[EthernetOverlayParams, ModuleValue[EthernetPCS]]]](Nil)

class EthernetPads extends Bundle {
  val tx_p = Output(Bool())
  val tx_n = Output(Bool())
  val rx_p = Input(Bool())
  val rx_n = Input(Bool())
  val refclk_p = Input(Clock())
  val refclk_n = Input(Clock())
}

class EthernetPCS extends Bundle {
  val rx_clock = Output(Clock())
  val rx_reset = Output(Bool())
  val rx_d = Output(UInt(64.W))
  val rx_c = Output(UInt(8.W))
  val tx_clock = Output(Clock())
  val tx_reset = Output(Bool())
  val tx_d = Input(UInt(64.W))
  val tx_c = Input(UInt(8.W))
  val loopback = Input(UInt(3.W))
  val rx_lock = Output(Bool())
  val sfp_detect = Output(Bool())
}

abstract class EthernetOverlay(val params: EthernetOverlayParams)
  extends IOOverlay[EthernetPads, ModuleValue[EthernetPCS]]
{
  implicit val p = params.p

  def ioFactory = new EthernetPads
  val designOutput = InModuleBody { Wire(new EthernetPCS) }
}
