// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class ButtonXilinxPlacedOverlay(name: String, di: ButtonDesignInput, si: ButtonShellInput, boardPins: Seq[String] = Nil, packagePins: Seq[String] = Nil, ioStandard: String = "LVCMOS33")
  extends ButtonPlacedOverlay(name, di, si)
{
  def shell: XilinxShell
  val width = boardPins.size + packagePins.size

  shell { InModuleBody {
    val but  = Wire(Bool())
    buttonSource.out(0)._1 := but
    val ibuf = Module(new IBUF)
    ibuf.suggestName(s"button_ibuf_${si.number}")
    ibuf.io.I := io
    but := ibuf.io.O

    val cutAt = boardPins.size
    val ios = IOPin.of(io)
    val boardIOs = ios.take(cutAt)
    val packageIOs = ios.drop(cutAt)

    (boardPins   zip boardIOs)   foreach { case (pin, io) => shell.xdc.addBoardPin  (io, pin) }
    (packagePins zip packageIOs) foreach { case (pin, io) =>
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, ioStandard)
    }
  } }
}
