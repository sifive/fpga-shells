// See LICENSE for license details.
package sifive.fpgashells.shell.microsemi.polarfireavalanchekitshell

import Chisel._
import chisel3.core.{Input, Output, attach}
import chisel3.experimental.{RawModule, Analog, withClockAndReset}

import freechips.rocketchip.config._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.util.{SyncResetSynchronizerShiftReg, ResetCatchAndSync, ElaborationArtefacts, HeterogeneousBag}

import sifive.blocks.devices.gpio._
//import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._

import sifive.fpgashells.ip.microsemi.{CLKINT}

import sifive.fpgashells.devices.microsemi.polarfireddr3._
import sifive.fpgashells.ip.microsemi.corejtagdebug._
import sifive.fpgashells.ip.microsemi.polarfireccc._
import sifive.fpgashells.ip.microsemi.polarfireinitmonitor._
import sifive.fpgashells.ip.microsemi.polarfirereset._

import sifive.fpgashells.ip.microsemi.polarfire_oscillator._
import sifive.fpgashells.ip.microsemi.polarfireclockdivider._

import sifive.fpgashells.clocks._


//-------------------------------------------------------------------------
// PolarFire Avalanche Kit Shell
//-------------------------------------------------------------------------

trait HasDDR3 { this: PolarFireAvalancheKitShell =>

  require(!p.lift(MemoryMicrosemiAvalancheBoardDDR3Key).isEmpty)
  val ddr = IO(new PolarFireAvalancheBoardDDR3Pads(p(MemoryMicrosemiAvalancheBoardDDR3Key)))

  def connectMIG(dut: HasMemoryPolarFireAvalancheBoardDDR3ModuleImp): Unit = {
    // Clock & Reset
    dut.polarfireddrsubsys.PLL_REF_CLK := mig_clock_in
    dut.polarfireddrsubsys.SYS_RESET_N := sys_reset_n

    mig_clock_out       := dut.polarfireddrsubsys.SYS_CLK
    mig_plllock_out     := dut.polarfireddrsubsys.PLL_LOCK
    ddr_ctrlr_ready     := dut.polarfireddrsubsys.CTRLR_READY

    ddr <> dut.polarfireddrsubsys
  }
}

abstract class PolarFireAvalancheKitShell(implicit val p: Parameters) extends RawModule {

  //-----------------------------------------------------------------------
  // Interface
  //-----------------------------------------------------------------------

  // Reset push-button - active low
  val pf_user_reset_n      = IO(Input(Bool()))

  // LED
  val led                  = IO(Vec(4, Output(Bool())))

  // UART
  val uart_tx              = IO(Output(Bool()))
  val uart_rx              = IO(Input(Bool()))

  // JTAG
  val jtag_TRSTB           = IO(Input(Bool()))
  val jtag_TCK             = IO(Input(Clock()))
  val jtag_TMS             = IO(Input(Bool()))
  val jtag_TDI             = IO(Input(Bool()))
  val jtag_TDO             = IO(Output(Bool()))

  //-----------------------------------------------------------------------
  // Wire declarations
  //-----------------------------------------------------------------------

  val sys_clock       = Wire(Clock())
  val sys_reset_n     = Wire(Bool())

  val dut_clock       = Wire(Clock())
  val dut_reset       = Wire(Bool())
  val dut_reset_i     = Wire(Bool())
  val dut_reset_sync  = Wire(Bool())

  val dut_ndreset     = Wire(Bool())

  val mig_mmcm_locked = Wire(Bool())
  val mig_sys_reset   = Wire(Bool())

  val mig_clock       = Wire(Clock())
  val mig_reset       = Wire(Bool())
  val mig_resetn      = Wire(Bool())

  val mig_clock_in    = Wire(Clock())
  val mig_clock_out   = Wire(Clock())
  val mig_plllock_out = Wire(Bool())

  val fpga_reset      = Wire(Bool())
  val ddr_ctrlr_ready = Wire(Bool())
  val ref_clk0        = Wire(Clock())

  //-----------------------------------------------------------------------
  // PolarFire Oscillator Instatiation
  //-----------------------------------------------------------------------

  val pf_oscillator = Module(new PolarFireOscillator)
  ref_clk0 := pf_oscillator.io.RCOSC_160MHZ_GL

  //-----------------------------------------------------------------------
  // DDR3 Subsystem Clocks
  //-----------------------------------------------------------------------
  val ddr3_clk_ccc = Module(new PolarFireCCC(
   PLLParameters(
    name = "ddr3_clk_ccc",
    PLLInClockParameters(166.666),
    Seq(
      PLLOutClockParameters(133.333))))) 

