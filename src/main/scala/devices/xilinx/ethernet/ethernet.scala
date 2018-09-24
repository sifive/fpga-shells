// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.ethernet

import chisel3._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.util._
import freechips.rocketchip.subsystem.CacheBlockBytes
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

  lazy val module = new LazyModuleImp(this) {
    port <> phy.module.io.pads

    val clocks = phy.module.io.clocks
    clocks.rx_core_clk_0             := clocks.tx_mii_clk_0
    clocks.dclk                      := clock
    clocks.s_axi_aclk_0              := clock
    clocks.s_axi_aresetn_0           := !reset.asUInt
    clocks.sys_reset                 := reset
    clocks.tx_reset_0                := reset
    clocks.rx_reset_0                := reset
    clocks.gtwiz_reset_tx_datapath_0 := reset
    clocks.gtwiz_reset_rx_datapath_0 := reset

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

    regmap(
      0  -> RegFieldGroup("control", Some("Control Registers"), Seq(
        RegField(1, txen, RegFieldDesc("tx_en", "TX Enable", reset=Some(0))),
        RegField(7),
        RegField(1, rxen, RegFieldDesc("rx_en", "RX Enable", reset=Some(0))),
        RegField(7),
        RegField.r(1, txQ.io.enq.ready, RegFieldDesc("tx_ready", "TX Ready")),
        RegField(7),
        RegField.r(1, rxQ.io.deq.valid, RegFieldDesc("rx_valid", "RX Valid")),
        RegField(7))),
      8  -> RegFieldGroup("tx", Some("TX Data Queue"), Seq(RegField.w(64, txQ.io.enq))),
      16 -> RegFieldGroup("rx", Some("RX Data Queue"), Seq(RegField.r(64, rxQ.io.deq))))

    txQ.io.deq.ready := mac.io.tx_axis_tready
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
