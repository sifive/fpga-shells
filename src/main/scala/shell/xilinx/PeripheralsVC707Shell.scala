// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.experimental._

import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._

import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

import sifive.blocks.devices.gpio._
import sifive.blocks.devices.pinctrl._

import sifive.enterprise.firrtl._

class UARTPeripheralVC707PlacedOverlay(val shell: VC707Shell, name: String, val designInput: UARTDesignInput, val shellInput: UARTShellInput)
  extends UARTXilinxPlacedOverlay(name, designInput, shellInput, true)
{
    shell { InModuleBody {
    val uartLocations = List(List("AT32", "AR34", "AU33", "AU36"), List("FILL", "THIS", "IN", "PINS"))
    val packagePinsWithPackageIOs = Seq((uartLocations(shellInput.number)(0), IOPin(io.ctsn.get)),
                                        (uartLocations(shellInput.number)(1), IOPin(io.rtsn.get)),
                                        (uartLocations(shellInput.number)(2), IOPin(io.rxd)),
                                        (uartLocations(shellInput.number)(3), IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
  } }
}

class UARTPeripheralVC707ShellPlacer(val shell: VC707Shell, val shellInput: UARTShellInput)(implicit val valName: ValName)
  extends UARTShellPlacer[VC707Shell]
{
  def place(designInput: UARTDesignInput) = new UARTPeripheralVC707PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class I2CPeripheralVC707PlacedOverlay(val shell: VC707Shell, name: String, val designInput: I2CDesignInput, val shellInput: I2CShellInput)
  extends I2CXilinxPlacedOverlay(name, designInput, shellInput)
{
    shell { InModuleBody {
    val i2cLocations = List(List("A", "B"), List("FILL", "IN"))
    val packagePinsWithPackageIOs = Seq((i2cLocations(shellInput.number)(0), IOPin(io.scl)),
                                        (i2cLocations(shellInput.number)(1), IOPin(io.sda)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
  } }
}

class I2CPeripheralVC707ShellPlacer(val shell: VC707Shell, val shellInput: I2CShellInput)(implicit val valName: ValName)
  extends I2CShellPlacer[VC707Shell]
{
  def place(designInput: I2CDesignInput) = new I2CPeripheralVC707PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class QSPIPeripheralVC707PlacedOverlay(val shell: VC707Shell, name: String, val designInput: SPIFlashDesignInput, val shellInput: SPIFlashShellInput)
  extends SPIFlashXilinxPlacedOverlay(name, designInput, shellInput)
{
    shell { InModuleBody {
    val qspiLocations = List(List("A", "B", "C", "D", "E", "F"), List("FILL", "THIS", "IN", "WITH", "PINS", "LATER"))
    val packagePinsWithPackageIOs = Seq((qspiLocations(shellInput.number)(0), IOPin(io.qspi_sck)),
                                        (qspiLocations(shellInput.number)(1), IOPin(io.qspi_cs)),
                                        (qspiLocations(shellInput.number)(2), IOPin(io.qspi_dq(0))),
                                        (qspiLocations(shellInput.number)(3), IOPin(io.qspi_dq(1))),
                                        (qspiLocations(shellInput.number)(4), IOPin(io.qspi_dq(2))),
                                        (qspiLocations(shellInput.number)(5), IOPin(io.qspi_dq(3))))

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

class QSPIPeripheralVC707ShellPlacer(val shell: VC707Shell, val shellInput: SPIFlashShellInput)(implicit val valName: ValName)
  extends SPIFlashShellPlacer[VC707Shell]
{
  def place(designInput: SPIFlashDesignInput) = new QSPIPeripheralVC707PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class PWMPeripheralVC707PlacedOverlay(val shell: VC707Shell, name: String, val designInput: PWMDesignInput, val shellInput: PWMShellInput)
  extends PWMXilinxPlacedOverlay(name, designInput, shellInput)
{
    shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("A", IOPin(io.pwm_gpio(0))),
                                        ("B", IOPin(io.pwm_gpio(1))),
                                        ("C", IOPin(io.pwm_gpio(2))),
                                        ("D", IOPin(io.pwm_gpio(3))))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
  } }
}

class PWMPeripheralVC707ShellPlacer(val shell: VC707Shell, val shellInput: PWMShellInput)(implicit val valName: ValName)
  extends PWMShellPlacer[VC707Shell] {
  def place(designInput: PWMDesignInput) = new PWMPeripheralVC707PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class GPIOPeripheralVC707PlacedOverlay(val shell: VC707Shell, name: String, val designInput: GPIODesignInput, val shellInput: GPIOShellInput)
  extends GPIOXilinxPlacedOverlay(name, designInput, shellInput)
{
    shell { InModuleBody {
    val gpioLocations = List("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M")
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

class GPIOPeripheralVC707ShellPlacer(val shell: VC707Shell, val shellInput: GPIOShellInput)(implicit val valName: ValName)
  extends GPIOShellPlacer[VC707Shell] {

  def place(designInput: GPIODesignInput) = new GPIOPeripheralVC707PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class PeripheralVC707Shell(implicit p: Parameters) extends VC707Shell {

  val gpio           = Overlay(GPIOOverlayKey,       new GPIOPeripheralVC707ShellPlacer(this, GPIOShellInput()))
  val uart  = Seq.tabulate(2) { i => Overlay(UARTOverlayKey, new UARTPeripheralVC707ShellPlacer(this, UARTShellInput(number = i))(valName = ValName(s"uart$i"))) }
  val qspi      = Seq.tabulate(2) { i => Overlay(SPIFlashOverlayKey, new QSPIPeripheralVC707ShellPlacer(this, SPIFlashShellInput(number = i))(valName = ValName(s"qspi$i"))) }
  val pwm       = Seq.tabulate(1) { i => Overlay(PWMOverlayKey, new PWMPeripheralVC707ShellPlacer(this, PWMShellInput(number = i))(valName = ValName(s"pwm$i"))) }
  val i2c       = Seq.tabulate(2) { i => Overlay(I2COverlayKey, new I2CPeripheralVC707ShellPlacer(this, I2CShellInput(number = i))(valName = ValName(s"i2c$i"))) }

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  p(ClockInputOverlayKey).foreach(_.place(ClockInputDesignInput()))

  override lazy val module = new LazyRawModuleImp(this) {
    topDesign.module.asInstanceOf[LazyRawModuleImp with MarkDUT].markDUT()

    val reset = IO(Input(Bool()))
    val clock = IO(Input(Clock()))

    val por_clock = sys_clock.get.get.asInstanceOf[SysClockVC707PlacedOverlay].clock
    val powerOnReset = PowerOnResetFPGAOnly(por_clock)

    pllReset :=
      reset || powerOnReset

  }
}
