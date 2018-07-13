// See LICENSE for license details.
package sifive.fpgashells.shell.microsemi

import chisel3._
import chisel3.experimental.IO
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.microsemi._
import sifive.blocks.devices.chiplink._

import sifive.fpgashells.devices.microsemi.polarfireevalkitpciex4._
import sifive.fpgashells.devices.microsemi.polarfireddr4._

import sifive.fpgashells.ip.microsemi._
import sifive.fpgashells.ip.microsemi.polarfirexcvrrefclk._
import sifive.fpgashells.ip.microsemi.polarfiretxpll._
import sifive.fpgashells.ip.microsemi.polarfire_oscillator._
import sifive.fpgashells.ip.microsemi.polarfireclockdivider._
import sifive.fpgashells.ip.microsemi.polarfireglitchlessmux._

class SysClockVeraOverlay(val shell: VeraShell, val name: String, params: ClockInputOverlayParams)
  extends ClockInputMicrosemiOverlay(params)
{
  val node = shell { ClockSourceNode(name, freqMHz = 50, jitterPS = 50)(ValName(name)) }

  shell { InModuleBody {
    val (c, _) = node.out(0)
    c.reset := shell.pllReset
    shell.io_pdc.addPin(io:Clock, "AG4")
  } }
}

class LEDVeraOverlay(val shell: VeraShell, val name: String, params: LEDOverlayParams)
  extends LEDMicrosemiOverlay(params, Seq("AK17", "AN17", "AM17", "AL18"))

class ChipLinkVeraOverlay(val shell: VeraShell, val name: String, params: ChipLinkOverlayParams)
  extends ChipLinkMicrosemiOverlay(params)
{
  val ereset_n = shell { InModuleBody {
    val ereset_n = IO(Input(Bool()))
    ereset_n.suggestName("ereset_n")
    shell.io_pdc.addPin(ereset_n, "U5")
    ereset_n
  } }

  shell { InModuleBody {
    val dir1 = Seq("U10", "AC8", "U9", /* clk, rst, send */
                   "V13", "W13", "T5",  "T4",  "V14", "W14", "R3",  "R2",
                   "W6",  "Y6",  "U2",  "U1",  "T3",  "T2",  "W1",  "Y1",
                   "V3",  "V4",  "AA4", "AA5", "V1",  "V2",  "Y3",  "Y2",
                   "W4",  "W3",  "AA3", "AA2", "Y7",  "AA7", "AB2", "AB1")
    val dir2 = Seq("T9",  "AD5", "AD3", /* clk, rst, send */
                   "AB9", "AA8", "AB5", "AC4", "AC1", "AD1", "AB4", "AC3",
                   "W10", "Y10", "AB7", "AB6", "W8",  "Y8",  "Y12", "Y13",
                   "AA10","AA9", "W11", "Y11", "AC7", "AC6", "AA14","AA13",
                   "AB11","AB10","AB14","AC13","AC11","AC12","AB12","AA12")
    val dirB2C = Seq(IOPin(io.b2c.clk), IOPin(io.b2c.rst), IOPin(io.b2c.send)) ++
                 IOPin.of(io.b2c.data)
    val dirC2B = Seq(IOPin(io.c2b.clk), IOPin(io.c2b.rst), IOPin(io.c2b.send)) ++
                 IOPin.of(io.c2b.data)
    (dirB2C zip dir1) foreach { case (io, pin) => shell.io_pdc.addPin(io, pin) }
    (dirC2B zip dir2) foreach { case (io, pin) => shell.io_pdc.addPin(io, pin) }

    val (rxIn, _) = rxI.out(0)
    rxIn.reset := shell.pllReset
  } }
}

class VeraShell()(implicit p: Parameters) extends PolarFireShell
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val sys_clock = Overlay(ClockInputOverlayKey)(new SysClockVeraOverlay(_, _, _))
  val led       = Overlay(LEDOverlayKey)       (new LEDVeraOverlay     (_, _, _))
  val chiplink  = Overlay(ChipLinkOverlayKey)  (new ChipLinkVeraOverlay(_, _, _))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  override lazy val module = new LazyRawModuleImp(this) {
    val pf_user_reset_n = IO(Input(Bool()))
    io_pdc.addPin(pf_user_reset_n, "AK18")

    pllReset :=
      !pf_user_reset_n ||
      !initMonitor.io.DEVICE_INIT_DONE ||
      chiplink.map(!_.ereset_n).getOrElse(false.B)
  }
}
