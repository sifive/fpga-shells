package shell.intel

import Chisel.Clock
import chisel3._
import chisel3.core.{Analog, Input, withClockAndReset}
import chisel3.experimental.RawModule
import freechips.rocketchip.config.Parameters

trait MemIfSGX {
  val local_init_done   = Output(Bool())

  val global_reset_n    = Input(Bool())
  val pll_ref_clk       = Input(Clock())
  val soft_reset_n      = Input(Bool())

  val reset_phy_clk_n   = Output(Clock())
  val mem_odt   = Output(UInt(2.W))
  val mem_cs_n  = Output(UInt(2.W))
  val mem_cke   = Output(UInt(2.W))
  val mem_addr  = Output(UInt(14.W))
  val mem_ba    = Output(UInt(2.W))
  val mem_ras_n = Output(UInt(1.W))
  val mem_cas_n = Output(UInt(1.W))
  val mem_we_n  = Output(UInt(1.W))
  val mem_dm    = Output(UInt(8.W))
  val phy_clk           = Output(Clock())
  val aux_full_rate_clk = Output(Clock())
  val aux_half_rate_clk = Output(Clock())
  val reset_request_n = Output(Bool())
  val mem_clk   = Analog(2.W)
  val mem_clk_n = Analog(2.W)
  val mem_dq    = Analog(64.W)
  val mem_dqs   = Analog(8.W)

  def connectFrom(mem_if: MemIf): Unit = {
    local_init_done := mem_if.local_init_done

    mem_if.global_reset_n := global_reset_n
    mem_if.pll_ref_clk := pll_ref_clk
    mem_if.soft_reset_n := soft_reset_n

    reset_phy_clk_n := mem_if.reset_phy_clk_n
    mem_odt <> mem_if.mem_odt
    mem_cs_n <> mem_if.mem_cs_n
    mem_cke <> mem_if.mem_cke
    mem_addr <> mem_if.mem_addr
    mem_ba <> mem_if.mem_ba
    mem_ras_n <> mem_if.mem_ras_n
    mem_cas_n <> mem_if.mem_cas_n
    mem_we_n <> mem_if.mem_we_n
    mem_dm <> mem_if.mem_dm

    mem_clk <> mem_if.mem_clk
    mem_clk_n <> mem_if.mem_clk_n
    mem_dq <> mem_if.mem_dq
    mem_dqs <> mem_if.mem_dqs

    phy_clk := mem_if.phy_clk
    aux_full_rate_clk := mem_if.aux_full_rate_clk
    aux_half_rate_clk := mem_if.aux_half_rate_clk
    reset_request_n := mem_if.reset_request_n
  }
}

class MemIfBundleSGX extends Bundle with MemIfSGX

class sgxShell(implicit val p: Parameters) extends RawModule {

  val mem_odt   = IO(Output(UInt(2.W)))
  val mem_cs_n  = IO(Output(UInt(2.W)))
  val mem_cke   = IO(Output(UInt(2.W)))
  val mem_addr  = IO(Output(UInt(14.W)))
  val mem_ba    = IO(Output(UInt(2.W)))
  val mem_ras_n = IO(Output(UInt(1.W)))
  val mem_cas_n = IO(Output(UInt(1.W)))
  val mem_we_n  = IO(Output(UInt(1.W)))
  val mem_dm    = IO(Output(UInt(8.W)))
  val mem_clk   = IO(Analog(2.W))
  val mem_clk_n = IO(Analog(2.W))
  val mem_dq    = IO(Analog(64.W))
  val mem_dqs   = IO(Analog(8.W))

  val clk25 = IO(Input(Clock()))
  val clk27 = IO(Input(Clock()))
  val clk48 = IO(Input(Clock()))

  val key1 = IO(Input(Bool()))
  val key2 = IO(Input(Bool()))
  val key3 = IO(Input(Bool()))

  val led_0 = IO(Output(Bool()))
  val led_1 = IO(Output(Bool()))
  val led_2 = IO(Output(Bool()))
  val led_3 = IO(Output(Bool()))

  val jtag_tdi = IO(Input(Bool()))
  val jtag_tdo = IO(Analog(1.W))
  val jtag_tck = IO(Input(Clock()))
  val jtag_tms = IO(Input(Bool()))

  val uart_rx = IO(Input(Bool()))
  val uart_tx = IO(Output(Bool()))

  val sd_cs = IO(Output(Bool()))
  val sd_sck = IO(Output(Bool()))
  val sd_mosi = IO(Output(Bool()))
  val sd_miso = IO(Input(Bool()))

  // Internal wiring

  val ddr4_rst = Wire(Bool())

  val cpu_clock = Wire(Clock())
  val cpu_rst = Wire(Bool())
  val jtag_rst = Wire(Bool())

  withClockAndReset(cpu_clock, false.B) {
    val counter = Reg(UInt(64.W))
    counter := counter + 1.U
    ddr4_rst := counter < 1000.U

    // to be overriden if DDR4 present...
    cpu_rst := counter < 2000.U
    jtag_rst := counter < 3000.U
  }

  def wireMemory(mem_if: MemIfBundle): Unit = {
    mem_odt <> mem_if.mem_odt
    mem_cs_n <> mem_if.mem_cs_n
    mem_cke <> mem_if.mem_cke
    mem_addr <> mem_if.mem_addr
    mem_ba <> mem_if.mem_ba
    mem_ras_n <> mem_if.mem_ras_n
    mem_cas_n <> mem_if.mem_cas_n
    mem_we_n <> mem_if.mem_we_n
    mem_dm <> mem_if.mem_dm
    mem_clk <> mem_if.mem_clk
    mem_clk_n <> mem_if.mem_clk_n
    mem_dq <> mem_if.mem_dq
    mem_dqs <> mem_if.mem_dqs

    mem_if.global_reset_n := !ddr4_rst
    mem_if.soft_reset_n := true.B

    mem_if.pll_ref_clk := clk25
    cpu_clock := clk25

    withClockAndReset(cpu_clock, !mem_if.local_init_done) {
      val counter = RegInit(0.U(64.W))
      counter := counter + 1.U
      cpu_rst := counter < 1000.U
      jtag_rst := counter < 2000.U
    }
  }
}
