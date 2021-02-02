// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.devices.xilinx.xilinxgenesys2mig._

class SysClockGenesys2PlacedOverlay(val shell: Genesys2ShellBasicOverlays, name: String, val designInput: ClockInputDesignInput, val shellInput: ClockInputShellInput)
  extends LVDSClockInputXilinxPlacedOverlay(name, designInput, shellInput)
{
  val node = shell { ClockSourceNode(freqMHz = 200, jitterPS = 2.5)(ValName(name)) }

  shell { InModuleBody {
    shell.xdc.addPackagePin(io.p, "AD12")
    shell.xdc.addPackagePin(io.n, "AD11")
    shell.xdc.addIOStandard(io.p, "LVDS")
    shell.xdc.addIOStandard(io.n, "LVDS")
  } }
}
class SysClockGenesys2ShellPlacer(shell: Genesys2ShellBasicOverlays, val shellInput: ClockInputShellInput)(implicit val valName: ValName)
  extends ClockInputShellPlacer[Genesys2ShellBasicOverlays]
{
    def place(designInput: ClockInputDesignInput) = new SysClockGenesys2PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class UARTGenesys2PlacedOverlay(val shell: Genesys2ShellBasicOverlays, name: String, val designInput: UARTDesignInput, val shellInput: UARTShellInput)
  extends UARTXilinxPlacedOverlay(name, designInput, shellInput, false)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("Y20", IOPin(io.rxd)),
                                        ("Y23", IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addIOB(io)
    } }
  } }
}

class UARTGenesys2ShellPlacer(shell: Genesys2ShellBasicOverlays, val shellInput: UARTShellInput)(implicit val valName: ValName)
  extends UARTShellPlacer[Genesys2ShellBasicOverlays] {
  def place(designInput: UARTDesignInput) = new UARTGenesys2PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class JTAGDebugGenesys2PlacedOverlay(val shell: Genesys2ShellBasicOverlays, name: String, val designInput: JTAGDebugDesignInput, val shellInput: JTAGDebugShellInput)
  extends JTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val pin_locations = Map(
      "PMOD_JA"  -> Seq("U27",       "T22",       "T27",       "U28",       "T23"    )
    )
    val pins      = Seq(io.jtag_TCK, io.jtag_TMS, io.jtag_TDI, io.jtag_TDO, io.srst_n)

    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))

    val pin_voltage:String = "LVCMOS33"

    (pin_locations(shellInput.location.get) zip pins) foreach { case (pin_location, ioport) =>
      val io = IOPin(ioport)
      shell.xdc.addPackagePin(io, pin_location)
      shell.xdc.addIOStandard(io, pin_voltage)
      shell.xdc.addPullup(io)
      shell.xdc.addIOB(io)
    }
  } }
}

class JTAGDebugGenesys2ShellPlacer(shell: Genesys2ShellBasicOverlays, val shellInput: JTAGDebugShellInput)(implicit val valName: ValName)
  extends JTAGDebugShellPlacer[Genesys2ShellBasicOverlays] {
  def place(designInput: JTAGDebugDesignInput) = new JTAGDebugGenesys2PlacedOverlay(shell, valName.name, designInput, shellInput)
}

