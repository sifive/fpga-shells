// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class SwitchXilinxOverlay(params: SwitchOverlayParams, boardPins: Seq[String] = Nil, packagePins: Seq[String] = Nil)
  extends SwitchOverlay(params)
{
  def shell: XilinxShell
  val width = boardPins.size + packagePins.size

  shell { InModuleBody {
    val vec = Wire(Vec(width, Bool()))
    switchSource.out(0)._1 := vec.asUInt
    (vec zip io.toBools).zipWithIndex.foreach { case ((o, i), idx) =>
      val ibuf = Module(new IBUF)
      ibuf.suggestName(s"switch_ibuf_${idx}")
      ibuf.io.I := i
      o := ibuf.io.O
    }

    val cutAt = boardPins.size
    val ios = IOPin.of(io)
    val boardIOs = ios.take(cutAt)
    val packageIOs = ios.drop(cutAt)

    (boardPins   zip boardIOs)   foreach { case (pin, io) => shell.xdc.addBoardPin  (io, pin) }
    (packagePins zip packageIOs) foreach { case (pin, io) => shell.xdc.addPackagePin(io, pin) }
  } }
}
