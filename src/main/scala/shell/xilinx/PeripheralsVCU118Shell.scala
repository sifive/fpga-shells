// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.experimental.{attach, Analog, IO}
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
import sifive.fpgashells.ip.xilinx.xxv_ethernet._

/*
class SPIFlashVCUV18PlacedOverlay(val shell: VCU118ShellBasicOverlays, name: String, val designInput: SPIFlashDesignInput, val shellInput: SPIFlashShellInput)
  extends SPIFlashXilinxPlacedOverlay(name, designInput, shellInput)
{

  shell { InModuleBody { 
    /*val packagePinsWithPackageIOs = Seq(("AF13", IOPin(io.qspi_sck)),
      ("AJ11", IOPin(io.qspi_cs)),
      ("AP11", IOPin(io.qspi_dq(0))),
      ("AN11", IOPin(io.qspi_dq(1))),
      ("AM11", IOPin(io.qspi_dq(2))),
      ("AL11", IOPin(io.qspi_dq(3))))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
*/
  } }
}
class SPIFlashVCU118ShellPlacer(shell: VCU118ShellBasicOverlays, val shellInput: SPIFlashShellInput)(implicit val valName: ValName)
  extends SPIFlashShellPlacer[VCU118ShellBasicOverlays] {
  def place(designInput: SPIFlashDesignInput) = new SPIFlashVCU118PlacedOverlay(shell, valName.name, designInput, shellInput)
}
*/

object PMODVCU118PinConstraints {
  val pins = Seq(Seq("AY14","AV16","AY15","AU16","AW15","AT15","AV15","AT16"),
                 Seq("N28","P29","M30","L31","N30","M31","P30","R29"))
}
class PMODVCU118PlacedOverlay(val shell: VCU118ShellBasicOverlays, name: String, val designInput: PMODDesignInput, val shellInput: PMODShellInput)
  extends PMODXilinxPlacedOverlay(name, designInput, shellInput, packagePin = PMODVCU118PinConstraints.pins(shellInput.index), ioStandard = "LVCMOS12")
class PMODVCU118ShellPlacer(shell: VCU118ShellBasicOverlays, val shellInput: PMODShellInput)(implicit val valName: ValName)
  extends PMODShellPlacer[VCU118ShellBasicOverlays] {
  def place(designInput: PMODDesignInput) = new PMODVCU118PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class FMCJTAGVCU118PlacedOverlay(val shell: VCU118ShellBasicOverlays, name: String, val designInput: JTAGDebugDesignInput, val shellInput: JTAGDebugShellInput)
  extends JTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))
    val packagePinsWithPackageIOs = Seq(("AL12", IOPin(io.jtag_TCK)),
                                        ("AN15", IOPin(io.jtag_TMS)),
                                        ("AP15", IOPin(io.jtag_TDI)),
                                        ("AM12", IOPin(io.jtag_TDO)),
                                        ("AK12", IOPin(io.srst_n)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addPullup(io)
      shell.xdc.addIOB(io)
    } }
  } }
}
class FMCJTAGVCU118ShellPlacer(shell: VCU118ShellBasicOverlays, val shellInput: JTAGDebugShellInput)(implicit val valName: ValName)
  extends JTAGDebugShellPlacer[VCU118ShellBasicOverlays] {
  def place(designInput: JTAGDebugDesignInput) = new FMCJTAGVCU118PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class PeripheralsVCU118Shell(implicit p: Parameters) extends VCU118Shell{
  //Shoukd UART be here?
  val pmod      = Seq.tabulate(2)(i => Overlay(PMODOverlayKey, new PMODVCU118ShellPlacer(this, PMODShellInput(index = i))))
  val fmcJTAG = Overlay(JTAGDebugOverlayKey, new FMCJTAGVCU118ShellPlacer(this, JTAGDebugShellInput()))
}

/*
// Something similar to this will need to be in the tip level Sesame Shell
class VCU118Shell()(implicit p: Parameters) extends VCU118ShellBasicOverlays
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  // Order matters; ddr depends on sys_clock
  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  designParameters(ClockInputOverlayKey).foreach { unused =>
    val source = unused.place(ClockInputDesignInput()).overlayOutput.node
    val sink = ClockSinkNode(Seq(ClockSinkParameters()))
    sink := source
  }

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addPackagePin(reset, "L19")
    xdc.addIOStandard(reset, "LVCMOS12")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    val sysclk: Clock = sys_clock.get() match {
      case Some(x: SysClockVCU118PlacedOverlay) => x.clock
    }

    val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
    sdc.addAsyncPath(Seq(powerOnReset))

    val ereset: Bool = chiplink.get() match {
      case Some(x: ChipLinkVCU118PlacedOverlay) => !x.ereset_n
      case _ => false.B
    }

    pllReset := (reset_ibuf.io.O || powerOnReset || ereset)
  }
}
*/
