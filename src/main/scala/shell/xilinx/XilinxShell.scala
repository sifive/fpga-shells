// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import sifive.fpgashells.clocks._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.shell._

class XDC(val name: String)
{
  private var constraints: Seq[() => String] = Nil
  protected def addConstraint(command: => String) { constraints = (() => command) +: constraints }
  ElaborationArtefacts.add(name, constraints.map(_()).reverse.mkString("\n") + "\n")

  def addBoardPin(io: IOPin, pin: String) {
    addConstraint(s"set_property BOARD_PIN {${pin}} ${io.sdcPin}")
  }
  def addPackagePin(io: IOPin, pin: String) {
    addConstraint(s"set_property PACKAGE_PIN {${pin}} ${io.sdcPin}")
  }
  def addIOStandard(io: IOPin, standard: String) {
    addConstraint(s"set_property IOSTANDARD {${standard}} ${io.sdcPin}")
  }
  def addPullup(io: IOPin) {
    addConstraint(s"set_property PULLUP {TRUE} ${io.sdcPin}")
  }
  def addIOB(io: IOPin) {
    if (io.isOutput) {
      addConstraint(s"set_property IOB {TRUE} [ get_cells -of_objects [ all_fanin -flat -startpoints_only ${io.sdcPin}]]")
    } else {
      addConstraint(s"set_property IOB {TRUE} [ get_cells -of_objects [ all_fanout -flat -endpoints_only ${io.sdcPin}]]")
    }
  }
  def addSlew(io: IOPin, speed: String) {
    addConstraint(s"set_property SLEW {${speed}} ${io.sdcPin}")
  }
  def addTermination(io: IOPin, kind: String) {
    addConstraint(s"set_property OFFCHIP_TERM {${kind}} ${io.sdcPin}")
  }
  def clockDedicatedRouteFalse(io: IOPin) {
    addConstraint(s"set_property CLOCK_DEDICATED_ROUTE {FALSE} [get_nets ${io.sdcPin}]")
  }
}

abstract class XilinxShell()(implicit p: Parameters) extends IOShell
{
  val sdc = new SDC("shell.sdc")
  val xdc = new XDC("shell.xdc")
  def pllReset: ModuleValue[Bool]

  ElaborationArtefacts.add("shell.vivado.tcl",
    """set shell_vivado_tcl [file normalize [info script]]
      |set shell_vivado_idx [string last ".shell.vivado.tcl" $shell_vivado_tcl]
      |add_files -fileset [current_fileset -constrset] [string replace $shell_vivado_tcl $shell_vivado_idx 999 ".shell.sdc"]
      |add_files -fileset [current_fileset -constrset] [string replace $shell_vivado_tcl $shell_vivado_idx 999 ".shell.xdc"]
      |""".stripMargin)
}

abstract class Series7Shell()(implicit p: Parameters) extends XilinxShell
{
  val pllFactory = new PLLFactory(this, 7, p => Module(new Series7MMCM(p)))
  override def designParameters = super.designParameters.alterPartial {
    case PLLFactoryKey => pllFactory
  }
}

abstract class UltraScaleShell()(implicit p: Parameters) extends XilinxShell
{
  val pllFactory = new PLLFactory(this, 7, p => Module(new Series7MMCM(p)))
  override def designParameters = super.designParameters.alterPartial {
    case PLLFactoryKey => pllFactory
  }
}
