// See LICENSE for license details.
package sifive.fpgashells.shell.microsemi

 import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import sifive.fpgashells.clocks._
import sifive.fpgashells.ip.microsemi.polarfireccc._
import sifive.fpgashells.ip.microsemi.polarfireinitmonitor._
import sifive.fpgashells.shell._

 class IO_PDC(val name: String)
{
  private var constraints: Seq[() => String] = Nil
  protected def addConstraint(command: => String) { constraints = (() => command) +: constraints }
  ElaborationArtefacts.add(name, constraints.map(_()).reverse.mkString("\n") + "\n")

   def addPin(io: IOPin, pin: String, ioStandard: String = "") {
    def dir = if (io.isInput) { if (io.isOutput) "INOUT" else "INPUT" } else { "OUTPUT" }
    def ioSt = if (!ioStandard.isEmpty) { 
      require(ioStandard == "LVCMOS33") 
      "-io_std LVCMOS33"
      } else {""}
    addConstraint(s"set_io -port_name {${io.name}} -pin_name ${pin} -fixed true -DIRECTION ${dir} ${ioSt} ")
  }
}

 abstract class MicrosemiShell()(implicit p: Parameters) extends IOShell
{
  val sdc = new SDC("shell.sdc")
  val io_pdc = new IO_PDC("shell.io.pdc")
}

 abstract class PolarFireShell()(implicit p: Parameters) extends MicrosemiShell
{
  val initMonitor = InModuleBody { Module(new PolarFireInitMonitor) }
  val pllFactory = new PLLFactory(this, 7, p => Module(new PolarFireCCC(p)))
  override def designParameters = super.designParameters.alterPartial {
    case PLLFactoryKey => pllFactory
  }
}
