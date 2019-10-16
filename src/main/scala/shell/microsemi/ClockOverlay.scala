// See LICENSE for license details.
package sifive.fpgashells.shell.microsemi

 import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.microsemi._

 abstract class ClockInputMicrosemiPlacedOverlay(name: String, di: ClockInputDesignInput, si: ClockInputShellInput)
  extends SingleEndedClockInputPlacedOverlay(name, di, si)
{
  def shell: MicrosemiShell

   shell { InModuleBody {
    val (c, _) = node.out(0)
    val clkint = Module(new CLKINT)
    clkint.suggestName(s"${name}_clkint")

     clkint.io.A := io
    c.clock := clkint.io.Y
    c.reset := false.B
  } }
}
