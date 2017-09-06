// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx.vc707shell

import Chisel._
import chisel3.core.{Input, Output, attach}
import chisel3.experimental.{RawModule, Analog, withClockAndReset}

import freechips.rocketchip.config._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.util.{SyncResetSynchronizerShiftReg}

import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._

import sifive.fpgashells.devices.xilinx.xilinxvc707mig._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import sifive.fpgashells.ip.xilinx.{IBUFDS, PowerOnResetFPGAOnly, sdio_spi_bridge, vc707clk_wiz_sync, vc707reset}

//-------------------------------------------------------------------------
// VC707Shell
//-------------------------------------------------------------------------

abstract class VC707Shell(implicit val p: Parameters) extends RawModule {

  //-----------------------------------------------------------------------
  // Interface
  //-----------------------------------------------------------------------

  // 200Mhz differential sysclk
  val sys_diff_clock_clk_n = IO(Input(Bool()))
  val sys_diff_clock_clk_p = IO(Input(Bool()))

  // active high reset
  val reset                = IO(Input(Bool()))

  // DDR SDRAM
  val ddr3_addr            = IO(Output(UInt(14.W)))
  val ddr3_ba              = IO(Output(UInt(3.W)))
  val ddr3_cas_n           = IO(Output(Bool()))
  val ddr3_ck_p            = IO(Output(Bool()))
  val ddr3_ck_n            = IO(Output(Bool()))
  val ddr3_cke             = IO(Output(Bool()))
  val ddr3_cs_n            = IO(Output(Bool()))
  val ddr3_dm              = IO(Output(UInt(8.W)))
  val ddr3_dq              = IO(Analog(64.W))
  val ddr3_dqs_n           = IO(Analog(8.W))
  val ddr3_dqs_p           = IO(Analog(8.W))
  val ddr3_odt             = IO(Output(Bool()))
  val ddr3_ras_n           = IO(Output(Bool()))
  val ddr3_reset_n         = IO(Output(Bool()))
  val ddr3_we_n            = IO(Output(Bool()))

  // LED
  val led                  = IO(Vec(8, Output(Bool())))

  // UART
  val uart_tx              = IO(Output(Bool()))
  val uart_rx              = IO(Input(Bool()))
  val uart_rtsn            = IO(Output(Bool()))
  val uart_ctsn            = IO(Input(Bool()))

  // SDIO
  val sdio_clk             = IO(Output(Bool()))
  val sdio_cmd             = IO(Analog(1.W))
  val sdio_dat             = IO(Analog(4.W))

  // JTAG
  val jtag_TCK             = IO(Input(Clock()))
  val jtag_TMS             = IO(Input(Bool()))
  val jtag_TDI             = IO(Input(Bool()))
  val jtag_TDO             = IO(Output(Bool()))

  // PCIe
  val pci_exp_txp          = IO(Output(Bool()))
  val pci_exp_txn          = IO(Output(Bool()))
  val pci_exp_rxp          = IO(Input(Bool()))
  val pci_exp_rxn          = IO(Input(Bool()))
  val pci_exp_refclk_rxp   = IO(Input(Bool()))
  val pci_exp_refclk_rxn   = IO(Input(Bool()))

  //-----------------------------------------------------------------------
  // Wire declrations
  //-----------------------------------------------------------------------

  val sys_clock       = Wire(Clock())
  val sys_reset       = Wire(Bool())

  val dut_clock       = Wire(Clock())
  val dut_reset       = Wire(Bool())
  val dut_resetn      = Wire(Bool())

  val dut_ndreset     = Wire(Bool())

  val sd_spi_sck      = Wire(Bool())
  val sd_spi_cs       = Wire(Bool())
  val sd_spi_dq_i     = Wire(Vec(4, Bool()))
  val sd_spi_dq_o     = Wire(Vec(4, Bool()))

  val do_reset        = Wire(Bool())

  val mig_mmcm_locked = Wire(Bool())
  val mig_sys_reset   = Wire(Bool())

  val mig_clock       = Wire(Clock())
  val mig_reset       = Wire(Bool())
  val mig_resetn      = Wire(Bool())

  val pcie_dat_reset  = Wire(Bool())
  val pcie_dat_resetn = Wire(Bool())
  val pcie_cfg_reset  = Wire(Bool())
  val pcie_cfg_resetn = Wire(Bool())
  val pcie_dat_clock  = Wire(Clock())
  val pcie_cfg_clock  = Wire(Clock())
  val mmcm_lock_pcie  = Wire(Bool())

