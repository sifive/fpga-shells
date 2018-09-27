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
import sifive.fpgashells.ip.xilinx.xxv_ethernet._

abstract class Ethernet(busWidthBytes: Int, c: XXVEthernetParams)(implicit p: Parameters) extends IORegisterRouter(
  RegisterRouterParams(
    name      = "ethernet",
    compat    = Seq("sifive,ethernet0"), 
    base      = c.control + 0x1000,
    beatBytes = busWidthBytes),
  new XXVEthernetPads)
{
  val phy = LazyModule(new DiplomaticXXVEthernet(c))
  val ctlnode =
    (phy.control // we drive s_axi_aclk_0 = clock; so syncrhonous
      := AXI4Buffer()
      := AXI4UserYanker(capMaxFlight = Some(2))
      := TLToAXI4()
      := TLFragmenter(4, p(CacheBlockBytes), holdFirstDeny = true))

  // connect this to a PLL source
  val dclk = ClockSinkNode(freqMHz = 75.0)

  lazy val module = new LazyModuleImp(this) {
    port <> phy.module.io.pads

    val clocks = phy.module.io.clocks
    clocks.rx_core_clk_0             := clocks.tx_mii_clk_0
    clocks.dclk                      := dclk.in(0)._1.clock
    clocks.s_axi_aclk_0              := clock
    clocks.s_axi_aresetn_0           := !reset.asUInt
    clocks.sys_reset                 := reset
    clocks.tx_reset_0                := false.B
    clocks.rx_reset_0                := false.B
    clocks.gtwiz_reset_tx_datapath_0 := false.B
    clocks.gtwiz_reset_rx_datapath_0 := false.B

    val mac = Module(new nfmac10g)
    val macIO = phy.module.io.mac
    macIO.tx_mii_d_0 := mac.io.xgmii_txd
    macIO.tx_mii_c_0 := mac.io.xgmii_txc
    mac.io.xgmii_rxd := macIO.rx_mii_d_0
    mac.io.xgmii_rxc := macIO.rx_mii_c_0

    mac.io.tx_clk0 := clocks.tx_mii_clk_0
    mac.io.rx_clk0 := clocks.rx_core_clk_0
    mac.io.reset := reset
    mac.io.tx_dcm_locked := !clocks.user_tx_reset_0
    mac.io.rx_dcm_locked := !clocks.user_rx_reset_0

    // FIFO interface
    val txen = RegInit(false.B)
    val rxen = RegInit(false.B)
    val rxQ  = Module(new AsyncQueue(UInt(64.W)))
    val txQ  = Module(new AsyncQueue(UInt(64.W)))

    // TX AXIS / RX AXIS
    mac.io.tx_axis_aresetn := !reset.asUInt
    mac.io.rx_axis_aresetn := !reset.asUInt
    rxQ.io.enq_clock := clocks.rx_core_clk_0
    rxQ.io.enq_reset := clocks.user_rx_reset_0
    rxQ.io.deq_clock := clock
    rxQ.io.deq_reset := reset
    txQ.io.enq_clock := clock
    txQ.io.enq_reset := reset
    txQ.io.deq_clock := clocks.tx_mii_clk_0
    txQ.io.deq_reset := clocks.user_tx_reset_0

    val gtC = withClockAndReset(clocks.gt_refclk_out, false.B) {
      val count = RegInit(0.U(32.W))
      count := count + 1.U
      count
    }
    val txC = withClockAndReset(clocks.tx_mii_clk_0, false.B) {
      val count = RegInit(0.U(32.W))
      count := count + 1.U
      count
    }
    val rxC = withClockAndReset(clocks.rxrecclkout_0, false.B) {
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
        RegField.r(1, clocks.user_tx_reset_0, RegFieldDesc("tx_reset", "TX Reset")),
        RegField.r(1, clocks.user_rx_reset_0, RegFieldDesc("rx_reset", "RX Reset")),
        RegField.r(1, mac.io.tx_axis_tready,  RegFieldDesc("mac_ready", "MAC Ready")),
        RegField.r(1, clocks.gtpowergood_out_0, RegFieldDesc("powergood", "PHY Power Good")))),
      4  -> RegFieldGroup("rxc", Some("RXC"), Seq(RegField.r(32, RegReadFn(RegNext(RegNext(rxC)))))),
      8  -> RegFieldGroup("gtc", Some("GTC"), Seq(RegField.r(32, RegReadFn(RegNext(RegNext(gtC)))))),
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

class TLEthernet(busWidthBytes: Int, c: XXVEthernetParams)(implicit p: Parameters)
  extends Ethernet(busWidthBytes, c) with HasTLControlRegMap
