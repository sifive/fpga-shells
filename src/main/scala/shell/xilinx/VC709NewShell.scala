// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.experimental.{attach, IO, withClockAndReset}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.SyncResetSynchronizerShiftReg
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.devices.xilinx.xilinxvc709mig._
import sifive.fpgashells.devices.xilinx.xilinxvc709pciex1._

class SysClockVC709PlacedOverlay(val shell: VC709ShellBasicOverlays, name: String, val designInput: ClockInputDesignInput, val shellInput: ClockInputShellInput)
  extends LVDSClockInputXilinxPlacedOverlay(name, designInput, shellInput)
{
  val node = shell { ClockSourceNode(freqMHz = 200, jitterPS = 50)(ValName(name)) }

  shell { InModuleBody {
    // sysclk_p, sysclk_n
//    val packagePinsWithPackageIOs = Seq(("H19", IOPin(io.p)),
//                                        ("G18", IOPin(io.n)))

//    packagePinsWithPackageIOs foreach { case (pin, io) => {
//      shell.xdc.addPackagePin(io, pin)
//      shell.xdc.addIOStandard(io, "DIFF_SSTL15")
//      shell.xdc.addIOB(io)
//    } }

    shell.xdc.addBoardPin(io.p, "clk_p")
    shell.xdc.addBoardPin(io.n, "clk_n")
  } }
}

