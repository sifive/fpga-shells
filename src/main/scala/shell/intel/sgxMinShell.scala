package shell.intel

import Chisel.Clock
import chisel3._
import chisel3.core.{Analog, Input, withClockAndReset}
import chisel3.experimental.RawModule
import freechips.rocketchip.config.Parameters

class sgxMinShell(implicit val p: Parameters) extends RawModule {

  val clk25 = IO(Input(Clock()))

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

  // Internal wiring

  val cpu_clock = Wire(Clock())
  val cpu_rst = Wire(Bool())
  val jtag_rst = Wire(Bool())

  withClockAndReset(cpu_clock, false.B) {
    val counter = Reg(UInt(64.W))
    counter := counter + 1.U

    // to be overriden if DDR4 present...
    cpu_rst := counter < 2000.U
    jtag_rst := counter < 3000.U
  }
}
