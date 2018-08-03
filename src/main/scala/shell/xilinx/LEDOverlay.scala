// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class LEDXilinxOverlay(params: LEDOverlayParams, boardPins: Seq[String] = Nil, packagePins: Seq[String] = Nil)
  extends LEDOverlay(params)
{
  def shell: XilinxShell
  val width = boardPins.size + packagePins.size

  shell { InModuleBody {
    io := ledSink.bundle // could/should put OBUFs here?

    val cutAt = boardPins.size
    val ios = IOPin.of(io)
    val boardIOs = ios.take(cutAt)
    val packageIOs = ios.drop(cutAt)

    (boardPins   zip boardIOs)   foreach { case (pin, io) => shell.xdc.addBoardPin  (io, pin) }
    (packagePins zip packageIOs) foreach { case (pin, io) => shell.xdc.addPackagePin(io, pin) }
  } }
}
