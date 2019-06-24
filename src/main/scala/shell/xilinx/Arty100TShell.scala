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
import sifive.fpgashells.devices.xilinx.xilinxarty100tmig._

class SysClockArtyOverlay(val shell: Arty100TShellBasicOverlays, val name: String, params: ClockInputOverlayParams)
  extends SingleEndedClockInputXilinxOverlay(params)
{
  val node = shell { ClockSourceNode(name, freqMHz = 100, jitterPS = 50) }

  shell { InModuleBody {
    val clk: Clock = io
    shell.xdc.addPackagePin(clk, "E3")
    shell.xdc.addIOStandard(clk, "LVCMOS33")
  } }
}

//PMOD JA used for SDIO
class SDIOArtyOverlay(val shell: Arty100TShellBasicOverlays, val name: String, params: SDIOOverlayParams)
  extends SDIOXilinxOverlay(params)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("D12", IOPin(io.sdio_clk)),
      ("B11", IOPin(io.sdio_cmd)),
      ("A11", IOPin(io.sdio_dat_0)),
      ("D13", IOPin(io.sdio_dat_1)),
      ("B18", IOPin(io.sdio_dat_2)),
      ("G13", IOPin(io.sdio_dat_3)))

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

class SPIFlashArtyOverlay(val shell: Arty100TShellBasicOverlays, val name: String, params: SPIFlashOverlayParams)
  extends SPIFlashXilinxOverlay(params)
{

  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("L16", IOPin(io.qspi_sck)),
      ("L13", IOPin(io.qspi_cs)),
      ("K17", IOPin(io.qspi_dq_0)),
      ("K18", IOPin(io.qspi_dq_1)),
      ("L14", IOPin(io.qspi_dq_2)),
      ("M14", IOPin(io.qspi_dq_3)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}

class GPIOPMODArtyOverlay(val shell: Arty100TShellBasicOverlays, val name: String, params: GPIOPMODOverlayParams)
  extends GPIOPMODXilinxOverlay(params)
{

  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("E15", IOPin(io.gpio_pmod_0)), //These are PMOD B. Can be changed
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
  } }
}

class UARTArtyOverlay(val shell: Arty100TShellBasicOverlays, val name: String, params: UARTOverlayParams)
  extends UARTXilinxOverlay(params, false)
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

//LEDS - 4 normal leds, r0, g0, b0, r1, g1, b1 ...
class LEDArtyOverlay(val shell: Arty100TShellBasicOverlays, val name: String, params: LEDOverlayParams)
  extends LEDXilinxOverlay(params, packagePins = Seq("H5", "J5", "T9", "T10", "G6", "F6", "E1", "G3", "J4", "G4", "J3", "J2", "H4", "K1", "H6", "K2"))

//SWs
class SwitchArtyOverlay(val shell: Arty100TShellBasicOverlays, val name: String, params: SwitchOverlayParams)
  extends SwitchXilinxOverlay(params, packagePins = Seq("A8", "C11", "C10", "A10"))

//Buttons
class ButtonArtyOverlay(val shell: Arty100TShellBasicOverlays, val name: String, params: ButtonOverlayParams)
  extends ButtonXilinxOverlay(params, packagePins = Seq("D9", "C9", "B9", "B8"))

// PMOD JD used for JTAG
class JTAGDebugArtyOverlay(val shell: Arty100TShellBasicOverlays, val name: String, params: JTAGDebugOverlayParams)
  extends JTAGDebugXilinxOverlay(params)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))
    val packagePinsWithPackageIOs = Seq(("F4", IOPin(io.jtag_TCK)),  //pin JD-3
      ("D2", IOPin(io.jtag_TMS)),  //pin JD-8
      ("E2", IOPin(io.jtag_TDI)),  //pin JD-7
      ("D4", IOPin(io.jtag_TDO)))  //pin JD-1

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addPullup(io)
    } }
  } }
}

//cjtag
class cJTAGDebugArtyOverlay(val shell: Arty100TShellBasicOverlays, val name: String, params: cJTAGDebugOverlayParams)
  extends cJTAGDebugXilinxOverlay(params)
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
      shell.xdc.addPullup(io)
    } }
  } }
}

