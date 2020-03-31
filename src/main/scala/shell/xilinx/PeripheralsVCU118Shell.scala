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

class UARTPeripheralVCU118PlacedOverlay(val shell: VCU118ShellBasicOverlays, name: String, val designInput: UARTDesignInput, val shellInput: UARTShellInput)
  extends UARTXilinxPlacedOverlay(name, designInput, shellInput, true)
{
    shell { InModuleBody {
    val uartLocations = List(List("AY25", "BB22", "AW25", "BB21"), List("AW11", "AP13", "AY10", "AR13")) //uart0 - USB, uart1 - FMC 105 debug card J20 p1-rx p2-tx p3-ctsn p4-rtsn
    val packagePinsWithPackageIOs = Seq((uartLocations(shellInput.index)(0), IOPin(io.ctsn.get)),
                                        (uartLocations(shellInput.index)(1), IOPin(io.rtsn.get)),
                                        (uartLocations(shellInput.index)(2), IOPin(io.rxd)),
                                        (uartLocations(shellInput.index)(3), IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
  } }
}

class UARTPeripheralVCU118ShellPlacer(val shell: VCU118ShellBasicOverlays, val shellInput: UARTShellInput)(implicit val valName: ValName)
  extends UARTShellPlacer[VCU118ShellBasicOverlays]
{
  def place(designInput: UARTDesignInput) = new UARTPeripheralVCU118PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class I2CPeripheralVCU118PlacedOverlay(val shell: VCU118ShellBasicOverlays, name: String, val designInput: I2CDesignInput, val shellInput: I2CShellInput)
  extends I2CXilinxPlacedOverlay(name, designInput, shellInput)
{
    shell { InModuleBody {
    val i2cLocations = List(List("BA14", "AW12"), List("BB14", "AY12")) //i2c0: J1 p37-scl p38-sda i2c1: J2 p39-scl p40-sda
    val packagePinsWithPackageIOs = Seq((i2cLocations(shellInput.index)(0), IOPin(io.scl)),
                                        (i2cLocations(shellInput.index)(1), IOPin(io.sda)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
  } }
}

class I2CPeripheralVCU118ShellPlacer(val shell: VCU118ShellBasicOverlays, val shellInput: I2CShellInput)(implicit val valName: ValName)
  extends I2CShellPlacer[VCU118ShellBasicOverlays]
{
  def place(designInput: I2CDesignInput) = new I2CPeripheralVCU118PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class QSPIPeripheralVCU118PlacedOverlay(val shell: VCU118ShellBasicOverlays, name: String, val designInput: SPIFlashDesignInput, val shellInput: SPIFlashShellInput)
  extends SPIFlashXilinxPlacedOverlay(name, designInput, shellInput)
{
    shell { InModuleBody {
    val qspiLocations = List(List("AY9", "BB13", "BA9", "BB12", "BF10", "BA16")) //J1 pins 1-6 and 7-12 (sck, cs, dq0-3) 
//FIX when built in spi flash is integrated
    val packagePinsWithPackageIOs = Seq((qspiLocations(shellInput.index)(0), IOPin(io.qspi_sck)),
                                        (qspiLocations(shellInput.index)(1), IOPin(io.qspi_cs)),
                                        (qspiLocations(shellInput.index)(2), IOPin(io.qspi_dq(0))),
                                        (qspiLocations(shellInput.index)(3), IOPin(io.qspi_dq(1))),
                                        (qspiLocations(shellInput.index)(4), IOPin(io.qspi_dq(2))),
                                        (qspiLocations(shellInput.index)(5), IOPin(io.qspi_dq(3))))

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

class QSPIPeripheralVCU118ShellPlacer(val shell: VCU118ShellBasicOverlays, val shellInput: SPIFlashShellInput)(implicit val valName: ValName)
  extends SPIFlashShellPlacer[VCU118ShellBasicOverlays]
{
  def place(designInput: SPIFlashDesignInput) = new QSPIPeripheralVCU118PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class GPIOPeripheralVCU118PlacedOverlay(val shell: VCU118ShellBasicOverlays, name: String, val designInput: GPIODesignInput, val shellInput: GPIOShellInput)
  extends GPIOXilinxPlacedOverlay(name, designInput, shellInput)
{
    shell { InModuleBody {
    val gpioLocations = List("AU11", "AT12", "AV11", "AU12", "AW13", "AK15", "AY13", "AL15", "AN16", "AL14", "AP16", "AM14", "BF9", "BA15", "BC11", "BC14") //J20 pins 5-16, J1 pins 7-10
    val iosWithLocs = io.gpio.zip(gpioLocations)
    val packagePinsWithPackageIOs = iosWithLocs.map { case (io, pin) => (pin, IOPin(io)) }
    println(packagePinsWithPackageIOs)

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
  } }
}

class GPIOPeripheralVCU118ShellPlacer(val shell: VCU118ShellBasicOverlays, val shellInput: GPIOShellInput)(implicit val valName: ValName)
  extends GPIOShellPlacer[VCU118ShellBasicOverlays] {

  def place(designInput: GPIODesignInput) = new GPIOPeripheralVCU118PlacedOverlay(shell, valName.name, designInput, shellInput)
}

object PMODVCU118PinConstraints {
  val pins = Seq(Seq("AY14","AV16","AY15","AU16","AW15","AT15","AV15","AT16"),
                 Seq("N28","P29","M30","L31","N30","M31","P30","R29"))
}
class PMODVCU118PlacedOverlay(val shell: VCU118ShellBasicOverlays, name: String, val designInput: PMODDesignInput, val shellInput: PMODShellInput)
  extends PMODXilinxPlacedOverlay(name, designInput, shellInput, packagePin = PMODVCU118PinConstraints.pins(shellInput.index), ioStandard = "LVCMOS18")
class PMODVCU118ShellPlacer(shell: VCU118ShellBasicOverlays, val shellInput: PMODShellInput)(implicit val valName: ValName)
  extends PMODShellPlacer[VCU118ShellBasicOverlays] {
  def place(designInput: PMODDesignInput) = new PMODVCU118PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class PMODJTAGVCU118PlacedOverlay(val shell: VCU118ShellBasicOverlays, name: String, val designInput: JTAGDebugDesignInput, val shellInput: JTAGDebugShellInput)
  extends JTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))
    val packagePinsWithPackageIOs = Seq(("AW15", IOPin(io.jtag_TCK)),
                                        ("AU16", IOPin(io.jtag_TMS)),
                                        ("AV16", IOPin(io.jtag_TDI)),
                                        ("AY14", IOPin(io.jtag_TDO)),
                                        ("AY15", IOPin(io.srst_n))) 
    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addPullup(io)
      shell.xdc.addIOB(io)
    } }
  } }
}
class PMODJTAGVCU118ShellPlacer(shell: VCU118ShellBasicOverlays, val shellInput: JTAGDebugShellInput)(implicit val valName: ValName)
  extends JTAGDebugShellPlacer[VCU118ShellBasicOverlays] {
  def place(designInput: JTAGDebugDesignInput) = new PMODJTAGVCU118PlacedOverlay(shell, valName.name, designInput, shellInput)
}

abstract class PeripheralsVCU118Shell(implicit p: Parameters) extends VCU118ShellBasicOverlays{
  //val pmod_female      = Overlay(PMODOverlayKey, new PMODVCU118ShellPlacer(this, PMODShellInput(index = 0)))
  val pmodJTAG = Overlay(JTAGDebugOverlayKey, new PMODJTAGVCU118ShellPlacer(this, JTAGDebugShellInput()))
  val gpio           = Overlay(GPIOOverlayKey,       new GPIOPeripheralVCU118ShellPlacer(this, GPIOShellInput()))
  val uart  = Seq.tabulate(2) { i => Overlay(UARTOverlayKey, new UARTPeripheralVCU118ShellPlacer(this, UARTShellInput(index = i))(valName = ValName(s"uart$i"))) }
  val qspi      = Seq.tabulate(0) { i => Overlay(SPIFlashOverlayKey, new QSPIPeripheralVCU118ShellPlacer(this, SPIFlashShellInput(index = i))(valName = ValName(s"qspi$i"))) }
  val i2c       = Seq.tabulate(2) { i => Overlay(I2COverlayKey, new I2CPeripheralVCU118ShellPlacer(this, I2CShellInput(index = i))(valName = ValName(s"i2c$i"))) }

  val topDesign = LazyModule(p(DesignKey)(designParameters))
  p(ClockInputOverlayKey).foreach(_.place(ClockInputDesignInput()))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    val por_clock = sys_clock.get.get.asInstanceOf[SysClockVCU118PlacedOverlay].clock
    val powerOnReset = PowerOnResetFPGAOnly(por_clock)

    xdc.addPackagePin(reset, "L19")
    xdc.addIOStandard(reset, "LVCMOS12")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    sdc.addAsyncPath(Seq(powerOnReset))

    val ereset: Bool = chiplink.get() match {
      case Some(x: ChipLinkVCU118PlacedOverlay) => !x.ereset_n
      case _ => false.B
    }
   pllReset := reset_ibuf.io.O || powerOnReset || ereset
  }
}
