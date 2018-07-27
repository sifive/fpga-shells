// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx.vcu118shell

import Chisel._
import chisel3.core.{Input, Output, attach}
import chisel3.experimental.{RawModule, Analog, withClockAndReset}

import freechips.rocketchip.config._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.util.{SyncResetSynchronizerShiftReg}

import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._

import sifive.fpgashells.devices.xilinx.xilinxvcu118mig._
import sifive.fpgashells.ip.xilinx.{IBUFDS, PowerOnResetFPGAOnly, sdio_spi_bridge, vcu118_sys_clock_mmcm0,
                                    vcu118_sys_clock_mmcm1, vcu118reset}

//-------------------------------------------------------------------------
// VCU118Shell
//-------------------------------------------------------------------------

trait HasDDR3 { this: VCU118Shell =>
  
  require(!p.lift(MemoryXilinxDDRKey).isEmpty)
  val ddr = IO(new XilinxVCU118MIGPads(p(MemoryXilinxDDRKey)))
  
  def connectMIG(dut: HasMemoryXilinxVCU118MIGModuleImp): Unit = {
    // Clock & Reset
    dut.xilinxvcu118mig.c0_sys_clk_i := sys_clock.asUInt
    mig_clock                    := dut.xilinxvcu118mig.c0_ddr4_ui_clk
    mig_sys_reset                := dut.xilinxvcu118mig.c0_ddr4_ui_clk_sync_rst
    dut.xilinxvcu118mig.c0_ddr4_aresetn   := mig_resetn
    dut.xilinxvcu118mig.sys_rst   := sys_reset

    ddr <> dut.xilinxvcu118mig
  }
}

abstract class VCU118Shell(implicit val p: Parameters) extends RawModule {

  //-----------------------------------------------------------------------
  // Interface
  //-----------------------------------------------------------------------
  
  // 250Mhz differential sysclk
  val sys_diff_clock_clk_n = IO(Input(Clock()))
  val sys_diff_clock_clk_p = IO(Input(Clock()))

  // active high reset
  val reset                = IO(Input(Bool()))

  // LED
  //val led                  = IO(Vec(8, Output(Bool())))

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

  //Buttons
  //val btn_0                = IO(Analog(1.W))
  //val btn_1                = IO(Analog(1.W))
  //val btn_2                = IO(Analog(1.W))
  //val btn_3                = IO(Analog(1.W))

  //Sliding switches
  //val sw_0                 = IO(Analog(1.W))
  //val sw_1                 = IO(Analog(1.W))
  //val sw_2                 = IO(Analog(1.W))
  //val sw_3                 = IO(Analog(1.W))
  //val sw_4                 = IO(Analog(1.W))
  //val sw_5                 = IO(Analog(1.W))
  //val sw_6                 = IO(Analog(1.W))
  //val sw_7                 = IO(Analog(1.W))


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
  sys_clock := sys_clk_ibufds.io.O

  // Allow the debug module to reset everything. Resets the MIG
  sys_reset := reset | dut_ndreset

  //-----------------------------------------------------------------------
  // Clock Generator
  //-----------------------------------------------------------------------

  //25MHz and multiples
  val vcu118_sys_clock_mmcm0 = Module(new vcu118_sys_clock_mmcm0)
  vcu118_sys_clock_mmcm0.io.clk_in1 := sys_clock.asUInt
  vcu118_sys_clock_mmcm0.io.reset   := reset
  val clk12_5              = vcu118_sys_clock_mmcm0.io.clk_out1
  val clk25                = vcu118_sys_clock_mmcm0.io.clk_out2
  val clk37_5              = vcu118_sys_clock_mmcm0.io.clk_out3
  val clk50                = vcu118_sys_clock_mmcm0.io.clk_out4
  val clk100               = vcu118_sys_clock_mmcm0.io.clk_out5
  val clk150               = vcu118_sys_clock_mmcm0.io.clk_out6
  val clk75                = vcu118_sys_clock_mmcm0.io.clk_out7
  val vcu118_sys_clock_mmcm0_locked = vcu118_sys_clock_mmcm0.io.locked

  //65MHz and multiples
  val vcu118_sys_clock_mmcm1 = Module(new vcu118_sys_clock_mmcm1)
  vcu118_sys_clock_mmcm1.io.clk_in1 := sys_clock.asUInt
  vcu118_sys_clock_mmcm1.io.reset   := reset
  val clk32_5              = vcu118_sys_clock_mmcm1.io.clk_out1
  val clk65                = vcu118_sys_clock_mmcm1.io.clk_out2
  val vcu118_sys_clock_mmcm1_locked = vcu118_sys_clock_mmcm1.io.locked

  // DUT clock
  dut_clock := clk37_5

  //-----------------------------------------------------------------------
  // System reset
  //-----------------------------------------------------------------------

  do_reset             := !mig_mmcm_locked || !mmcm_lock_pcie || mig_sys_reset || !vcu118_sys_clock_mmcm0_locked ||
                          !vcu118_sys_clock_mmcm1_locked
  mig_resetn           := !mig_reset
  dut_resetn           := !dut_reset
  pcie_dat_resetn      := !pcie_dat_reset
  pcie_cfg_resetn      := !pcie_cfg_reset


  val safe_reset = Module(new vcu118reset)

  safe_reset.io.areset := do_reset
  safe_reset.io.clock1 := mig_clock
  mig_reset            := safe_reset.io.reset1
  safe_reset.io.clock2 := pcie_dat_clock
  pcie_dat_reset       := safe_reset.io.reset2
  safe_reset.io.clock3 := pcie_cfg_clock
  pcie_cfg_reset       := safe_reset.io.reset3
  safe_reset.io.clock4 := dut_clock
  dut_reset            := safe_reset.io.reset4

  //overrided in connectMIG and connect PCIe
  //provide defaults to allow above reset sequencing logic to work without both
  mig_clock            := dut_clock
  pcie_dat_clock       := dut_clock
  pcie_cfg_clock       := dut_clock
  mig_mmcm_locked      := UInt("b1")
  mmcm_lock_pcie       := UInt("b1")
 
  //---------------------------------------------------------------------
  // Debug JTAG
  //---------------------------------------------------------------------

  def connectDebugJTAG(dut: HasPeripheryDebugModuleImp): SystemJTAGIO = {
    val djtag     = dut.debug.systemjtag.get

    djtag.jtag.TCK := jtag_TCK
    djtag.jtag.TMS := jtag_TMS
    djtag.jtag.TDI := jtag_TDI
    jtag_TDO       := djtag.jtag.TDO.data

    djtag.mfr_id   := p(JtagDTMKey).idcodeManufId.U(11.W)

    djtag.reset    := PowerOnResetFPGAOnly(dut_clock)
    dut_ndreset    := dut.debug.ndreset
    djtag
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

}
