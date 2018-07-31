// See LICENSE for license details.
package sifive.fpgashells.shell.microsemi.verashell

import Chisel._
import chisel3.core.{Input, Output, attach}
import chisel3.experimental.{RawModule, Analog, withClockAndReset}

import freechips.rocketchip.config._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.util.{SyncResetSynchronizerShiftReg, ResetCatchAndSync, ElaborationArtefacts, HeterogeneousBag, SimpleTimer}

import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._

import sifive.fpgashells.ip.microsemi.{CLKINT}

import sifive.fpgashells.devices.microsemi.polarfireddr3._
import sifive.fpgashells.devices.microsemi.polarfireddr4._

import sifive.fpgashells.ip.microsemi.corejtagdebug._
import sifive.fpgashells.ip.microsemi.polarfireccc._
import sifive.fpgashells.ip.microsemi.polarfireinitmonitor._
import sifive.fpgashells.ip.microsemi.polarfirereset._

import sifive.fpgashells.devices.microsemi.polarfireevalkitpciex4._
import sifive.fpgashells.ip.microsemi.polarfirexcvrrefclk._
import sifive.fpgashells.ip.microsemi.polarfiretxpll._

import sifive.fpgashells.ip.microsemi.polarfire_oscillator._
import sifive.fpgashells.ip.microsemi.polarfireclockdivider._
import sifive.fpgashells.ip.microsemi.polarfireglitchlessmux._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.clocks._
//-------------------------------------------------------------------------
// Vera Shell
//-------------------------------------------------------------------------

trait HasDDR3 { this: VeraShell =>
  
  require(!p.lift(MemoryMicrosemiDDR3Key).isEmpty)
  val ddr = IO(new PolarFireEvalKitDDR3Pads(p(MemoryMicrosemiDDR3Key)))
  
  def connectMIG(dut: HasMemoryPolarFireEvalKitDDR3ModuleImp): Unit = {
    // Clock & Reset
    dut.polarfireddrsubsys.PLL_REF_CLK := mig_clock_in
    dut.polarfireddrsubsys.SYS_RESET_N := sys_reset_n
    
    mig_clock_out       := dut.polarfireddrsubsys.SYS_CLK
    mig_plllock_out     := dut.polarfireddrsubsys.PLL_LOCK
    ddr_ctrlr_ready     := dut.polarfireddrsubsys.CTRLR_READY
    
    ddr <> dut.polarfireddrsubsys
  }
}

trait HasDDR4 { this: VeraShell =>
  
  require(!p.lift(MemoryMicrosemiDDR4Key).isEmpty)
  val ddr = IO(new PolarFireEvalKitDDR4Pads(p(MemoryMicrosemiDDR4Key)))
  
  def connectMIG(dut: HasMemoryPolarFireEvalKitDDR4ModuleImp): Unit = {
    // Clock & Reset
    dut.polarfireddrsubsys.PLL_REF_CLK := mig_clock_in
    dut.polarfireddrsubsys.SYS_RESET_N := sys_reset_n
    
    mig_clock_out       := dut.polarfireddrsubsys.SYS_CLK
    mig_plllock_out     := dut.polarfireddrsubsys.PLL_LOCK
    ddr_ctrlr_ready     := dut.polarfireddrsubsys.CTRLR_READY
    
    ddr <> dut.polarfireddrsubsys
  }
}

trait HasPCIe { this: VeraShell =>
  val pcie = IO(new PolarFireEvalKitPCIeX4Pads)

  def connectPCIe(dut: HasSystemPolarFireEvalKitPCIeX4ModuleImp): Unit = {
    // Clock & Reset
    dut.pf_eval_kit_pcie.APB_S_PCLK     := hart_clk_25
    dut.pf_eval_kit_pcie.APB_S_PRESET_N := UInt("b1")
    
    dut.pf_eval_kit_pcie.AXI_CLK        := hart_clk_125
    dut.pf_eval_kit_pcie.AXI_CLK_STABLE := hart_clk_lock
    
    dut.pf_eval_kit_pcie.PCIE_1_TL_CLK_125MHz   := pcie_tl_clk
    
    dut.pf_eval_kit_pcie.PCIE_1_TX_PLL_REF_CLK  := pf_tx_pll_refclk_to_lane

    dut.pf_eval_kit_pcie.PCIE_1_TX_BIT_CLK := pf_tx_pll_bitclk
    
    dut.pf_eval_kit_pcie.PCIESS_LANE0_CDR_REF_CLK_0 := pcie_refclk
    dut.pf_eval_kit_pcie.PCIESS_LANE1_CDR_REF_CLK_0 := pcie_refclk
    dut.pf_eval_kit_pcie.PCIESS_LANE2_CDR_REF_CLK_0 := pcie_refclk
    dut.pf_eval_kit_pcie.PCIESS_LANE3_CDR_REF_CLK_0 := pcie_refclk

    dut.pf_eval_kit_pcie.PCIE_1_TX_PLL_LOCK := pf_tx_pll_lock

    pcie <> dut.pf_eval_kit_pcie
  }
}


