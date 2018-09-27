// See LICENSE for license details.
package sifive.fpgashells.ip.xilinx.xxv_ethernet

import chisel3._
import chisel3.util._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.util.{ElaborationArtefacts}

trait HasXXVEthernetPads {
  val gt_txp_out_0 = Output(Bool())
  val gt_txn_out_0 = Output(Bool())
  val gt_rxp_in_0 = Input(Bool())
  val gt_rxn_in_0 = Input(Bool())
  val gt_refclk_p = Input(Clock())
  val gt_refclk_n = Input(Clock())
}

trait HasXXVEthernetClocks {
  val gt_refclk_out = Output(Clock()) // 156.25MHz from xcvr refclk pads
  val rx_core_clk_0 = Input (Clock()) // >= 156.25MHz ... maybe core clock to avoid another crossing?
  val tx_mii_clk_0  = Output(Clock()) // TX data path
  val rx_clk_out_0  = Output(Clock()) // RX control+status signals
  val rxrecclkout_0 = Output(Clock())

  val s_axi_aclk_0     = Input(Clock()) // ok: 10-300 MHz
  val s_axi_aresetn_0  = Input(Bool())

  val sys_reset = Input(Bool())
  val dclk      = Input(Clock()) // 100MHz free-running debug clock

  val rx_reset_0      = Input (Bool()) // omg. so glitchy.
  val tx_reset_0      = Input (Bool())
  val user_rx_reset_0 = Output(Bool())
  val user_tx_reset_0 = Output(Bool())

  val gtwiz_reset_tx_datapath_0 = Input(Bool()) // dclk
  val gtwiz_reset_rx_datapath_0 = Input(Bool())

  val gtpowergood_out_0 = Output(Bool())
}

trait HasXXVEthernetControl {
  // AW
  val s_axi_awvalid_0 = Input (Bool())
  val s_axi_awready_0 = Output(Bool())
  val s_axi_awaddr_0  = Input (UInt(32.W))

  // AR
  val s_axi_arvalid_0 = Input (Bool())
  val s_axi_arready_0 = Output(Bool())
  val s_axi_araddr_0  = Input (UInt(32.W))

  // W
  val s_axi_wvalid_0 = Input (Bool())
  val s_axi_wready_0 = Output(Bool())
  val s_axi_wdata_0  = Input (UInt(32.W))
  val s_axi_wstrb_0  = Input (UInt(4.W))

  // B
  val s_axi_bvalid_0 = Output(Bool())
  val s_axi_bready_0 = Input (Bool())
  val s_axi_bresp_0  = Output(UInt(2.W))

  // R
  val s_axi_rvalid_0 = Output(Bool())
  val s_axi_rready_0 = Input (Bool())
  val s_axi_rdata_0  = Output(UInt(32.W))
  val s_axi_rresp_0  = Output(UInt(2.W))
}

trait HasXXVEthernetMAC {
  val rx_mii_d_0 = Output(UInt(64.W)) // rx_mii_clk??
  val tx_mii_d_0 = Input (UInt(64.W))
  val rx_mii_c_0 = Output(UInt(8.W)) // tx_mii_clk
  val tx_mii_c_0 = Input (UInt(8.W))
}

trait HasXXVEthernetJunk {
  // Drive these always to 3'b101 as per documentation
  val txoutclksel_in_0 = Input(UInt(3.W))
  val rxoutclksel_in_0 = Input(UInt(3.W))

  // Drive to 1 to latch statistics (ie: always drive to 0)
  val pm_tick_0 = Input(Bool())

  val stat_rx_block_lock_0        = Output(Bool())
  val stat_rx_framing_err_valid_0 = Output(Bool())
  val stat_rx_framing_err_0       = Output(Bool())
  val stat_rx_hi_ber_0            = Output(Bool())
  val stat_rx_valid_ctrl_code_0   = Output(Bool())
  val stat_rx_bad_code_0          = Output(Bool())
  val stat_rx_bad_code_valid_0    = Output(Bool())
  val stat_rx_error_valid_0       = Output(Bool())
  val stat_rx_error_0             = Output(UInt(8.W))
  val stat_rx_fifo_error_0        = Output(Bool())
  val stat_rx_local_fault_0       = Output(Bool())
  val stat_rx_status_0            = Output(Bool())
  val stat_tx_local_fault_0       = Output(Bool())
  val user_reg0_0                 = Output(UInt(32.W))
}

class XXVEthernetBlackBoxIO extends Bundle
  with HasXXVEthernetPads
  with HasXXVEthernetClocks
  with HasXXVEthernetControl
  with HasXXVEthernetMAC
  with HasXXVEthernetJunk

class XXVEthernetPads() extends Bundle with HasXXVEthernetPads
class XXVEthernetMAC() extends Bundle with HasXXVEthernetMAC
class XXVEthernetClocks() extends Bundle with HasXXVEthernetClocks

case class XXVEthernetParams(
  name:     String,
  control:  BigInt,
  speed:    Int)
{
  require (speed == 10 || speed == 25)
  val MHz = if (speed == 10) 156.25 else 161.1328125
}

class XXVEthernetBlackBox(c: XXVEthernetParams) extends BlackBox
{
  override def desiredName = c.name

  val io = IO(new XXVEthernetBlackBoxIO)

