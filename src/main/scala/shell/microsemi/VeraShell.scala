// See LICENSE for license details.
package sifive.fpgashells.shell.microsemi

import chisel3._
import chisel3.experimental.{withClockAndReset,IO}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
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
  extends ChipLinkPolarFireOverlay(params)
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
    (IOPin.of(io.b2c) zip dir1) foreach { case (io, pin) => shell.io_pdc.addPin(io, pin) }
    (IOPin.of(io.c2b) zip dir2) foreach { case (io, pin) => shell.io_pdc.addPin(io, pin) }

    val (rx, _) = rxI.out(0)
    rx.reset := shell.pllReset
  } }
}

class PCIeVeraOverlay(val shell: VeraShell, val name: String, params: PCIeOverlayParams)
  extends PCIeOverlay[PolarFirePCIeX4Pads](params)
{
  val pcie = LazyModule(new PolarFireEvalKitPCIeX4)
  val ioSource = BundleBridgeSource(() => pcie.module.io.cloneType)
  val ioSink = shell { ioSource.sink }

  val slaveSide = TLIdentityNode()
  pcie.slave := pcie.crossTLIn := slaveSide
  pcie.control := pcie.crossTLIn := slaveSide
  val node = NodeHandle(slaveSide, pcie.crossTLOut := pcie.master)
  val intnode = pcie.crossIntOut := pcie.intnode

  def designOutput = (node, intnode)
  def ioFactory = new PolarFirePCIeX4Pads

  InModuleBody {
    val pcie_io = pcie.module.io
    val (source, _) = ioSource.out(0)

    source <> pcie_io
    pcie_io.extra.APB_S_PCLK     := Module.clock
    pcie_io.extra.APB_S_PRESET_N := !Module.reset.asUInt
    pcie_io.extra.AXI_CLK        := Module.clock
    pcie_io.extra.AXI_CLK_STABLE := Module.reset // !!! hart_clk_lock
  }

  shell { InModuleBody {
    def mIO(x: Bool, pin: String)(implicit valName: ValName): Bool = {
      val z = IO(x)
      z.suggestName(valName.name)
      shell.io_pdc.addPin(z, pin)
      z
    }

    // PCIe reference clock
    val ref_clk_pad_p   = mIO(Input(Bool()), "W27")
    val ref_clk_pad_n   = mIO(Input(Bool()), "W28")
    // PCIe switch reset
    val pf_rstb         = mIO(Output(Bool()), "AG15")
    // PCIe slots reset signals
//    val perst_x1_slot   = mIO(Output(Bool()), "B4")
//    val perst_x16_slot  = mIO(Output(Bool()), "A4")
//    val perst_m2_slot   = mIO(Output(Bool()), "B5")
//    val perst_sata_slot = mIO(Output(Bool()), "A5")

    val pciePins = Seq("V34", "V33", "Y34", "Y33", "AA32", "AA31", "AB34", "AB33", /* TXD[0-3]_[NP] */
                       "V30", "V29", "W32", "W31", "Y30",  "Y29",  "AB30", "AB29") /* RXD[0-3]_[NP] */
    (IOPin.of(io) zip pciePins) foreach { case (io, pin) => shell.io_pdc.addPin(io, pin) }

    // Most of this should move inside PolarFireEvalKitPCIeX4...
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

    val pf_oscillator = Module(new PolarFireOscillator)
    val pf_clk_divider = Module(new PolarFireClockDivider)
    val pf_glitchless_mux = Module(new PolarFireGlitchlessMux)
    pf_clk_divider.io.CLK_IN  := pf_oscillator.io.RCOSC_160MHZ_GL
    pf_glitchless_mux.io.CLK0 := pf_clk_divider.io.CLK_OUT
    pf_glitchless_mux.io.CLK1 := pf_tx_pll.io.CLK_125
    pf_glitchless_mux.io.SEL  := shell.initMonitor.io.PCIE_INIT_DONE
    val pcie_tl_clk = pf_glitchless_mux.io.CLK_OUT

    val pcie_io = ioSink.io
    val extra = pcie_io.extra
    io <> pcie_io.pads

    extra.PCIE_1_TL_CLK_125MHz       := pcie_tl_clk
    extra.PCIE_1_TX_PLL_REF_CLK      := pf_tx_pll_refclk_to_lane
    extra.PCIE_1_TX_BIT_CLK          := pf_tx_pll_bitclk
    extra.PCIE_1_TX_PLL_LOCK         := pf_tx_pll_lock
    extra.PCIESS_LANE0_CDR_REF_CLK_0 := pcie_refclk
    extra.PCIESS_LANE1_CDR_REF_CLK_0 := pcie_refclk
    extra.PCIESS_LANE2_CDR_REF_CLK_0 := pcie_refclk
    extra.PCIESS_LANE3_CDR_REF_CLK_0 := pcie_refclk

    withClockAndReset(pcie_fab_ref_clk, ResetCatchAndSync(pcie_fab_ref_clk, shell.initMonitor.io.PCIE_INIT_DONE)) {
      val timer = RegInit(268435456.U(29.W)) // ??? secret sauce
      val notDone = timer.orR
      timer := timer - notDone

      // PCIe slots reset
      pf_rstb         := !notDone
//      perst_x1_slot   := !notDone
//      perst_x16_slot  := !notDone
//      perst_m2_slot   := !notDone
//      perst_sata_slot := !notDone
    }
  } }
}

class VeraShell()(implicit p: Parameters) extends PolarFireShell
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val sys_clock = Overlay(ClockInputOverlayKey)(new SysClockVeraOverlay(_, _, _))
  val led       = Overlay(LEDOverlayKey)       (new LEDVeraOverlay     (_, _, _))
  val chiplink  = Overlay(ChipLinkOverlayKey)  (new ChipLinkVeraOverlay(_, _, _))
  val pcie      = Overlay(PCIeOverlayKey)      (new PCIeVeraOverlay    (_, _, _))

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
