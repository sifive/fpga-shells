// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.ethernet

import chisel3._
import chisel3.util._
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

class EtherBeat extends Bundle {
  val data = UInt(64.W)
  val last = Bool()
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
    mac.io.reset := reset // combined with locked to reset MAC RX/TX FSMs
    mac.io.tx_dcm_locked := !port.pcs.tx_reset.asBool
    mac.io.rx_dcm_locked := !port.pcs.rx_reset.asBool
    mac.io.tx_axis_aresetn := !ResetCatchAndSync(port.pcs.tx_clock, reset.asBool)
    mac.io.rx_axis_aresetn := !ResetCatchAndSync(port.pcs.rx_clock, reset.asBool)

    // FIFO interface
    val txen = RegInit(false.B)
    val rxen = RegInit(false.B)
    val loop = RegInit(0.U(3.W))
    val skip = RegInit(false.B)
    val last = RegInit(true.B)
    val rxQ  = Module(new AsyncQueue(new EtherBeat, params=AsyncQueueParams(depth=16)))
    val txQ  = Module(new AsyncQueue(new EtherBeat, params=AsyncQueueParams(depth=16)))
    val txO = withClockAndReset(port.pcs.tx_clock, port.pcs.tx_reset) { Queue(txQ.io.deq, 2) }
    val rxI = withClockAndReset(port.pcs.rx_clock, port.pcs.rx_reset) {
      val w = Wire(chiselTypeOf(rxQ.io.enq))
      rxQ.io.enq <> Queue(w, 2)
      w
    }

    // TX AXIS / RX AXIS
    rxQ.io.enq_clock := port.pcs.rx_clock
    rxQ.io.enq_reset := port.pcs.rx_reset
    rxQ.io.deq_clock := clock
    rxQ.io.deq_reset := reset
    txQ.io.enq_clock := clock
    txQ.io.enq_reset := reset
    txQ.io.deq_clock := port.pcs.tx_clock
    txQ.io.deq_reset := port.pcs.tx_reset

    port.pcs.loopback := loop

    regmap(
      0  -> RegFieldGroup("control", Some("Control Registers"), Seq(
        RegField(1, txen, RegFieldDesc("tx_en", "TX Enable", reset=Some(0))),
        RegField(1, rxen, RegFieldDesc("rx_en", "RX Enable", reset=Some(0))),
        RegField(1),
        RegField(1, last, RegFieldDesc("last", "Last frame", reset=Some(1))),
        RegField(3, loop, RegFieldDesc("loop", "TX-RX PCS Loopback", reset=Some(0))),
        RegField(1, skip, RegFieldDesc("skip", "Bypass RX-TX directly", reset=Some(0))),
        RegField(8),
        RegField.r(1, txQ.io.enq.ready, RegFieldDesc("tx_ready", "TX Ready")),
        RegField.r(1, rxQ.io.deq.valid, RegFieldDesc("rx_valid", "RX Valid")),
        RegField.r(1, port.pcs.tx_reset.asBool, RegFieldDesc("tx_reset", "TX Reset")),
        RegField.r(1, port.pcs.rx_reset.asBool, RegFieldDesc("rx_reset", "RX Reset")),
        RegField.r(1, mac.io.tx_axis_tready,  RegFieldDesc("mac_ready", "MAC Ready")),
//        RegField.r(1, port.pcs.rx_lock, RegFieldDesc("rx_lock", "RX Lock")),
        RegField.r(1, port.pcs.sfp_detect, RegFieldDesc("sfp_detect", "SFP Detect")))),
      16 -> RegFieldGroup("tx", Some("TX Data Queue"), Seq(RegField.w(64, RegWriteFn((valid, data) => {
        txQ.io.enq.valid := valid;
        txQ.io.enq.bits.data := data
        txQ.io.enq.bits.last := last
        txQ.io.enq.ready })))),
      24 -> RegFieldGroup("rx", Some("RX Data Queue"), Seq(RegField.r(64, RegReadFn(ready => {
        rxQ.io.deq.ready := ready
        (rxQ.io.deq.valid, rxQ.io.deq.bits.data)}))))) // discards last

    val tx_txen = withClockAndReset(port.pcs.tx_clock, port.pcs.tx_reset) { RegNext(RegNext(txen)) }
    txO.ready := mac.io.tx_axis_tready && tx_txen
    mac.io.tx_axis_tvalid := txO.valid && tx_txen
    mac.io.tx_axis_tdata  := txO.bits.data
    mac.io.tx_axis_tlast  := txO.bits.last
    mac.io.tx_axis_tkeep  := 0xff.U
    mac.io.tx_axis_tuser  := 0.U

    // rxQ.io ready is ignored; loss of packets can happen
    val rx_rxen = withClockAndReset(port.pcs.rx_clock, port.pcs.rx_reset) { RegNext(RegNext(rxen)) }
    rxI.valid := mac.io.rx_axis_tvalid && rx_rxen
    rxI.bits.data := mac.io.rx_axis_tdata
    rxI.bits.last := mac.io.rx_axis_tlast
    // ignore tkeep/tuser

    when (withClockAndReset(port.pcs.tx_clock, port.pcs.tx_reset) { RegNext(RegNext(skip)) } ) {
      txO.ready := rxI.ready
      rxI.valid := txO.valid
      rxI.bits := txO.bits
    }
  }
}

class TLEthernetMAC(busWidthBytes: Int, c: BigInt)(implicit p: Parameters)
  extends EthernetMAC(busWidthBytes, c) with HasTLControlRegMap
