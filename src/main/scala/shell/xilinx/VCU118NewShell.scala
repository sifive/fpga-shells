// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.experimental.{attach, Analog, IO, withClockAndReset}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.SyncResetSynchronizerShiftReg
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.devices.xilinx.xilinxvcu118mig._
import sifive.fpgashells.devices.xilinx.xdma._

class SysClockVCU118Overlay(val shell: VCU118Shell, val name: String, params: ClockInputOverlayParams)
  extends LVDSClockInputXilinxOverlay(params)
{
  val node = shell { ClockSourceNode(freqMHz = 250, jitterPS = 50)(ValName(name)) }

  shell { InModuleBody {
    shell.xdc.addPackagePin(io.p, "E12")
    shell.xdc.addPackagePin(io.n, "D12")
    shell.xdc.addIOStandard(io.p, "DIFF_SSTL12")
    shell.xdc.addIOStandard(io.n, "DIFF_SSTL12")
  } }
}

class SDIOVCU118Overlay(val shell: VCU118Shell, val name: String, params: SDIOOverlayParams)
  extends SDIOXilinxOverlay(params)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("AV15", IOPin(io.sdio_clk)),
                                        ("AY15", IOPin(io.sdio_cmd)),
                                        ("AW15", IOPin(io.sdio_dat_0)),
                                        ("AV16", IOPin(io.sdio_dat_1)),
                                        ("AU16", IOPin(io.sdio_dat_2)),
                                        ("AY14", IOPin(io.sdio_dat_3)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}

class UARTVCU118Overlay(val shell: VCU118Shell, val name: String, params: UARTOverlayParams)
  extends UARTXilinxOverlay(params)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("AY25", IOPin(io.ctsn)),
                                        ("BB22", IOPin(io.rtsn)),
                                        ("AW25", IOPin(io.rxd)),
                                        ("BB21", IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
  } }
}

class LEDVCU118Overlay(val shell: VCU118Shell, val name: String, params: LEDOverlayParams)
  extends LEDXilinxOverlay(params, packagePins = Seq("AT32", "AV34", "AY30", "BB32", "BF32", "AU37", "AV36", "BA37"))
{
  shell { InModuleBody {
    IOPin.of(io).foreach { shell.xdc.addIOStandard(_, "LVCMOS12") }
  } }
}

class SwitchVCU118Overlay(val shell: VCU118Shell, val name: String, params: SwitchOverlayParams)
  extends SwitchXilinxOverlay(params, packagePins = Seq("B17", "G16", "J16", "D21"))
{
  shell { InModuleBody {
    IOPin.of(io).foreach { shell.xdc.addIOStandard(_, "LVCMOS12") }
  } }
}

class ChipLinkVCU118Overlay(val shell: VCU118Shell, val name: String, params: ChipLinkOverlayParams)
  extends ChipLinkXilinxOverlay(params, rxPhase= -120, txPhase= -90, rxMargin=0.6, txMargin=0.5)
{
  val ereset_n = shell { InModuleBody {
    val ereset_n = IO(Analog(1.W))
    ereset_n.suggestName("ereset_n")
    val pin = IOPin(ereset_n, 0)
    shell.xdc.addPackagePin(pin, "BC8")
    shell.xdc.addIOStandard(pin, "LVCMOS18")
    shell.xdc.addTermination(pin, "NONE")
    shell.xdc.addPullup(pin)

    val iobuf = Module(new IOBUF)
    iobuf.suggestName("chiplink_ereset_iobuf")
    attach(ereset_n, iobuf.io.IO)
    iobuf.io.T := true.B // !oe
    iobuf.io.I := false.B

    iobuf.io.O
  } }

  shell { InModuleBody {
    val dir1 = Seq("BC9", "AV8", "AV9", /* clk, rst, send */
                   "AY9",  "BA9",  "BF10", "BF9",  "BC11", "BD11", "BD12", "BE12",
                   "BF12", "BF11", "BE14", "BF14", "BD13", "BE13", "BC15", "BD15",
                   "BE15", "BF15", "BA14", "BB14", "BB13", "BB12", "BA16", "BA15",
                   "BC14", "BC13", "AY8",  "AY7",  "AW8",  "AW7",  "BB16", "BC16")
    val dir2 = Seq("AV14", "AK13", "AK14", /* clk, rst, send */
                   "AR14", "AT14", "AP12", "AR12", "AW12", "AY12", "AW11", "AY10",
                   "AU11", "AV11", "AW13", "AY13", "AN16", "AP16", "AP13", "AR13",
                   "AT12", "AU12", "AK15", "AL15", "AL14", "AM14", "AV10", "AW10",
                   "AN15", "AP15", "AK12", "AL12", "AM13", "AM12", "AJ13", "AJ12")
    (IOPin.of(io.b2c) zip dir1) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
    (IOPin.of(io.c2b) zip dir2) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
  } }
}