  //-----------------------------------------------------------------------
  // Differential clock
  //-----------------------------------------------------------------------

  val sys_clk_ibufds = Module(new IBUFDS)
  sys_clk_ibufds.io.I  := sys_diff_clock_clk_p
  sys_clk_ibufds.io.IB := sys_diff_clock_clk_n

  //-----------------------------------------------------------------------
  // System clock and reset
  //-----------------------------------------------------------------------

  // Clock that drives the clock generator and the MIG
  sys_clock := sys_clk_ibufds.io.O.asClock

  // Allow the debug module to reset everything. Resets the MIG
  sys_reset := reset | dut_ndreset

  //-----------------------------------------------------------------------
  // Clock Generator
  //-----------------------------------------------------------------------

  val coreplex_mmcm = Module(new vc707clk_wiz_sync)
  coreplex_mmcm.io.clk_in1 := sys_clock.asUInt
  coreplex_mmcm.io.reset   := mig_sys_reset

  val clk12_5              = coreplex_mmcm.io.clk_out1
  val clk25                = coreplex_mmcm.io.clk_out2
  val clk37_5              = coreplex_mmcm.io.clk_out3
  val clk50                = coreplex_mmcm.io.clk_out4
  val clk100               = coreplex_mmcm.io.clk_out5
  val clk150               = coreplex_mmcm.io.clk_out6
  val clk75                = coreplex_mmcm.io.clk_out7
  val coreplex_mmcm_locked = coreplex_mmcm.io.locked

  // DUT clock
  dut_clock := clk37_5

  //-----------------------------------------------------------------------
  // System reset
  //-----------------------------------------------------------------------

  do_reset             := !mig_mmcm_locked || !mmcm_lock_pcie || mig_sys_reset || !coreplex_mmcm_locked
  mig_resetn           := !mig_reset
  dut_resetn           := !dut_reset
  pcie_dat_resetn      := !pcie_dat_reset
  pcie_cfg_resetn      := !pcie_cfg_reset

  val safe_reset = Module(new vc707reset)

  safe_reset.io.areset := do_reset
  safe_reset.io.clock1 := mig_clock
  mig_reset            := safe_reset.io.reset1
  safe_reset.io.clock2 := pcie_dat_clock
  pcie_dat_reset       := safe_reset.io.reset2
  safe_reset.io.clock3 := pcie_cfg_clock
  pcie_cfg_reset       := safe_reset.io.reset3
  safe_reset.io.clock4 := dut_clock
  dut_reset            := safe_reset.io.reset4

  //---------------------------------------------------------------------
  // Debug JTAG
  //---------------------------------------------------------------------

  def connectDebugJTAG(dut: HasPeripheryDebugModuleImp): Unit = {
    val djtag     = dut.debug.systemjtag.get

    djtag.jtag.TCK := jtag_TCK
    djtag.jtag.TMS := jtag_TMS
    djtag.jtag.TDI := jtag_TDI
    jtag_TDO       := djtag.jtag.TDO.data

    djtag.mfr_id   := p(JtagDTMKey).idcodeManufId.U(11.W)

    djtag.reset    := PowerOnResetFPGAOnly(dut_clock)
    dut_ndreset    := dut.debug.ndreset
  }

  //-----------------------------------------------------------------------
  // UART
  //-----------------------------------------------------------------------

  uart_rtsn := false.B

  def connectUART(dut: HasPeripheryUARTModuleImp): Unit = {
    val uartParams = p(PeripheryUARTKey)
    if (!uartParams.isEmpty) {
      // uart connections
      dut.uart(0).rxd := SyncResetSynchronizerShiftReg(uart_rx, 2, init = Bool(true), name=Some("uart_rxd_sync"))
      uart_tx         := dut.uart(0).txd
    }
  }

  //-----------------------------------------------------------------------
  // SPI
  //-----------------------------------------------------------------------