case object Genesys2DDRSize extends Field[BigInt](0x40000000L * 1) // 1GB
class DDRGenesys2PlacedOverlay(val shell: Genesys2ShellBasicOverlays, name: String, val designInput: DDRDesignInput, val shellInput: DDRShellInput)
  extends DDRPlacedOverlay[XilinxGenesys2MIGPads](name, designInput, shellInput)
{
  val size = p(Genesys2DDRSize)

  val migParams = XilinxGenesys2MIGParams(address = AddressSet.misaligned(di.baseAddress, size))
  val mig = LazyModule(new XilinxGenesys2MIG(migParams))
  val ioNode = BundleBridgeSource(() => mig.module.io.cloneType)
  val topIONode = shell { ioNode.makeSink() }
  val ddrUI     = shell { ClockSourceNode(freqMHz = 200) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := designInput.wrangler := ddrUI

  def overlayOutput = DDROverlayOutput(ddr = mig.node)
  def ioFactory = new XilinxGenesys2MIGPads(size)

  InModuleBody { ioNode.bundle <> mig.module.io }

  shell { InModuleBody {
    require (shell.sys_clock.get.isDefined, "Use of DDRGenesys2Overlay depends on SysClockGenesys2Overlay")
    val (sys, _) = shell.sys_clock.get.get.overlayOutput.node.out(0)
    val (ui, _) = ddrUI.out(0)
    val (ar, _) = areset.in(0)
    val port = topIONode.bundle.port
    io <> port
    ui.clock := port.ui_clk
    ui.reset := /*!port.mmcm_locked ||*/ port.ui_clk_sync_rst
    port.sys_clk_i := sys.clock.asUInt
    port.sys_rst := sys.reset // pllReset
    port.aresetn := !ar.reset

    val allddrpins = Seq(     "AC12", "AE8", "AD8",  "AC10", "AD9",  "AA13", "AA10",
      "AA11", "Y10",  "Y11",  "AB8",  "AA8", "AB12", "AA12", "AE9",  "AB10", "AC11",
      "AE11", "AF11", "AG13", "AG5",  "AB9", "AC9",  "AJ9",  "AH12", "AD4",  "AF3",
      "AH4",  "AF8",  "AK9",  "AD3",  "AC2", "AC1",  "AC5",  "AC4",  "AD6",  "AE6",
      "AC7",  "AF2",  "AE1",  "AF1",  "AE4", "AE3",  "AE5",  "AF5",  "AF6",  "AJ4",
      "AH6",  "AH5",  "AH2",  "AJ2",  "AJ1", "AK1",  "AJ3",  "AF7",  "AG7",  "AJ6",
      "AK6",  "AJ8",  "AK8",  "AK5",  "AK4", "AD1",  "AG3",  "AH1",  "AJ7",  "AD2",
      "AG4",  "AG2",  "AH7")

    (IOPin.of(io) zip allddrpins) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
  } }

  shell.sdc.addGroup(pins = Seq(mig.island.module.blackbox.io.ui_clk))
}
class DDRGenesys2ShellPlacer(shell: Genesys2ShellBasicOverlays, val shellInput: DDRShellInput)(implicit val valName: ValName)
  extends DDRShellPlacer[Genesys2ShellBasicOverlays] {
  def place(designInput: DDRDesignInput) = new DDRGenesys2PlacedOverlay(shell, valName.name, designInput, shellInput)
}

abstract class Genesys2ShellBasicOverlays()(implicit p: Parameters) extends Series7Shell {
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val sys_clock = Overlay(ClockInputOverlayKey, new SysClockGenesys2ShellPlacer(this, ClockInputShellInput()))
  val ddr       = Overlay(DDROverlayKey, new DDRGenesys2ShellPlacer(this, DDRShellInput()))
}

case object Genesys2ShellPMOD extends Field[String]("JTAG")

class WithGenesys2ShellPMOD(device: String) extends Config((site, here, up) => {
  case Genesys2ShellPMOD => device
})

class WithGenesys2ShellPMODJTAG extends WithGenesys2ShellPMOD("JTAG")

class Genesys2Shell()(implicit p: Parameters) extends Genesys2ShellBasicOverlays
{
  val jtag_location = Some("PMOD_JA")

  // Order matters; ddr depends on sys_clock
  val uart      = Overlay(UARTOverlayKey, new UARTGenesys2ShellPlacer(this, UARTShellInput()))
  val jtag      = Overlay(JTAGDebugOverlayKey, new JTAGDebugGenesys2ShellPlacer(this, JTAGDebugShellInput(location = jtag_location)))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  designParameters(ClockInputOverlayKey).foreach { unused =>
    val source = unused.place(ClockInputDesignInput()).overlayOutput.node
    val sink = ClockSinkNode(Seq(ClockSinkParameters()))
    sink := source
  }

  override lazy val module = new LazyRawModuleImp(this) {
    val reset_n = IO(Input(Bool()))
    xdc.addPackagePin(reset_n, "R19")
    xdc.addIOStandard(reset_n, "LVCMOS33")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := ~reset_n

    val sysclk: Clock = sys_clock.get() match {
      case Some(x: SysClockGenesys2PlacedOverlay) => x.clock
    }

    val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
    sdc.addAsyncPath(Seq(powerOnReset))

    pllReset := (reset_ibuf.io.O || powerOnReset)
  }
}
