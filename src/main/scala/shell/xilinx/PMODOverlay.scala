// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class PMODXilinxPlacedOverlay(name: String, di: PMODDesignInput, si: PMODShellInput, boardPin: Seq[String] = Seq(), packagePin: Seq[String] = Seq(), ioStandard: String = "LVCMOS33")
  extends PMODPlacedOverlay(name, di, si)
{
  def shell: XilinxShell

  shell { InModuleBody {
    require((boardPin.isEmpty || packagePin.isEmpty), "can't provide both boardpin and packagepin, this is ambiguous")
    val cutAt = boardPin.length
    val ios = IOPin.of(io)
    val boardIO = ios.take(cutAt)
    val packageIO = ios.drop(cutAt)

    (boardPin   zip boardIO)   foreach { case (pin, io) => shell.xdc.addBoardPin  (io, pin) }
    (packagePin zip packageIO) foreach { case (pin, io) =>
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, ioStandard)
    }
  } }
}