trait HasPFEvalKitChipLink { this: VeraShell =>

  val chiplink = IO(new WideDataLayerPort(ChipLinkParams(Nil,Nil)))
  val ereset_n = IO(Bool(INPUT))

  def constrainChipLink(iofpga: Boolean = false): Unit = {
    val direction0Pins = if(iofpga) "chiplink_b2c"  else "chiplink_c2b"
    val direction1Pins = if(iofpga) "chiplink_c2b"  else "chiplink_b2c"
  }

  def connectChipLink(dut: { val chiplink: Seq[WideDataLayerPort] } , iofpga: Boolean = false): Unit = {
    constrainChipLink(iofpga)

    chiplink <> dut.chiplink(0)
  }
}

abstract class VeraShell(implicit val p: Parameters) extends RawModule {

  //-----------------------------------------------------------------------
  // Interface
  //-----------------------------------------------------------------------
  
  // 50MHz crystal oscillator
  val ref_clk0             = IO(Input(Clock()))

  // 
  val ref_clk_pad_p = IO(Input(Bool()))
  val ref_clk_pad_n = IO(Input(Bool()))
  
  // Reset push-button - active low
  val pf_user_reset_n      = IO(Input(Bool()))
  
  // LED
//  val led                  = IO(Vec(8, Output(Bool())))
  val led2                   = IO(Output(Bool()))
  val led3                   = IO(Output(Bool()))
  val led4                   = IO(Output(Bool()))
  val led5                   = IO(Output(Bool()))
  
  // PCIe switch reset
  val pf_rstb              = IO(Output(Bool()))
  
  // PCIe slots reset signals
  val perst_x1_slot        = IO(Output(Bool()))
  val perst_x16_slot       = IO(Output(Bool()))
  val perst_m2_slot        = IO(Output(Bool()))
  val perst_sata_slot      = IO(Output(Bool()))

  
  // debug
  val debug_io0            = IO(Output(Clock()))
  val debug_io1            = IO(Output(Bool()))
  val debug_io2            = IO(Output(Bool()))
  val debug_io3            = IO(Output(Bool()))
  val debug_io4            = IO(Output(Bool()))
  val debug_io5            = IO(Output(Bool()))
  
  //-----------------------------------------------------------------------
  // Wire declarations
  //-----------------------------------------------------------------------

  val sys_clock       = Wire(Clock())
  val sys_reset_n     = Wire(Bool())

  val dut_clock       = Wire(Clock())
  val dut_reset       = Wire(Bool())
  val dut_reset_i     = Wire(Bool())
  val dut_reset_sync  = Wire(Bool())

  val pcie_sw_rst_complete  = Wire(Bool())

  val dut_ndreset     = Wire(Bool())
  val dut_ext_reset_n = Wire(Bool())

  val mig_mmcm_locked = Wire(Bool())
  val mig_sys_reset   = Wire(Bool())

  val mig_clock       = Wire(Clock())
  val mig_reset       = Wire(Bool())
  val mig_resetn      = Wire(Bool())
  
  val mig_clock_in    = Wire(Clock())
  val mig_clock_out   = Wire(Clock())
  val mig_plllock_out = Wire(Bool())
  
  val ddr_ready       = Wire(Bool())
  val ddr_pll_lock    = Wire(Bool())

  val pcie_dat_reset  = Wire(Bool())
  val pcie_dat_resetn = Wire(Bool())
  val pcie_cfg_reset  = Wire(Bool())
  val pcie_cfg_resetn = Wire(Bool())
  val pcie_dat_clock  = Wire(Clock())
  val pcie_cfg_clock  = Wire(Clock())
  val mmcm_lock_pcie  = Wire(Bool())

  val fpga_reset      = Wire(Bool())
  val ddr_ctrlr_ready = Wire(Bool())
  
