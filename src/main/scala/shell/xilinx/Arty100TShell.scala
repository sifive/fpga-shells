// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.SyncResetSynchronizerShiftReg
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.devices.xilinx.xilinxarty100tmig._

class SysClockArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: ClockInputDesignInput, val shellInput: ClockInputShellInput)
  extends SingleEndedClockInputXilinxPlacedOverlay(name, designInput, shellInput)
{
  val node = shell { ClockSourceNode(freqMHz = 100, jitterPS = 50) }

  shell { InModuleBody {
    val clk: Clock = io
    shell.xdc.addPackagePin(clk, "E3")
    shell.xdc.addIOStandard(clk, "LVCMOS33")
  } }
}
class SysClockArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: ClockInputShellInput)(implicit val valName: ValName)
  extends ClockInputShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: ClockInputDesignInput) = new SysClockArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//PMOD JA used for SDIO
class SDIOArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: SPIDesignInput, val shellInput: SPIShellInput)
  extends SDIOXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("D12", IOPin(io.spi_clk)),
      ("B11", IOPin(io.spi_cs)),
      ("A11", IOPin(io.spi_dat(0))),
      ("D13", IOPin(io.spi_dat(1))),
      ("B18", IOPin(io.spi_dat(2))),
      ("G13", IOPin(io.spi_dat(3))))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addIOB(io)
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}
class SDIOArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: SPIShellInput)(implicit val valName: ValName)
  extends SPIShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: SPIDesignInput) = new SDIOArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class SPIFlashArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: SPIFlashDesignInput, val shellInput: SPIFlashShellInput)
  extends SPIFlashXilinxPlacedOverlay(name, designInput, shellInput)
{

  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("L16", IOPin(io.qspi_sck)),
      ("L13", IOPin(io.qspi_cs)),
      ("K17", IOPin(io.qspi_dq(0))),
      ("K18", IOPin(io.qspi_dq(1))),
      ("L14", IOPin(io.qspi_dq(2))),
      ("M14", IOPin(io.qspi_dq(3))))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}
class SPIFlashArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: SPIFlashShellInput)(implicit val valName: ValName)
  extends SPIFlashShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: SPIFlashDesignInput) = new SPIFlashArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class TracePMODArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: TracePMODDesignInput, val shellInput: TracePMODShellInput)
  extends TracePMODXilinxPlacedOverlay(name, designInput, shellInput, packagePins = Seq("U12", "V12", "V10", "V11", "U14", "V14", "T13", "U13"))
class TracePMODArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: TracePMODShellInput)(implicit val valName: ValName)
  extends TracePMODShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: TracePMODDesignInput) = new TracePMODArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class GPIOPMODArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: GPIOPMODDesignInput, val shellInput: GPIOPMODShellInput)
  extends GPIOPMODXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("E15", IOPin(io.gpio_pmod_0)), //These are PMOD B
      ("E16", IOPin(io.gpio_pmod_1)),
      ("D15", IOPin(io.gpio_pmod_2)),
      ("C15", IOPin(io.gpio_pmod_3)),
      ("J17", IOPin(io.gpio_pmod_4)),
      ("J18", IOPin(io.gpio_pmod_5)),
      ("K15", IOPin(io.gpio_pmod_6)),
      ("J15", IOPin(io.gpio_pmod_7)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
    } }
    packagePinsWithPackageIOs drop 7 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}
class GPIOPMODArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: GPIOPMODShellInput)(implicit val valName: ValName)
  extends GPIOPMODShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: GPIOPMODDesignInput) = new GPIOPMODArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class UARTArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: UARTDesignInput, val shellInput: UARTShellInput)
  extends UARTXilinxPlacedOverlay(name, designInput, shellInput, false)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("A9", IOPin(io.rxd)),
      ("D10", IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addIOB(io)
    } }
  } }
}
class UARTArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: UARTShellInput)(implicit val valName: ValName)
  extends UARTShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: UARTDesignInput) = new UARTArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//LEDS - r0, g0, b0, r1, g1, b1 ..., 4 normal leds_
object LEDArtyPinConstraints{
  val pins = Seq("G6", "F6", "E1", "G3", "J4", "G4", "J3", "J2", "H4", "K1", "H6", "K2", "H5", "J5", "T9", "T10")
}
class LEDArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: LEDDesignInput, val shellInput: LEDShellInput)
  extends LEDXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(LEDArtyPinConstraints.pins(shellInput.number)))
class LEDArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: LEDShellInput)(implicit val valName: ValName)
  extends LEDShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: LEDDesignInput) = new LEDArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//SWs
object SwitchArtyPinConstraints{
  val pins = Seq("A8", "C11", "C10", "A10")
}
class SwitchArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: SwitchDesignInput, val shellInput: SwitchShellInput)
  extends SwitchXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(SwitchArtyPinConstraints.pins(shellInput.number)))
class SwitchArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: SwitchShellInput)(implicit val valName: ValName)
  extends SwitchShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: SwitchDesignInput) = new SwitchArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//Buttons
object ButtonArtyPinConstraints {
  val pins = Seq("D9", "C9", "B9", "B8")
}
class ButtonArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: ButtonDesignInput, val shellInput: ButtonShellInput)
  extends ButtonXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(ButtonArtyPinConstraints.pins(shellInput.number)))
class ButtonArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: ButtonShellInput)(implicit val valName: ValName)
  extends ButtonShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: ButtonDesignInput) = new ButtonArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class JTAGDebugBScanArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: JTAGDebugBScanDesignInput, val shellInput: JTAGDebugBScanShellInput)
 extends JTAGDebugBScanXilinxPlacedOverlay(name, designInput, shellInput)
class JTAGDebugBScanArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: JTAGDebugBScanShellInput)(implicit val valName: ValName)
  extends JTAGDebugBScanShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: JTAGDebugBScanDesignInput) = new JTAGDebugBScanArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// PMOD JD used for JTAG
class JTAGDebugArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: JTAGDebugDesignInput, val shellInput: JTAGDebugShellInput)
  extends JTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))
    val packagePinsWithPackageIOs = Seq(("F4", IOPin(io.jtag_TCK)),  //pin JD-3
      ("D2", IOPin(io.jtag_TMS)),  //pin JD-8
      ("E2", IOPin(io.jtag_TDI)),  //pin JD-7
      ("D4", IOPin(io.jtag_TDO)),  //pin JD-1
      ("H2", IOPin(io.srst_n)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addPullup(io)
    } }
  } }
}
class JTAGDebugArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: JTAGDebugShellInput)(implicit val valName: ValName)
  extends JTAGDebugShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: JTAGDebugDesignInput) = new JTAGDebugArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//cjtag
class cJTAGDebugArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: cJTAGDebugDesignInput, val shellInput: cJTAGDebugShellInput)
  extends cJTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCKC", IOPin(io.cjtag_TCKC), 10)
    shell.sdc.addGroup(clocks = Seq("JTCKC"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.cjtag_TCKC))
    val packagePinsWithPackageIOs = Seq(("F4", IOPin(io.cjtag_TCKC)),  //pin JD-3
      ("D2", IOPin(io.cjtag_TMSC)),  //pin JD-8
      ("H2", IOPin(io.srst_n)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
    } }
      shell.xdc.addPullup(IOPin(io.cjtag_TCKC))
      shell.xdc.addPullup(IOPin(io.srst_n))
  } }
}
class cJTAGDebugArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: cJTAGDebugShellInput)(implicit val valName: ValName)
  extends cJTAGDebugShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: cJTAGDebugDesignInput) = new cJTAGDebugArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

case object ArtyDDRSize extends Field[BigInt](0x10000000L * 1) // 256 MB
class DDRArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: DDRDesignInput, val shellInput: DDRShellInput)
  extends DDRPlacedOverlay[XilinxArty100TMIGPads](name, designInput, shellInput)
{
  val size = p(ArtyDDRSize)

  val ddrClk1 = shell { ClockSinkNode(freqMHz = 166.666)}
  val ddrClk2 = shell { ClockSinkNode(freqMHz = 200)}
  val ddrGroup = shell { ClockGroup() }
  ddrClk1 := di.wrangler := ddrGroup := di.corePLL
  ddrClk2 := di.wrangler := ddrGroup
  
  val migParams = XilinxArty100TMIGParams(address = AddressSet.misaligned(di.baseAddress, size))
  val mig = LazyModule(new XilinxArty100TMIG(migParams))
  val ioNode = BundleBridgeSource(() => mig.module.io.cloneType)
  val topIONode = shell { ioNode.makeSink() }
  val ddrUI     = shell { ClockSourceNode(freqMHz = 100) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := di.wrangler := ddrUI

  def overlayOutput = DDROverlayOutput(ddr = mig.node)
  def ioFactory = new XilinxArty100TMIGPads(size)

  InModuleBody { ioNode.bundle <> mig.module.io }

  shell { InModuleBody {
    require (shell.sys_clock.get.isDefined, "Use of DDRArtyPlacedOverlay depends on SysClockArtyPlacedOverlay")
    val (sys, _) = shell.sys_clock.get.get.overlayOutput.node.out(0)
    val (ui, _) = ddrUI.out(0)
    val (dclk1, _) = ddrClk1.in(0)
    val (dclk2, _) = ddrClk2.in(0)
    val (ar, _) = areset.in(0)
    val port = topIONode.bundle.port
    
    io <> port
    ui.clock := port.ui_clk
    ui.reset := !port.mmcm_locked || port.ui_clk_sync_rst
    port.sys_clk_i := dclk1.clock.asUInt
    port.clk_ref_i := dclk2.clock.asUInt
    port.sys_rst := shell.pllReset
    port.aresetn := !ar.reset
  } }

  shell.sdc.addGroup(clocks = Seq("clk_pll_i"), pins = Seq(mig.island.module.blackbox.io.ui_clk))
}
class DDRArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: DDRShellInput)(implicit val valName: ValName)
  extends DDRShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: DDRDesignInput) = new DDRArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//Core to shell external resets
