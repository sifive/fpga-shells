// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class LVDSClockInputXilinxOverlay(params: ClockInputOverlayParams)
  extends LVDSClockInputOverlay(params)
{
  def shell: XilinxShell

  shell { InModuleBody {
    val ibufds = Module(new IBUFDS)
    ibufds.suggestName(s"${name}_ibufds")

    val (c, _) = node.out(0)
    ibufds.io.I  := io.p
    ibufds.io.IB := io.n
    c.clock := ibufds.io.O
    c.reset := shell.pllReset
  } }
}
