// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class ButtonXilinxPlacedOverlay(name: String, di: ButtonDesignInput, si: ButtonShellInput, boardPin: Option[String] = None, packagePin: Option[String] = None, ioStandard: String = "LVCMOS33")
  extends ButtonPlacedOverlay(name, di, si)
{
  def shell: XilinxShell

  shell { InModuleBody {
    val but  = Wire(Bool())
    buttonSource.out(0)._1 := but
    val ibuf = Module(new IBUF)
    ibuf.suggestName(s"button_ibuf_${si.number}")
    ibuf.io.I := io
    but := ibuf.io.O

    require((boardPin.isEmpty || packagePin.isEmpty), "can't provide both boardpin and packagepin, this is ambiguous")
    val cutAt = if(boardPin.isDefined) 1 else 0
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