class SysClockVC709ShellPlacer(shell: VC709ShellBasicOverlays, val shellInput: ClockInputShellInput)(implicit val valName: ValName)
  extends ClockInputShellPlacer[VC709ShellBasicOverlays]
{
    def place(designInput: ClockInputDesignInput) = new SysClockVC709PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class UARTVC709PlacedOverlay(val shell: VC709ShellBasicOverlays, name: String, val designInput: UARTDesignInput, val shellInput: UARTShellInput)
  extends UARTXilinxPlacedOverlay(name, designInput, shellInput, true)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("AR34", IOPin(io.rtsn.get)),
                                        ("AT32", IOPin(io.ctsn.get)),
                                        ("AU36", IOPin(io.txd)),
                                        ("AU33", IOPin(io.rxd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
  } }
}
class UARTVC709ShellPlacer(val shell: VC709ShellBasicOverlays, val shellInput: UARTShellInput)(implicit val valName: ValName)
  extends UARTShellPlacer[VC709ShellBasicOverlays] {
  def place(designInput: UARTDesignInput) = new UARTVC709PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class LEDVC709PlacedOverlay(val shell: VC709ShellBasicOverlays, name: String, val designInput: LEDDesignInput, val shellInput: LEDShellInput)
  extends LEDXilinxPlacedOverlay(name, designInput, shellInput, boardPin = Some(s"leds_8bits_tri_o_${shellInput.number}"))
class LEDVC709ShellPlacer(val shell: VC709ShellBasicOverlays, val shellInput: LEDShellInput)(implicit val valName: ValName)
  extends LEDShellPlacer[VC709ShellBasicOverlays] {
  def place(designInput: LEDDesignInput) = new LEDVC709PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class SwitchVC709PlacedOverlay(val shell: VC709ShellBasicOverlays, name: String, val designInput: SwitchDesignInput, val shellInput: SwitchShellInput)
  extends SwitchXilinxPlacedOverlay(name, designInput, shellInput, boardPin = Some(s"dip_switches_tri_i_${shellInput.number}"))
class SwitchVC709ShellPlacer(val shell: VC709ShellBasicOverlays, val shellInput: SwitchShellInput)(implicit val valName: ValName)
  extends SwitchShellPlacer[VC709ShellBasicOverlays] {
  def place(designInput: SwitchDesignInput) = new SwitchVC709PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class ButtonVC709PlacedOverlay(val shell: VC709ShellBasicOverlays, name: String, val designInput: ButtonDesignInput, val shellInput: ButtonShellInput)
  extends ButtonXilinxPlacedOverlay(name, designInput, shellInput, boardPin = Some(s"push_buttons_5bits_tri_i_${shellInput.number}"))
class ButtonVC709ShellPlacer(val shell: VC709ShellBasicOverlays, val shellInput: ButtonShellInput)(implicit val valName: ValName)
  extends ButtonShellPlacer[VC709ShellBasicOverlays] {
  def place(designInput: ButtonDesignInput) = new ButtonVC709PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class ChipLinkVC709PlacedOverlay(val shell: VC709ShellBasicOverlays, name: String, val designInput: ChipLinkDesignInput, val shellInput: ChipLinkShellInput)
  extends ChipLinkXilinxPlacedOverlay(name, designInput, shellInput, rxPhase=280, txPhase=220, rxMargin=0.3, txMargin=0.3)
{
  val ereset_n = shell { InModuleBody {
    val ereset_n = IO(Input(Bool()))
    ereset_n.suggestName("ereset_n")
    shell.xdc.addPackagePin(ereset_n, "AF40")
    shell.xdc.addIOStandard(ereset_n, "LVCMOS18")
    shell.xdc.addTermination(ereset_n, "NONE")
    ereset_n
  } }

  shell { InModuleBody {
    val dir1 = Seq("AF39", "AJ41", "AJ40", /* clk, rst, send */
                   "AD40", "AD41", "AF41", "AG41", "AK39", "AL39", "AJ42", "AK42",
                   "AL41", "AL42", "AF42", "AG42", "AD38", "AE38", "AC40", "AC41",
                   "AD42", "AE42", "AJ38", "AK38", "AB41", "AB42", "Y42",  "AA42",
                   "Y39",  "AA39", "W40",  "Y40",  "AB38", "AB39", "AC38", "AC39")
    val dir2 = Seq("U39", "R37", "T36", /* clk, rst, send */
                   "U37", "U38", "U36", "T37", "U32", "U33", "V33", "V34",
                   "P35", "P36", "W32", "W33", "R38", "R39", "U34", "T35",
                   "R33", "R34", "N33", "N34", "P32", "P33", "V35", "V36",
                   "W36", "W37", "T32", "R32", "V39", "V40", "P37", "P38")
    (IOPin.of(io.b2c) zip dir1) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
    (IOPin.of(io.c2b) zip dir2) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
  } }
}
class ChipLinkVC709ShellPlacer(val shell: VC709ShellBasicOverlays, val shellInput: ChipLinkShellInput)(implicit val valName: ValName)
  extends ChipLinkShellPlacer[VC709ShellBasicOverlays] {
  def place(designInput: ChipLinkDesignInput) = new ChipLinkVC709PlacedOverlay(shell, valName.name, designInput, shellInput)
}

// TODO: JTAG is untested
class JTAGDebugVC709PlacedOverlay(val shell: VC709ShellBasicOverlays, name: String, val designInput: JTAGDebugDesignInput, val shellInput: JTAGDebugShellInput)
  extends JTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))
/* if old method
    val packagePinsWithPackageIOs = Seq(("R32", IOPin(io.jtag_TCK)),
                                        ("W36", IOPin(io.jtag_TMS)),
                                        ("W37", IOPin(io.jtag_TDI)),
                                        ("V40", IOPin(io.jtag_TDO)))
*/
    /*
           #Olimex Pin  Olimex Function LCD Pin LCD Function FPGA Pin
           #1           VREF            14      5V
           #3           TTRST_N         1       LCD_DB7       AN40
           #5           TTDI            2       LCD_DB6       AR39
           #7           TTMS            3       LCD_DB5       AR38
           #9           TTCK            4       LCD_DB4       AT42
           #11          TRTCK           NC      NC            NC
           #13          TTDO            9       LCD_E         AT40
           #15          TSRST_N         10      LCD_RW        AR42
           #2           VREF            14      5V
           #18          GND             13      GND
     */
    val packagePinsWithPackageIOs = Seq(("AT42", IOPin(io.jtag_TCK)),
                                        ("AR38", IOPin(io.jtag_TMS)),
                                        ("AR39", IOPin(io.jtag_TDI)),
                                        ("AR42", IOPin(io.srst_n)),
                                        ("AT40", IOPin(io.jtag_TDO)))
    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addPullup(io)
    } }
  } }
}
class JTAGDebugVC709ShellPlacer(val shell: VC709ShellBasicOverlays, val shellInput: JTAGDebugShellInput)(implicit val valName: ValName)
  extends JTAGDebugShellPlacer[VC709ShellBasicOverlays] {
  def place(designInput: JTAGDebugDesignInput) = new JTAGDebugVC709PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class DDR3VC709PlacedOverlay(val shell: VC709ShellBasicOverlays, name: String, val designInput: DDRDesignInput, val shellInput: DDRShellInput)
  extends DDR3XilinxPlacedOverlay(shell, name, designInput, shellInput)
{
  shell { InModuleBody {
    // The pins for DDR3 on vc709 board are emitted in the following order:
    // addr[0->15], ba[0-2], ras_n, cas_n, we_n, reset_n, ck_p, ck_n, cke, cs_n, odt, dm[0->7], dq[0->63], dqs_n[0->7], dqs_p[0->7]
    val allddrpins = Seq(
      "A20", "B19", "C20", "A19", "A17", "A16", "D20", "C18", "D17", "C19", "B21", "B17", "A15", "A21", "F17", "E17", // addr[0->15]
      "D21", "C21", "D18", // ba[0->2]
      "E20", "K17", "F20", "P18", "E19", "E18", "K19", "J17", "H20", // ras_n, cas_n, we_n, reset_n, ck_p, ck_n, cke, cs_n, odt
      "M13", "K15", "F12", "A14", "C23", "D25", "C31", "F31", // dm [0->7]
      "N14", "N13", "L14", "M14", "M12", "N15", "M11", "L12", "K14", "K13", "H13", "J13", "L16", "L15", "H14", "J15", // dq[0->15]
      "E15", "E13", "F15", "E14", "G13", "G12", "F14", "G14", "B14", "C13", "B16", "D15", "D13", "E12", "C16", "D16", // dq[16->31]
      "A24", "B23", "B27", "B26", "A22", "B22", "A25", "C24", "E24", "D23", "D26", "C25", "E23", "D22", "F22", "E22", // dq[32->47]
      "A30", "D27", "A29", "C28", "D28", "B31", "A31", "A32", "E30", "F29", "F30", "F27", "C30", "E29", "F26", "D30", // dq[48->63]
      "M16", "J12", "G16", "C14", "A27", "E25", "B29", "E28", // dqs_n[0->7]
      "N16", "K12", "H16", "C15", "A26", "F25", "B28", "E27") // dqs_p[0->7]

    IOPin.of(io).foreach { shell.xdc.addPackagePin(_, "") }
    (IOPin.of(io) zip allddrpins) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
  } }

  shell.sdc.addGroup(clocks = Seq("ui_clk0"), pins = Seq(mig.island.module.blackbox.io.ui_clk))
}
class DDR3VC709ShellPlacer(shell: VC709ShellBasicOverlays, val shellInput: DDRShellInput)(implicit val valName: ValName)
  extends DDRShellPlacer[VC709ShellBasicOverlays] {
  def place(designInput: DDRDesignInput) = new DDR3VC709PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class PCIeVC709PlacedOverlay(val shell: VC709ShellBasicOverlays, name: String, val designInput: PCIeDesignInput, val shellInput: PCIeShellInput)
  extends PCIePlacedOverlay[XilinxVC709PCIeX1Pads](name, designInput, shellInput)
{
  val pcie      = LazyModule(new XilinxVC709PCIeX1)
  val bridge    = BundleBridgeSource(() => pcie.module.io.cloneType)
  val topBridge = shell { bridge.makeSink() }
  val axiClk    = shell { ClockSourceNode(freqMHz = 125) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := designInput.wrangler := axiClk

  val slaveSide = TLIdentityNode()
  pcie.crossTLIn(pcie.slave)   := slaveSide
  pcie.crossTLIn(pcie.control) := slaveSide
  val node = NodeHandle(slaveSide, pcie.crossTLOut(pcie.master))
  val intNode = pcie.crossIntOut(pcie.intnode)

  def overlayOutput = PCIeOverlayOutput(node, intNode)
  def ioFactory = new XilinxVC709PCIeX1Pads

  InModuleBody { bridge.bundle <> pcie.module.io }

  shell { InModuleBody {
    val (axi, _) = axiClk.out(0)
    val (ar, _) = areset.in(0)
    val port = topBridge.bundle.port
    io <> port
    axi.clock := port.axi_aclk             // port.axi_aclk_out is changed to port.axi_aclk in 3.0
    // axi.reset := !port.mmcm_lock        // mmcm_lock is removed in 3.0
    port.axi_aresetn := !ar.reset
    port.axi_ctl_aresetn := !ar.reset

    def bind(io: Seq[IOPin], pad: Seq[String]) {
      (io zip pad) foreach { case (io, pad) => shell.xdc.addPackagePin(io, pad) }
    }

    val txn = Seq("W1", "AA1", "AC1", "AE1", "AG1", "AH3", "AJ1", "AK3") /* [0-7] */
    val txp = Seq("W2", "AA2", "AC2", "AE2", "AG2", "AH4", "AJ2", "AK4") /* [0-7] */
    val rxn = Seq("Y3", "AA5", "AB3", "AC5", "AD3", "AE5", "AF3", "AG5") /* [0-7] */
    val rxp = Seq("Y4", "AA6", "AB4", "AC6", "AD4", "AE6", "AF4", "AG6") /* [0-7] */
    
    IOPin.of(io).foreach { shell.xdc.addPackagePin(_, "") }
    bind(IOPin.of(io.REFCLK_rxp), Seq("AB8"))
    bind(IOPin.of(io.REFCLK_rxn), Seq("AB7"))
    bind(IOPin.of(io.pci_exp_rxp), rxp)
    bind(IOPin.of(io.pci_exp_rxn), rxn)
    bind(IOPin.of(io.pci_exp_txp), txp)
    bind(IOPin.of(io.pci_exp_txn), txn)

    shell.sdc.addClock(s"${name}_ref_clk", io.REFCLK_rxp, 100)
  } }

  shell.sdc.addGroup(clocks = Seq("txoutclk", "userclk1"))
}
class PCIeVC709ShellPlacer(val shell: VC709ShellBasicOverlays, val shellInput: PCIeShellInput)(implicit val valName: ValName)
  extends PCIeShellPlacer[VC709ShellBasicOverlays] {
  def place(designInput: PCIeDesignInput) = new PCIeVC709PlacedOverlay(shell, valName.name, designInput, shellInput)
}

abstract class VC709ShellBasicOverlays()(implicit p: Parameters) extends Series7Shell
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  // Order matters; ddr depends on sys_clock
  val sys_clock = Overlay(ClockInputOverlayKey, new SysClockVC709ShellPlacer(this, ClockInputShellInput()))
  // val led       = Seq.tabulate(8)(i => Overlay(LEDOverlayKey, new LEDVC709ShellPlacer(this, LEDShellInput(color = "red", number = i))(valName = ValName(s"led_$i"))))
  // val switch    = Seq.tabulate(8)(i => Overlay(SwitchOverlayKey, new SwitchVC709ShellPlacer(this, SwitchShellInput(number = i))(valName = ValName(s"switch_$i"))))
  // val button    = Seq.tabulate(5)(i => Overlay(ButtonOverlayKey, new ButtonVC709ShellPlacer(this, ButtonShellInput(number = i))(valName = ValName(s"button_$i"))))
  val uart      = Seq.tabulate(1)(i => Overlay(UARTOverlayKey, new UARTVC709ShellPlacer(this, UARTShellInput(index = 0))))
  val jtag      = Overlay(JTAGDebugOverlayKey, new JTAGDebugVC709ShellPlacer(this, JTAGDebugShellInput()))
  val chiplink  = Overlay(ChipLinkOverlayKey, new ChipLinkVC709ShellPlacer(this, ChipLinkShellInput())) 
  val ddr0      = Overlay(DDROverlayKey, new DDR3VC709ShellPlacer(this, DDRShellInput()))
  val pcie      = Overlay(PCIeOverlayKey, new PCIeVC709ShellPlacer(this, PCIeShellInput()))
}

class VC709BaseShell()(implicit p: Parameters) extends VC709ShellBasicOverlays
{
  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  p(ClockInputOverlayKey).foreach(_.place(ClockInputDesignInput()))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addBoardPin(reset, "reset")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    val sysclk: Clock = sys_clock.get() match {
      case Some(x: SysClockVC709PlacedOverlay) => x.clock
    }
    val powerOnReset = PowerOnResetFPGAOnly(sysclk)
    sdc.addAsyncPath(Seq(powerOnReset))

    val ereset: Bool = chiplink.get() match {
      case Some(x: ChipLinkVC709PlacedOverlay) => !x.ereset_n
      case _ => false.B
    }
    pllReset :=
      reset_ibuf.io.O || powerOnReset || ereset
  }
}