// TODO: JTAG is untested
class JTAGDebugVCU118Overlay(val shell: VCU118Shell, val name: String, params: JTAGDebugOverlayParams)
  extends JTAGDebugXilinxOverlay(params)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))
    val packagePinsWithPackageIOs = Seq(("P29", IOPin(io.jtag_TCK)),
                                        ("L31", IOPin(io.jtag_TMS)),
                                        ("M31", IOPin(io.jtag_TDI)),
                                        ("R29", IOPin(io.jtag_TDO)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addPullup(io)
    } }
  } }
}

case object VCU118DDRSize extends Field[BigInt](0x40000000L * 2) // 2GB
class DDRVCU118Overlay(val shell: VCU118Shell, val name: String, params: DDROverlayParams)
  extends DDROverlay[XilinxVCU118MIGPads](params)
{
  val size = p(VCU118DDRSize)

  val migParams = XilinxVCU118MIGParams(address = AddressSet.misaligned(params.baseAddress, size))
  val mig = LazyModule(new XilinxVCU118MIG(migParams))
  val ioNode = BundleBridgeSource(() => mig.module.io.cloneType)
  val topIONode = shell { ioNode.makeSink() }
  val ddrUI     = shell { ClockSourceNode(freqMHz = 200) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := params.wrangler := ddrUI

  def designOutput = mig.node
  def ioFactory = new XilinxVCU118MIGPads(size)

  InModuleBody { ioNode.bundle <> mig.module.io }

  shell { InModuleBody {
    require (shell.sys_clock.isDefined, "Use of DDRVCU118Overlay depends on SysClockVCU118Overlay")
    val (sys, _) = shell.sys_clock.get.node.out(0)
    val (ui, _) = ddrUI.out(0)
    val (ar, _) = areset.in(0)
    val port = topIONode.bundle.port
    io <> port
    ui.clock := port.c0_ddr4_ui_clk
    ui.reset := /*!port.mmcm_locked ||*/ port.c0_ddr4_ui_clk_sync_rst
    port.c0_sys_clk_i := sys.clock.asUInt
    port.sys_rst := sys.reset // pllReset
    port.c0_ddr4_aresetn := !ar.reset

    val allddrpins = Seq(  "D14", "B15", "B16", "C14", "C15", "A13", "A14",
      "A15", "A16", "B12", "C12", "B13", "C13", "D15", "H14", "H15", "F15",
      "H13", "G15", "G13", "N20", "E13", "E14", "F14", "A10", "F13", "C8",
      "F11", "E11", "F10", "F9",  "H12", "G12", "E9",  "D9",  "R19", "P19",
      "M18", "M17", "N19", "N18", "N17", "M16", "L16", "K16", "L18", "K18",
      "J17", "H17", "H19", "H18", "F19", "F18", "E19", "E18", "G20", "F20",
      "E17", "D16", "D17", "C17", "C19", "C18", "D20", "D19", "C20", "B20",
      "N23", "M23", "R21", "P21", "R22", "P22", "T23", "R23", "K24", "J24",
      "M21", "L21", "K21", "J21", "K22", "J22", "H23", "H22", "E23", "E22",
      "F21", "E21", "F24", "F23", "D10", "P16", "J19", "E16", "A18", "M22",
      "L20", "G23", "D11", "P17", "K19", "F16", "A19", "N22", "M20", "H24",
      "G11", "R18", "K17", "G18", "B18", "P20", "L23", "G22")

    (IOPin.of(io) zip allddrpins) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
  } }

  shell.sdc.addGroup(pins = Seq(mig.island.module.blackbox.io.c0_ddr4_ui_clk))
}

class PCIeVCU118FMCOverlay(val shell: VCU118Shell, val name: String, params: PCIeOverlayParams)
  extends PCIeUltraScaleOverlay(XDMAParams(
    name     = "fmc_xdma",
    location = "X0Y3",
    bars     = params.bars,
    control  = params.ecam,
    lanes    = 4), params)
{
  shell { InModuleBody {
    // Work-around incorrectly pre-assigned pins
    IOPin.of(io).foreach { shell.xdc.addPackagePin(_, "") }

    // We need some way to connect both of these to reach x8
    val ref126 = Seq("V38",  "V39")  /* [pn] GBT0 Bank 126 */
    val ref121 = Seq("AK38", "AK39") /* [pn] GBT0 Bank 121 */
    val ref = ref126

    // Bank 126 (DP5, DP6, DP4, DP7), Bank 121 (DP3, DP2, DP1, DP0)
    val rxp = Seq("U45", "R45", "W45", "N45", "AJ45", "AL45", "AN45", "AR45") /* [0-7] */
    val rxn = Seq("U46", "R46", "W46", "N46", "AJ46", "AL46", "AN46", "AR46") /* [0-7] */
    val txp = Seq("P42", "M42", "T42", "K42", "AL40", "AM42", "AP42", "AT42") /* [0-7] */
    val txn = Seq("P43", "M43", "T43", "K43", "AL41", "AM43", "AP43", "AT43") /* [0-7] */

    def bind(io: Seq[IOPin], pad: Seq[String]) {
      (io zip pad) foreach { case (io, pad) => shell.xdc.addPackagePin(io, pad) }
    }

    bind(IOPin.of(io.refclk), ref)
    // We do these individually so that zip falls off the end of the lanes:
    bind(IOPin.of(io.lanes.pci_exp_txp), txp)
    bind(IOPin.of(io.lanes.pci_exp_txn), txn)
    bind(IOPin.of(io.lanes.pci_exp_rxp), rxp)
    bind(IOPin.of(io.lanes.pci_exp_rxn), rxn)
  } }
}

class PCIeVCU118EdgeOverlay(val shell: VCU118Shell, val name: String, params: PCIeOverlayParams)
  extends PCIeUltraScaleOverlay(XDMAParams(
    name     = "edge_xdma",
    location = "X1Y2",
    bars     = params.bars,
    control  = params.ecam,
    lanes    = 8), params)
{
  shell { InModuleBody {
    // Work-around incorrectly pre-assigned pins
    IOPin.of(io).foreach { shell.xdc.addPackagePin(_, "") }

    // PCIe Edge connector U2
    //   Lanes 00-03 Bank 227
    //   Lanes 04-07 Bank 226
    //   Lanes 08-11 Bank 225
    //   Lanes 12-15 Bank 224

    // FMC+ J22
    val ref227 = Seq("AC9", "AC8")  /* [pn]  Bank 227 PCIE_CLK2_*/
    val ref = ref227

    // PCIe Edge connector U2 : Bank 227, 226
    val rxp = Seq("AA4", "AB2", "AC4", "AD2", "AE4", "AF2", "AG4", "AH2") // [0-7]
    val rxn = Seq("AA3", "AB1", "AC3", "AD1", "AE3", "AF1", "AG3", "AH1") // [0-7]
    val txp = Seq("Y7", "AB7", "AD7", "AF7", "AH7", "AK7", "AM7", "AN5") // [0-7]
    val txn = Seq("Y6", "AB6", "AD6", "AF6", "AH6", "AK6", "AM6", "AN4") // [0-7]

    def bind(io: Seq[IOPin], pad: Seq[String]) {
      (io zip pad) foreach { case (io, pad) => shell.xdc.addPackagePin(io, pad) }
    }

    bind(IOPin.of(io.refclk), ref)
    // We do these individually so that zip falls off the end of the lanes:
    bind(IOPin.of(io.lanes.pci_exp_txp), txp)
    bind(IOPin.of(io.lanes.pci_exp_txn), txn)
    bind(IOPin.of(io.lanes.pci_exp_rxp), rxp)
    bind(IOPin.of(io.lanes.pci_exp_rxn), rxn)
  } }
}

class VCU118Shell()(implicit p: Parameters) extends UltraScaleShell
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  // Order matters; ddr depends on sys_clock
  val sys_clock = Overlay(ClockInputOverlayKey)(new SysClockVCU118Overlay (_, _, _))
  val led       = Overlay(LEDOverlayKey)       (new LEDVCU118Overlay      (_, _, _))
  val switch    = Overlay(SwitchOverlayKey)    (new SwitchVCU118Overlay   (_, _, _))
  val chiplink  = Overlay(ChipLinkOverlayKey)  (new ChipLinkVCU118Overlay (_, _, _))
  val ddr       = Overlay(DDROverlayKey)       (new DDRVCU118Overlay      (_, _, _))
  val fmc       = Overlay(PCIeOverlayKey)      (new PCIeVCU118FMCOverlay  (_, _, _))
  val edge      = Overlay(PCIeOverlayKey)      (new PCIeVCU118EdgeOverlay (_, _, _))
  val uart      = Overlay(UARTOverlayKey)      (new UARTVCU118Overlay     (_, _, _))
  val sdio      = Overlay(SDIOOverlayKey)      (new SDIOVCU118Overlay     (_, _, _))
  val jtag      = Overlay(JTAGDebugOverlayKey) (new JTAGDebugVCU118Overlay(_, _, _))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  p(ClockInputOverlayKey).foreach(_(ClockInputOverlayParams()))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addPackagePin(reset, "L19")
    xdc.addIOStandard(reset, "LVCMOS12")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    val powerOnReset = PowerOnResetFPGAOnly(sys_clock.get.clock)
    sdc.addAsyncPath(Seq(powerOnReset))

    pllReset :=
      reset_ibuf.io.O || powerOnReset ||
      chiplink.map(!_.ereset_n).getOrElse(false.B)
  }
}
