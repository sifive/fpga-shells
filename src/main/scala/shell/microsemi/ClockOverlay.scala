// See LICENSE for license details.
package sifive.fpgashells.shell.microsemi

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.microsemi._

abstract class ClockInputMicrosemiOverlay(params: ClockInputOverlayParams)
  extends ClockInputOverlay(params)
{
  def shell: MicrosemiShell

  val reset = shell { InModuleBody {
    val (c, _) = node.out(0)
    c.clock := io
    c.reset := false.B

    PowerOnResetFPGAOnly(io)
  } }
}