class CTSResetArtyPlacedOverlay(val shell: Arty100TShellBasicOverlays, name: String, val designInput: CTSResetDesignInput, val shellInput: CTSResetShellInput)
  extends CTSResetPlacedOverlay(name, designInput, shellInput)
class CTSResetArtyShellPlacer(val shell: Arty100TShellBasicOverlays, val shellInput: CTSResetShellInput)(implicit val valName: ValName)
  extends CTSResetShellPlacer[Arty100TShellBasicOverlays] {
  def place(designInput: CTSResetDesignInput) = new CTSResetArtyPlacedOverlay(shell, valName.name, designInput, shellInput)
}


abstract class Arty100TShellBasicOverlays()(implicit p: Parameters) extends Series7Shell {
  // Order matters; ddr depends on sys_clock
  val sys_clock = Overlay(ClockInputOverlayKey, new SysClockArtyShellPlacer(this, ClockInputShellInput()))
  val led       = Seq.tabulate(16)(i => Overlay(LEDOverlayKey, new LEDArtyShellPlacer(this, LEDMetas(i))(valName = ValName(s"led_$i"))))
  val switch    = Seq.tabulate(4)(i => Overlay(SwitchOverlayKey, new SwitchArtyShellPlacer(this, SwitchShellInput(number = i))(valName = ValName(s"switch_$i"))))
  val button    = Seq.tabulate(4)(i => Overlay(ButtonOverlayKey, new ButtonArtyShellPlacer(this, ButtonShellInput(number = i))(valName = ValName(s"button_$i"))))
  val ddr       = Overlay(DDROverlayKey, new DDRArtyShellPlacer(this, DDRShellInput()))
  val uart      = Overlay(UARTOverlayKey, new UARTArtyShellPlacer(this, UARTShellInput()))
  val sdio      = Overlay(SPIOverlayKey, new SDIOArtyShellPlacer(this, SPIShellInput()))
  val jtag      = Overlay(JTAGDebugOverlayKey, new JTAGDebugArtyShellPlacer(this, JTAGDebugShellInput()))
  val cjtag     = Overlay(cJTAGDebugOverlayKey, new cJTAGDebugArtyShellPlacer(this, cJTAGDebugShellInput()))
  val spi_flash = Overlay(SPIFlashOverlayKey, new SPIFlashArtyShellPlacer(this, SPIFlashShellInput()))
  val cts_reset = Overlay(CTSResetOverlayKey, new CTSResetArtyShellPlacer(this, CTSResetShellInput()))
  val jtagBScan = Overlay(JTAGDebugBScanOverlayKey, new JTAGDebugBScanArtyShellPlacer(this, JTAGDebugBScanShellInput()))

  def LEDMetas(i: Int): LEDShellInput =
    LEDShellInput(
      color = if((i < 12) && (i % 3 == 1)) "green" else if((i < 12) && (i % 3 == 2)) "blue" else "red",
      rgb = (i < 12),
      number = i)
}

class Arty100TShell()(implicit p: Parameters) extends Arty100TShellBasicOverlays
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  p(ClockInputOverlayKey).foreach(_.place(ClockInputDesignInput()))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addBoardPin(reset, "reset")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset
    val sysclk: Clock = sys_clock.get() match {
      case Some(x: SysClockArtyPlacedOverlay) => x.clock
    }
    val powerOnReset = PowerOnResetFPGAOnly(sysclk)
    sdc.addAsyncPath(Seq(powerOnReset))

    pllReset :=
      (!reset_ibuf.io.O) || powerOnReset //Arty100T is active low reset
  }
}

class Arty100TShellGPIOPMOD()(implicit p: Parameters) extends Arty100TShellBasicOverlays
//This is the Shell used for coreip arty builds, with GPIOS and trace signals on the pmods
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val gpio_pmod = Overlay(GPIOPMODOverlayKey, new GPIOPMODArtyShellPlacer(this, GPIOPMODShellInput()))
  val trace_pmod = Overlay(TracePMODOverlayKey, new TracePMODArtyShellPlacer(this, TracePMODShellInput()))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  p(ClockInputOverlayKey).foreach(_.place(ClockInputDesignInput()))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addBoardPin(reset, "reset")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    val sysclk: Clock = sys_clock.get() match {
      case Some(x: SysClockArtyPlacedOverlay) => x.clock
    }
    val powerOnReset = PowerOnResetFPGAOnly(sysclk)
    sdc.addAsyncPath(Seq(powerOnReset))
    val ctsReset: Bool = cts_reset.get() match {
      case Some(x: CTSResetArtyPlacedOverlay) => x.designInput.rst
      case None => false.B
    }

    pllReset :=
      (!reset_ibuf.io.O) || powerOnReset || ctsReset //Arty100T is active low reset
  }
}