  ddr3_clk_ccc.io.REF_CLK_0 := ref_clk0
  val ddr3_clk_in = ddr3_clk_ccc.io.OUT0_FABCLK_0.get
  val ddr3_clk_in_lock = ddr3_clk_ccc.io.PLL_LOCK_0
  mig_clock_in := ddr3_clk_in

  //-----------------------------------------------------------------------
  // Coreplex Clock Generator
  //-----------------------------------------------------------------------
  val hart_clk_ccc = Module(new PolarFireCCC(PLLParameters(
    name = "hart_clk_ccc",
    PLLInClockParameters(166.666),
    Seq(
      PLLOutClockParameters(25)))))

  val hart_clk_25   = hart_clk_ccc.io.OUT0_FABCLK_0.get
  val hart_clk_lock = hart_clk_ccc.io.PLL_LOCK_0

  // DUT clock
  hart_clk_ccc.io.REF_CLK_0 := mig_clock_out
  dut_clock := hart_clk_25



  //-----------------------------------------------------------------------
  // System reset
  //-----------------------------------------------------------------------
  val pf_init_monitor = Module(new PolarFireInitMonitor)

  val pf_reset = Module(new PolarFireReset)

  pf_reset.io.CLK           := ddr3_clk_in
  pf_reset.io.PLL_LOCK      := ddr3_clk_in_lock
  pf_reset.io.INIT_DONE     := pf_init_monitor.io.DEVICE_INIT_DONE
  pf_reset.io.EXT_RST_N     := pf_user_reset_n

  pf_reset.io.SS_BUSY       := UInt("b0")
  pf_reset.io.FF_US_RESTORE := UInt("b0")

  fpga_reset := !pf_reset.io.FABRIC_RESET_N

  sys_reset_n   := pf_reset.io.FABRIC_RESET_N

  mig_resetn           := !mig_reset


  dut_reset_i := !pf_reset.io.FABRIC_RESET_N | !hart_clk_lock | !ddr_ctrlr_ready

  withClockAndReset(dut_clock, fpga_reset) {
    dut_reset := ResetCatchAndSync(dut_clock, dut_reset_i, 10)
  }

  //overrided in connectMIG and connect PCIe
  //provide defaults to allow above reset sequencing logic to work without both
  mig_clock            := dut_clock
  mig_mmcm_locked      := UInt("b1")


  led(3) := dut_ndreset
  led(2) := !pf_user_reset_n
  led(1) := fpga_reset
  led(0) := dut_reset


  //---------------------------------------------------------------------
  // Debug JTAG
  //---------------------------------------------------------------------

  // JTAG inside the FPGA fabric through user JTAG FPGA macro (UJTAG)

  val fpga_jtag = Module(new CoreJtagDebugBlock)

  def connectDebugJTAG(dut: HasPeripheryDebugModuleImp): SystemJTAGIO = {
    val djtag     = dut.debug.systemjtag.get

    djtag.jtag.TCK          := fpga_jtag.io.TGT_TCK_0
    djtag.jtag.TMS          := fpga_jtag.io.TGT_TMS_0
    djtag.jtag.TDI          := fpga_jtag.io.TGT_TDI_0
    fpga_jtag.io.TGT_TDO_0    := djtag.jtag.TDO.data

    fpga_jtag.io.TRSTB  := jtag_TRSTB
    fpga_jtag.io.TCK    := jtag_TCK
    fpga_jtag.io.TMS    := jtag_TMS
    fpga_jtag.io.TDI	  := jtag_TDI
    jtag_TDO            := fpga_jtag.io.TDO

    djtag.mfr_id   := p(JtagDTMKey).idcodeManufId.U(11.W)

    djtag.reset    := fpga_reset
    dut_ndreset    := dut.debug.ndreset
    djtag
  }

  //-----------------------------------------------------------------------
  // UART
  //-----------------------------------------------------------------------

  def connectUART(dut: HasPeripheryUARTModuleImp): Unit = {
    val uartParams = p(PeripheryUARTKey)
    if (!uartParams.isEmpty) {
      // uart connections
      dut.uart(0).rxd := SyncResetSynchronizerShiftReg(uart_rx, 2, init = Bool(true), name=Some("uart_rxd_sync"))
      uart_tx         := dut.uart(0).txd
    }
  }


}