  //-----------------------------------------------------------------------
  // Differential clock
  //-----------------------------------------------------------------------
  val pf_xcvr_ref_clk = Module(new PolarFireTransceiverRefClk)
  pf_xcvr_ref_clk.io.REF_CLK_PAD_P := ref_clk_pad_p
  pf_xcvr_ref_clk.io.REF_CLK_PAD_N := ref_clk_pad_n
  val pcie_refclk = pf_xcvr_ref_clk.io.REF_CLK
  val pcie_fab_ref_clk = pf_xcvr_ref_clk.io.FAB_REF_CLK

  val pf_tx_pll = Module(new PolarFireTxPLL)
  pf_tx_pll.io.REF_CLK := pcie_refclk
  val pf_tx_pll_bitclk = pf_tx_pll.io.BIT_CLK
  val pf_tx_pll_refclk_to_lane = pf_tx_pll.io.REF_CLK_TO_LANE
  val pf_tx_pll_lock = pf_tx_pll.io.LOCK

  //-----------------------------------------------------------------------
  // Coreplex Clock Generator
  //-----------------------------------------------------------------------
  val ref_clk_int = Module(new CLKINT)
  val hart_clk_ccc = Module(new PolarFireCCC(PLLParameters(
    name = "hart_clk_ccc",
    PLLInClockParameters(50),
    Seq(
      PLLOutClockParameters(25),
      PLLOutClockParameters(125),
      PLLOutClockParameters(125, 31.5)))))
  
  val hart_clk_25     = hart_clk_ccc.io.OUT0_FABCLK_0.get
  val hart_clk_125    = hart_clk_ccc.io.OUT1_FABCLK_0.get
  val hart_clk_125_tx = hart_clk_ccc.io.OUT2_FABCLK_0.get
  val hart_clk_lock   = hart_clk_ccc.io.PLL_LOCK_0
  
  ref_clk_int.io.A := ref_clk0
  hart_clk_ccc.io.REF_CLK_0 := ref_clk_int.io.Y
  
  // DUT clock
  dut_clock := hart_clk_25

  //-----------------------------------------------------------------------
  // System reset
  //-----------------------------------------------------------------------
  val pf_init_monitor = Module(new PolarFireInitMonitor)

  val pf_reset = Module(new PolarFireReset)
  
  pf_reset.io.CLK           := dut_clock
  pf_reset.io.PLL_LOCK      := hart_clk_lock
  pf_reset.io.INIT_DONE     := pf_init_monitor.io.DEVICE_INIT_DONE
  pf_reset.io.EXT_RST_N     := dut_ext_reset_n  //ereset_n   //pf_user_reset_n
  
  pf_reset.io.SS_BUSY       := UInt("b0")
  pf_reset.io.FF_US_RESTORE := UInt("b0")
  
  fpga_reset    := !pf_reset.io.FABRIC_RESET_N & ddr_ready & ddr_pll_lock
  sys_reset_n   := pf_reset.io.FABRIC_RESET_N

  mig_resetn           := !mig_reset
  pcie_dat_resetn      := !pcie_dat_reset
  pcie_cfg_resetn      := !pcie_cfg_reset

  dut_reset := !pf_reset.io.FABRIC_RESET_N

  
  //overrided in connectMIG and connect PCIe
  //provide defaults to allow above reset sequencing logic to work without both
  mig_clock            := dut_clock
  pcie_dat_clock       := dut_clock
  pcie_cfg_clock       := dut_clock
  mig_mmcm_locked      := UInt("b1")
  mmcm_lock_pcie       := UInt("b1")
 
  //-----------------------------------------------------------------------
  // PCIe Subsystem TL Clock
  //-----------------------------------------------------------------------
  val pf_oscillator = Module(new PolarFireOscillator)
  val pf_clk_divider = Module(new PolarFireClockDivider) 
  val pf_glitchless_mux = Module(new PolarFireGlitchlessMux)
  pf_clk_divider.io.CLK_IN  := pf_oscillator.io.RCOSC_160MHZ_GL
  pf_glitchless_mux.io.CLK0 := pf_clk_divider.io.CLK_OUT
  pf_glitchless_mux.io.CLK1 := pf_tx_pll.io.CLK_125
  pf_glitchless_mux.io.SEL  := pf_init_monitor.io.PCIE_INIT_DONE
  val pcie_tl_clk = pf_glitchless_mux.io.CLK_OUT
}
