// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.ethernet

import chisel3._
import chisel3.experimental.withClockAndReset
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.util._
import freechips.rocketchip.subsystem.CacheBlockBytes
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx.xxv_ethernet._

class EthernetMACIO extends Bundle {
  val pcs = Flipped(new EthernetPCS)
}

abstract class EthernetMAC(busWidthBytes: Int, base: BigInt)(implicit p: Parameters) extends IORegisterRouter(
  RegisterRouterParams(
    name      = "ethernet",
    compat    = Seq("sifive,ethernet0"), 
    base      = base,
    beatBytes = busWidthBytes),
  new EthernetMACIO)
{
  lazy val module = new LazyModuleImp(this) {
    val mac = Module(new nfmac10g)
    port.pcs.tx_d := mac.io.xgmii_txd
    port.pcs.tx_c := mac.io.xgmii_txc
    mac.io.xgmii_rxd := port.pcs.rx_d
    mac.io.xgmii_rxc := port.pcs.rx_c

    mac.io.tx_clk0 := port.pcs.tx_clock
    mac.io.rx_clk0 := port.pcs.rx_clock
    mac.io.tx_dcm_locked := !port.pcs.tx_reset
    mac.io.rx_dcm_locked := !port.pcs.rx_reset
    mac.io.reset := reset

    // FIFO interface
    val txen = RegInit(false.B)
    val rxen = RegInit(false.B)
    val rxQ  = Module(new AsyncQueue(UInt(64.W)))
    val txQ  = Module(new AsyncQueue(UInt(64.W)))

    // TX AXIS / RX AXIS
    mac.io.tx_axis_aresetn := !reset.asUInt
    mac.io.rx_axis_aresetn := !reset.asUInt
    rxQ.io.enq_clock := port.pcs.rx_clock
    rxQ.io.enq_reset := port.pcs.rx_reset
    rxQ.io.deq_clock := clock
    rxQ.io.deq_reset := reset
    txQ.io.enq_clock := clock
    txQ.io.enq_reset := reset
    txQ.io.deq_clock := port.pcs.tx_clock
    txQ.io.deq_reset := port.pcs.tx_reset

    val txC = withClockAndReset(port.pcs.tx_clock, false.B) {
      val count = RegInit(0.U(32.W))
      count := count + 1.U
      count
    }
    val rxC = withClockAndReset(port.pcs.rx_clock, false.B) {
      val count = RegInit(0.U(32.W))
      count := count + 1.U
      count
    }

    regmap(
      0  -> RegFieldGroup("control", Some("Control Registers"), Seq(
        RegField(1, txen, RegFieldDesc("tx_en", "TX Enable", reset=Some(0))),
        RegField(1, rxen, RegFieldDesc("rx_en", "RX Enable", reset=Some(0))),
        RegField(14),
        RegField.r(1, txQ.io.enq.ready, RegFieldDesc("tx_ready", "TX Ready")),
        RegField.r(1, rxQ.io.deq.valid, RegFieldDesc("rx_valid", "RX Valid")),
        RegField.r(1, port.pcs.tx_reset, RegFieldDesc("tx_reset", "TX Reset")),
        RegField.r(1, port.pcs.rx_reset, RegFieldDesc("rx_reset", "RX Reset")),
        RegField.r(1, mac.io.tx_axis_tready,  RegFieldDesc("mac_ready", "MAC Ready")))),
      8  -> RegFieldGroup("rxc", Some("RXC"), Seq(RegField.r(32, RegReadFn(RegNext(RegNext(rxC)))))),
      12 -> RegFieldGroup("txc", Some("TXC"), Seq(RegField.r(32, RegReadFn(RegNext(RegNext(txC)))))),
      16 -> RegFieldGroup("tx", Some("TX Data Queue"), Seq(RegField.w(64, txQ.io.enq))),
      24 -> RegFieldGroup("rx", Some("RX Data Queue"), Seq(RegField.r(64, rxQ.io.deq))))

    txQ.io.deq.ready := mac.io.tx_axis_tready && txen
    mac.io.tx_axis_tvalid := txQ.io.deq.valid && txen
    mac.io.tx_axis_tdata  := txQ.io.deq.bits
    mac.io.tx_axis_tlast  := true.B // one word per frame
    mac.io.tx_axis_tkeep  := 0xff.U
    mac.io.tx_axis_tuser  := 0.U

    // rxQ.io ready is ignored; loss of packets can happen
    rxQ.io.enq.valid := mac.io.rx_axis_tvalid && rxen
    rxQ.io.enq.bits  := mac.io.rx_axis_tdata
    // ignore tkeep/tlast/tuser
  }
}

class TLEthernetMAC(busWidthBytes: Int, c: BigInt)(implicit p: Parameters)
  extends EthernetMAC(busWidthBytes, c) with HasTLControlRegMap
