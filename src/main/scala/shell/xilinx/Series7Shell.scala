// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.config._
import sifive.fpgashells.clocks._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.shell._

abstract class XilinxShell()(implicit p: Parameters) extends IOShell
{
  def setBoardPin(io: IOPin, pin: String) {
    addConstraint(s"set_property BOARD_PIN {${pin}} ${portOf(io)}")
  }
  def setPackagePin(io: IOPin, pin: String) {
    addConstraint(s"set_property PACKAGE_PIN ${pin} ${portOf(io)}")
  }
  def setIOStandard(io: IOPin, standard: String) {
    addConstraint(s"set_property IOSTANDARD ${standard} ${portOf(io)}")
  }
  def setIOB(io: IOPin) {
    if (io.isOutput) {
      addConstraint(s"set_property IOB TRUE [ get_cells -of_objects [ all_fanin -flat -startpoints_only ${portOf(io)} ] ]")
    } else {
      addConstraint(s"set_property IOB TRUE [ get_cells -of_objects [ all_fanout -flat -endpoints_only ${portOf(io)} ] ]")
    }
  }
  def setSlew(io: IOPin, speed: String) {
    addConstraint(s"set_property SLEW ${speed} ${portOf(io)}")
  }
  def setTermination(io: IOPin, kind: String) {
    addConstraint(s"set_property OFFCHIP_TERM ${kind} ${portOf(io)}")
  }
}

abstract class Series7Shell()(implicit p: Parameters) extends XilinxShell
{
  val pllFactory = new PLLFactory(this, 7, p => Module(new Series7MMCM(p)))
  override def designParameters = super.designParameters.alterPartial {
    case PLLFactoryKey => pllFactory
  }
}