case object ArtyDDRSize extends Field[BigInt](0x10000000L * 1) // 256 MB
class DDRArtyOverlay(val shell: Arty100TShellBasicOverlays, val name: String, params: DDROverlayParams)
  extends DDROverlay[XilinxArty100TMIGPads](params)
{
  val size = p(ArtyDDRSize)

  val ddrClk1 = shell { ClockSinkNode(freqMHz = 166.666)}
  val ddrClk2 = shell { ClockSinkNode(freqMHz = 200)}
  val ddrGroup = shell { ClockGroup() }
  ddrClk1 := params.wrangler := ddrGroup := params.corePLL
  ddrClk2 := params.wrangler := ddrGroup
  
  val sdcClockName = "userclk1"
  val migParams = XilinxArty100TMIGParams(address = AddressSet.misaligned(params.baseAddress, size))
  val mig = LazyModule(new XilinxArty100TMIG(migParams))
  val ioNode = BundleBridgeSource(() => mig.module.io.cloneType)
  val topIONode = shell { ioNode.makeSink() }
  val ddrUI     = shell { ClockSourceNode(sdcClockName, freqMHz = 100) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := params.wrangler := ddrUI

  def designOutput = mig.node
  def ioFactory = new XilinxArty100TMIGPads(size)

  InModuleBody { ioNode.bundle <> mig.module.io }

  shell { InModuleBody {
    require (shell.sys_clock.isDefined, "Use of DDRArtyOverlay depends on SysClockArtyOverlay")
    val (sys, _) = shell.sys_clock.get.node.out(0)
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

  shell.sdc.addGroup(clocks = Seq("clk_pll_i", "userClock1"))
}

abstract class Arty100TShellBasicOverlays()(implicit p: Parameters) extends Series7Shell {
  // Order matters; ddr depends on sys_clock
  val sys_clock = Overlay(ClockInputOverlayKey)(new SysClockArtyOverlay   (_, _, _))
  val led       = Overlay(LEDOverlayKey)       (new LEDArtyOverlay        (_, _, _))
  val switch    = Overlay(SwitchOverlayKey)    (new SwitchArtyOverlay     (_, _, _))
  val button    = Overlay(ButtonOverlayKey)    (new ButtonArtyOverlay     (_, _, _))
  val ddr       = Overlay(DDROverlayKey)       (new DDRArtyOverlay        (_, _, _))
  val uart      = Overlay(UARTOverlayKey)      (new UARTArtyOverlay       (_, _, _))
  val sdio      = Overlay(SDIOOverlayKey)      (new SDIOArtyOverlay       (_, _, _))
  val jtag      = Overlay(JTAGDebugOverlayKey) (new JTAGDebugArtyOverlay  (_, _, _))
  val cjtag     = Overlay(cJTAGDebugOverlayKey) (new cJTAGDebugArtyOverlay  (_, _, _))
  val spi_flash = Overlay(SPIFlashOverlayKey)  (new SPIFlashArtyOverlay   (_, _, _))
}

class Arty100TShell()(implicit p: Parameters) extends Arty100TShellBasicOverlays
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  p(ClockInputOverlayKey).foreach(_(ClockInputOverlayParams()))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addBoardPin(reset, "reset")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    val powerOnReset = PowerOnResetFPGAOnly(sys_clock.get.clock)
    sdc.addAsyncPath(Seq(powerOnReset))

    pllReset :=
      (!reset_ibuf.io.O) || powerOnReset //Arty100T is active low reset
  }
}

class Arty100TShellGPIOPMOD()(implicit p: Parameters) extends Arty100TShellBasicOverlays
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val gpio_pmod = Overlay(GPIOPMODOverlayKey) (new GPIOPMODArtyOverlay (_,_,_))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  p(ClockInputOverlayKey).foreach(_(ClockInputOverlayParams()))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addBoardPin(reset, "reset")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    val powerOnReset = PowerOnResetFPGAOnly(sys_clock.get.clock)
    sdc.addAsyncPath(Seq(powerOnReset))

    pllReset :=
      (!reset_ibuf.io.O) || powerOnReset //Arty100T is active low reset
  }
}