  ElaborationArtefacts.add(s"${desiredName}.vivado.tcl",
    s"""create_ip -vendor xilinx.com -library ip -version 2.4 -name xxv_ethernet -module_name ${desiredName} -dir $$ipdir -force
       |set_property -dict [list 							\\
       |  CONFIG.NUM_OF_CORES			{1}					\\
       |  CONFIG.CORE				{Ethernet PCS/PMA 64-bit}		\\
       |  CONFIG.BASE_R_KR			{BASE-R}				\\
       |  CONFIG.LINE_RATE			{${c.speed}}				\\
       |  CONFIG.ENABLE_PIPELINE_REG		{1}					\\
       |  CONFIG.GT_REF_CLK_FREQ		{${c.MHz}}				\\
       |  CONFIG.GT_DRP_CLK			{75}					\\
       |  CONFIG.INCLUDE_AXI4_INTERFACE		{1}					\\
       |] [get_ips ${desiredName}]
       |""".stripMargin)
//       |  CONFIG.GT_GROUP_SELECT		{Quad_X1Y12}				\\
//       |  CONFIG.LANE1_GT_LOC			{X1Y48}					\\
// INCLUDE_USER_FIFO 0
// TX_LATENCY_ADJUST 0
// CLOCKIN Asynchronous
}

class DiplomaticXXVEthernet(c: XXVEthernetParams)(implicit p:Parameters) extends LazyModule
{
  val device = new SimpleDevice("ethernet-pcs", Seq("xlnx,xxv-ethernet-pcs"))

  val control = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = List(AddressSet(c.control, 0xfffL)),
      resources     = device.reg("control"),
      supportsWrite = TransferSizes(1, 4),
      supportsRead  = TransferSizes(1, 4),
      interleavedId = Some(0))), // AXI4-Lite never interleaves responses
    beatBytes = 4)))

  lazy val module = new LazyRawModuleImp(this) {
    // The master on the control port must be AXI-lite
    require (control.edges.in(0).master.endId == 1)

    val io = IO(new Bundle {
      val pads   = new XXVEthernetPads
      val mac    = new XXVEthernetMAC
      val clocks = new XXVEthernetClocks
    })

    val blackbox = Module(new XXVEthernetBlackBox(c))
    val (sl, _) = control.in(0)

    // pads
    io.pads.gt_txp_out_0 := blackbox.io.gt_txp_out_0
    io.pads.gt_txn_out_0 := blackbox.io.gt_txn_out_0
    blackbox.io.gt_rxp_in_0 := io.pads.gt_rxp_in_0
    blackbox.io.gt_rxn_in_0 := io.pads.gt_rxn_in_0
    blackbox.io.gt_refclk_p := io.pads.gt_refclk_p
    blackbox.io.gt_refclk_n := io.pads.gt_refclk_n

    // clocks
    io.clocks.gt_refclk_out     := blackbox.io.gt_refclk_out
    io.clocks.tx_mii_clk_0      := blackbox.io.tx_mii_clk_0
    io.clocks.rx_clk_out_0      := blackbox.io.rx_clk_out_0
    io.clocks.rxrecclkout_0     := blackbox.io.rxrecclkout_0
    io.clocks.user_rx_reset_0   := blackbox.io.user_rx_reset_0
    io.clocks.user_tx_reset_0   := blackbox.io.user_tx_reset_0
    io.clocks.gtpowergood_out_0 := blackbox.io.gtpowergood_out_0
    blackbox.io.rx_core_clk_0             := io.clocks.rx_core_clk_0
    blackbox.io.s_axi_aclk_0              := io.clocks.s_axi_aclk_0
    blackbox.io.s_axi_aresetn_0           := io.clocks.s_axi_aresetn_0
    blackbox.io.sys_reset                 := io.clocks.sys_reset
    blackbox.io.dclk                      := io.clocks.dclk
    blackbox.io.rx_reset_0                := io.clocks.rx_reset_0
    blackbox.io.tx_reset_0                := io.clocks.tx_reset_0
    blackbox.io.gtwiz_reset_tx_datapath_0 := io.clocks.gtwiz_reset_tx_datapath_0
    blackbox.io.gtwiz_reset_rx_datapath_0 := io.clocks.gtwiz_reset_rx_datapath_0

    // SL.AW
    sl.aw.ready := blackbox.io.s_axi_awready_0
    blackbox.io.s_axi_awvalid_0 := sl.aw.valid
    blackbox.io.s_axi_awaddr_0 := sl.aw.bits.addr

    // SL.AR
    sl.ar.ready := blackbox.io.s_axi_arready_0
    blackbox.io.s_axi_arvalid_0 := sl.ar.valid
    blackbox.io.s_axi_araddr_0 := sl.ar.bits.addr

    // SL.W
    sl.w.ready := blackbox.io.s_axi_wready_0
    blackbox.io.s_axi_wvalid_0 := sl.w.valid
    blackbox.io.s_axi_wdata_0 := sl.w.bits.data
    blackbox.io.s_axi_wstrb_0 := sl.w.bits.strb

    // SL.B
    blackbox.io.s_axi_bready_0 := sl.b.ready
    sl.b.valid := blackbox.io.s_axi_bvalid_0
    sl.b.bits.id   := 0.U
    sl.b.bits.resp := blackbox.io.s_axi_bresp_0

    // SL.R
    blackbox.io.s_axi_rready_0 := sl.r.ready
    sl.r.valid := blackbox.io.s_axi_rvalid_0
    sl.r.bits.id   := 0.U
    sl.r.bits.data := blackbox.io.s_axi_rdata_0
    sl.r.bits.resp := blackbox.io.s_axi_rresp_0
    sl.r.bits.last := true.B

    // MAC
    blackbox.io.tx_mii_d_0 := io.mac.tx_mii_d_0
    blackbox.io.tx_mii_c_0 := io.mac.tx_mii_c_0
    io.mac.rx_mii_d_0 := blackbox.io.rx_mii_d_0
    io.mac.rx_mii_c_0 := blackbox.io.rx_mii_c_0

    // Junk
    blackbox.io.txoutclksel_in_0 := 5.U
    blackbox.io.rxoutclksel_in_0 := 5.U
    blackbox.io.pm_tick_0 := false.B
  }
}