  def connectSPI(dut: HasPeripherySPIModuleImp): Unit = {
    // SPI
    sd_spi_sck := dut.spi(0).sck
    sd_spi_cs  := dut.spi(0).cs(0)

    dut.spi(0).dq.zipWithIndex.foreach {
      case(pin, idx) =>
        sd_spi_dq_o(idx) := pin.o
        pin.i            := sd_spi_dq_i(idx)
    }

    //-------------------------------------------------------------------
    // SDIO <> SPI Bridge
    //-------------------------------------------------------------------

    val ip_sdio_spi = Module(new sdio_spi_bridge())

    ip_sdio_spi.io.clk   := dut_clock
    ip_sdio_spi.io.reset := dut_reset

    // SDIO
    attach(sdio_dat, ip_sdio_spi.io.sd_dat)
    attach(sdio_cmd, ip_sdio_spi.io.sd_cmd)
    sdio_clk := ip_sdio_spi.io.spi_sck

    // SPI
    ip_sdio_spi.io.spi_sck  := sd_spi_sck
    ip_sdio_spi.io.spi_cs   := sd_spi_cs
    sd_spi_dq_i             := ip_sdio_spi.io.spi_dq_i.toBools
    ip_sdio_spi.io.spi_dq_o := sd_spi_dq_o.asUInt
  }

  //---------------------------------------------------------------------
  // MIG
  //---------------------------------------------------------------------

  def connectMIG(dut: HasMemoryXilinxVC707MIGModuleImp): Unit = {

    // Clock & Reset
    dut.xilinxvc707mig.sys_clk_i := sys_clock.asUInt
    mig_clock                    := dut.xilinxvc707mig.ui_clk
    mig_sys_reset                := dut.xilinxvc707mig.ui_clk_sync_rst
    mig_mmcm_locked              := dut.xilinxvc707mig.mmcm_locked
    dut.xilinxvc707mig.aresetn   := mig_resetn
    dut.xilinxvc707mig.sys_rst   := sys_reset

    // Outputs
    ddr3_addr    := dut.xilinxvc707mig.ddr3_addr
    ddr3_ba      := dut.xilinxvc707mig.ddr3_ba
    ddr3_ras_n   := dut.xilinxvc707mig.ddr3_ras_n
    ddr3_cas_n   := dut.xilinxvc707mig.ddr3_cas_n
    ddr3_we_n    := dut.xilinxvc707mig.ddr3_we_n
    ddr3_reset_n := dut.xilinxvc707mig.ddr3_reset_n
    ddr3_ck_p    := dut.xilinxvc707mig.ddr3_ck_p
    ddr3_ck_n    := dut.xilinxvc707mig.ddr3_ck_n
    ddr3_cke     := dut.xilinxvc707mig.ddr3_cke
    ddr3_cs_n    := dut.xilinxvc707mig.ddr3_cs_n
    ddr3_dm      := dut.xilinxvc707mig.ddr3_dm
    ddr3_odt     := dut.xilinxvc707mig.ddr3_odt

    attach(ddr3_dq,    dut.xilinxvc707mig.ddr3_dq)
    attach(ddr3_dqs_n, dut.xilinxvc707mig.ddr3_dqs_n)
    attach(ddr3_dqs_p, dut.xilinxvc707mig.ddr3_dqs_p)
  }

  //---------------------------------------------------------------------
  // PCIE
  //---------------------------------------------------------------------

  def connectPCIe(dut: HasSystemXilinxVC707PCIeX1ModuleImp): Unit = {
    // Clock & Reset
    dut.xilinxvc707pcie.axi_aresetn     := pcie_dat_resetn
    pcie_dat_clock                      := dut.xilinxvc707pcie.axi_aclk_out
    pcie_cfg_clock                      := dut.xilinxvc707pcie.axi_ctl_aclk_out
    mmcm_lock_pcie                      := dut.xilinxvc707pcie.mmcm_lock
    dut.xilinxvc707pcie.axi_ctl_aresetn := pcie_dat_resetn
    dut.xilinxvc707pcie.REFCLK_rxp      := pci_exp_refclk_rxp
    dut.xilinxvc707pcie.REFCLK_rxn      := pci_exp_refclk_rxn

    // PCIeX1 connections
    pci_exp_txp                         := dut.xilinxvc707pcie.pci_exp_txp
    pci_exp_txn                         := dut.xilinxvc707pcie.pci_exp_txn
    dut.xilinxvc707pcie.pci_exp_rxp     := pci_exp_rxp
    dut.xilinxvc707pcie.pci_exp_rxn     := pci_exp_rxn
  }

}
