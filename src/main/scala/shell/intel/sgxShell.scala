package shell.intel

import Chisel.Clock
import chisel3._
import chisel3.core.{Analog, IO, Input, withClockAndReset}
import chisel3.experimental.RawModule
import freechips.rocketchip.config.Parameters

object sgxShell {
  class MemIf extends Bundle {
    val mem_addr = IO(Analog(14.W))
    val mem_ba = IO(Analog(3.W))
    val mem_cas_n = IO(Analog(1.W))
    val mem_cke = IO(Analog(2.W))
    val mem_clk = IO(Analog(2.W))
    val mem_clk_n = IO(Analog(2.W))
    val mem_cs_n = IO(Analog(2.W))
    val mem_dm = IO(Analog(8.W))
    val mem_dq = IO(Analog(64.W))
    val mem_dqs = IO(Analog(8.W))
    val mem_odt = IO(Analog(2.W))
    val mem_ras_n = IO(Analog(1.W))
    val mem_we_n = IO(Analog(1.W))
  }
}

class sgxShell(implicit val p: Parameters) extends RawModule {
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
  val uart_tx = IO(Analog(1.W))

  // Internal wiring

  val cpu_clock = Wire(Clock())
  val cpu_rst = Wire(Bool())
  val jtag_rst = Wire(Bool())

  withClockAndReset(cpu_clock, false.B) {
    val counter = Reg(UInt(64.W))
    counter := counter + 1.U
    cpu_rst := (counter > 1000.U) && (counter < 2000.U)
    jtag_rst := (counter > 3000.U) && (counter < 4000.U)
  }

  cpu_clock <> clk25
}
