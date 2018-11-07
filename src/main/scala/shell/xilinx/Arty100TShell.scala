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

class SysClockArtyOverlay(val shell: Arty100TShell, val name: String, params: ClockInputOverlayParams)
  extends SingleEndedClockInputXilinxOverlay(params)
{
  val node = shell { ClockSourceNode(freqMHz = 200, jitterPS = 50)(ValName(name)) }

  shell { InModuleBody {
    val clk: Clock = io
    shell.xdc.addPackagePin(clk, "E3")
    shell.xdc.addIOStandard(clk, "LVCMOS33")
  } }
}

//PMOD JA used for SDIO. Pins may need to be remapped based on pinout of header
//TODO: CD,WP?
class SDIOArtyOverlay(val shell: Arty100TShell, val name: String, params: SDIOOverlayParams)
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

class UARTArtyOverlay(val shell: Arty100TShell, val name: String, params: UARTOverlayParams)
  extends UARTXilinxOverlay(params, false)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("D10", IOPin(io.rxd)),
      ("A9", IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addIOB(io)
    } }
  } }
}

//TODO: add rgbs?
class LEDArtyOverlay(val shell: Arty100TShell, val name: String, params: LEDOverlayParams)
  extends LEDXilinxOverlay(params, packagePins = Seq("H5", "J5", "T9", "T10"))

class SwitchArtyOverlay(val shell: Arty100TShell, val name: String, params: SwitchOverlayParams)
  extends SwitchXilinxOverlay(params, packagePins = Seq("A8", "C11", "C10", "A10"))

// PMOD JD used for JTAG
class JTAGDebugArtyOverlay(val shell: Arty100TShell, val name: String, params: JTAGDebugOverlayParams)
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

case object ArtyDDRSize extends Field[BigInt](0x10000000L * 1) // 256 MB
class DDRArtyOverlay(val shell: Arty100TShell, val name: String, params: DDROverlayParams)
  extends DDROverlay[XilinxArty100TMIGPads](params)
{
  val size = p(ArtyDDRSize)

  val migParams = XilinxArty100TMIGParams(address = AddressSet.misaligned(params.baseAddress, size))
  val mig = LazyModule(new XilinxArty100TMIG(migParams))
  val ioNode = BundleBridgeSource(() => mig.module.io.cloneType)
  val topIONode = shell { ioNode.makeSink() }
  val ddrUI     = shell { ClockSourceNode(freqMHz = 200) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := params.wrangler := ddrUI

  def designOutput = mig.node
  def ioFactory = new XilinxArty100TMIGPads(size)

  InModuleBody { ioNode.bundle <> mig.module.io }

  shell { InModuleBody {
    require (shell.sys_clock.isDefined, "Use of DDRArtyOverlay depends on SysClockArtyOverlay")
    val (sys, _) = shell.sys_clock.get.node.out(0)
    val (ui, _) = ddrUI.out(0)
    val (ar, _) = areset.in(0)
    val port = topIONode.bundle.port
    io <> port
    ui.clock := port.ui_clk
    ui.reset := !port.mmcm_locked || port.ui_clk_sync_rst
    port.sys_clk_i := sys.clock.asUInt
    port.sys_rst := sys.reset // pllReset
    port.aresetn := !ar.reset
  } }

  shell.sdc.addGroup(clocks = Seq("clk_pll_i"))
}

class Arty100TShell()(implicit p: Parameters) extends Series7Shell
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  // Order matters; ddr depends on sys_clock
  val sys_clock = Overlay(ClockInputOverlayKey)(new SysClockArtyOverlay   (_, _, _))
  val led       = Overlay(LEDOverlayKey)       (new LEDArtyOverlay        (_, _, _))
  val switch    = Overlay(SwitchOverlayKey)    (new SwitchArtyOverlay     (_, _, _))
  val ddr       = Overlay(DDROverlayKey)       (new DDRArtyOverlay        (_, _, _))
  val uart      = Overlay(UARTOverlayKey)      (new UARTArtyOverlay       (_, _, _))
  val sdio      = Overlay(SDIOOverlayKey)      (new SDIOArtyOverlay       (_, _, _))
  val jtag      = Overlay(JTAGDebugOverlayKey) (new JTAGDebugArtyOverlay  (_, _, _))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  p(ClockInputOverlayKey).foreach(_(ClockInputOverlayParams()))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addBoardPin(reset, "reset")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := !reset // Arty100T is active low reset

    val powerOnReset = PowerOnResetFPGAOnly(sys_clock.get.clock)
    sdc.addAsyncPath(Seq(powerOnReset))

    pllReset :=
      reset_ibuf.io.O || powerOnReset
  }
}
