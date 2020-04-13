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
  val rx_core_clk_0 = Input (Clock()) // >= 156.25MHz ... maybe core clock to avoid another crossing?
  val tx_mii_clk_0  = Output(Clock()) // TX data path

  val sys_reset = Input(Reset())
  val dclk      = Input(Clock()) // free-running fsm clock

  val user_rx_reset_0 = Output(AsyncReset())
  val user_tx_reset_0 = Output(AsyncReset())
}

trait HasXXVEthernetMAC {
  val rx_mii_d_0 = Output(UInt(64.W))
  val tx_mii_d_0 = Input (UInt(64.W))
  val rx_mii_c_0 = Output(UInt(8.W))
  val tx_mii_c_0 = Input (UInt(8.W))

  val gt_loopback_in_0 = Input(UInt(3.W))
  val stat_rx_block_lock_0 = Output(Bool())
}

trait HasXXVEthernetJunk {
  // Unused loopback test stuff
  val ctl_rx_test_pattern_0 = Input(Bool())
  val ctl_rx_test_pattern_enable_0 = Input(Bool())
  val ctl_rx_data_pattern_select_0 = Input(Bool())
  val ctl_rx_prbs31_test_pattern_enable_0 = Input(Bool())

  val ctl_tx_test_pattern_0 = Input(Bool())
  val ctl_tx_test_pattern_enable_0 = Input(Bool())
  val ctl_tx_test_pattern_select_0 = Input(Bool())
  val ctl_tx_data_pattern_select_0 = Input(Bool())
  val ctl_tx_test_pattern_seed_a_0 = Input(UInt(58.W))
  val ctl_tx_test_pattern_seed_b_0 = Input(UInt(58.W))
  val ctl_tx_prbs31_test_pattern_enable_0 = Input(Bool())

  // Drive these always to 0
  val rx_reset_0 = Input(Reset())
  val tx_reset_0 = Input(Reset())
  val gtwiz_reset_tx_datapath_0 = Input(Reset())
  val gtwiz_reset_rx_datapath_0 = Input(Reset())

  val gtpowergood_out_0 = Output(Bool())
  val gt_refclk_out = Output(Clock()) // 156.25MHz from xcvr refclk pads
  val rx_clk_out_0  = Output(Clock()) // RX control+status signals
  val rxrecclkout_0 = Output(Clock())

  // Drive these always to 3'b101 as per documentation
  val txoutclksel_in_0 = Input(UInt(3.W))
  val rxoutclksel_in_0 = Input(UInt(3.W))

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
}

class XXVEthernetBlackBoxIO extends Bundle
  with HasXXVEthernetPads
  with HasXXVEthernetClocks
  with HasXXVEthernetMAC
  with HasXXVEthernetJunk

class XXVEthernetPads() extends Bundle with HasXXVEthernetPads
class XXVEthernetMAC() extends Bundle with HasXXVEthernetMAC
class XXVEthernetClocks() extends Bundle with HasXXVEthernetClocks

case class XXVEthernetParams(
  name:    String,
  speed:   Int,
  dclkMHz: Double)
{
  require (speed == 10 || speed == 25)
  val refMHz = if (speed == 10) 156.25 else 161.1328125
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
       |  CONFIG.GT_REF_CLK_FREQ		{${c.refMHz}}				\\
       |  CONFIG.GT_DRP_CLK			{${c.dclkMHz}}				\\
       |] [get_ips ${desiredName}]
       |""".stripMargin)
}

class DiplomaticXXVEthernet(c: XXVEthernetParams)(implicit p:Parameters) extends LazyModule
{
  lazy val module = new LazyRawModuleImp(this) {
    val io = IO(new Bundle {
      val pads   = new XXVEthernetPads
      val mac    = new XXVEthernetMAC
      val clocks = new XXVEthernetClocks
    })

    val blackbox = Module(new XXVEthernetBlackBox(c))

    // pads
    io.pads.gt_txp_out_0 := blackbox.io.gt_txp_out_0
    io.pads.gt_txn_out_0 := blackbox.io.gt_txn_out_0
    blackbox.io.gt_rxp_in_0 := io.pads.gt_rxp_in_0
    blackbox.io.gt_rxn_in_0 := io.pads.gt_rxn_in_0
    blackbox.io.gt_refclk_p := io.pads.gt_refclk_p
    blackbox.io.gt_refclk_n := io.pads.gt_refclk_n

    // clocks
    io.clocks.tx_mii_clk_0    := blackbox.io.tx_mii_clk_0
    io.clocks.user_rx_reset_0 := blackbox.io.user_rx_reset_0
    io.clocks.user_tx_reset_0 := blackbox.io.user_tx_reset_0
    blackbox.io.rx_core_clk_0 := io.clocks.rx_core_clk_0
    blackbox.io.sys_reset     := io.clocks.sys_reset
    blackbox.io.dclk          := io.clocks.dclk

    // MAC
    blackbox.io.tx_mii_d_0 := io.mac.tx_mii_d_0
    blackbox.io.tx_mii_c_0 := io.mac.tx_mii_c_0
    io.mac.rx_mii_d_0 := blackbox.io.rx_mii_d_0
    io.mac.rx_mii_c_0 := blackbox.io.rx_mii_c_0
    blackbox.io.gt_loopback_in_0 := io.mac.gt_loopback_in_0
    io.mac.stat_rx_block_lock_0 := blackbox.io.stat_rx_block_lock_0

    // Junk
    blackbox.io.txoutclksel_in_0 := 5.U
    blackbox.io.rxoutclksel_in_0 := 5.U
    blackbox.io.rx_reset_0                := io.clocks.sys_reset
    blackbox.io.tx_reset_0                := io.clocks.sys_reset
    blackbox.io.gtwiz_reset_tx_datapath_0 := io.clocks.sys_reset
    blackbox.io.gtwiz_reset_rx_datapath_0 := io.clocks.sys_reset
    blackbox.io.ctl_rx_test_pattern_0 := false.B
    blackbox.io.ctl_rx_test_pattern_enable_0 := false.B
    blackbox.io.ctl_rx_data_pattern_select_0 := false.B
    blackbox.io.ctl_rx_prbs31_test_pattern_enable_0 := false.B
    blackbox.io.ctl_tx_test_pattern_0 := false.B
    blackbox.io.ctl_tx_test_pattern_enable_0 := false.B
    blackbox.io.ctl_tx_test_pattern_select_0 := false.B
    blackbox.io.ctl_tx_data_pattern_select_0 := false.B
    blackbox.io.ctl_tx_test_pattern_seed_a_0 := 0.U
    blackbox.io.ctl_tx_test_pattern_seed_b_0 := 0.U
    blackbox.io.ctl_tx_prbs31_test_pattern_enable_0 := false.B
  }
}
