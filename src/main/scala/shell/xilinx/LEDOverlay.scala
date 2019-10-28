// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class LEDXilinxPlacedOverlay(name: String, di: LEDDesignInput, si: LEDShellInput, boardPin: Option[String] = None, packagePin: Option[String] = None, ioStandard: String = "LVCMOS33")
  extends LEDPlacedOverlay(name, di, si)
{
  def shell: XilinxShell

  shell { InModuleBody {
    io := ledSink.bundle // could/should put OBUFs here?

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
