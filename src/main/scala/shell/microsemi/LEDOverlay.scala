// See LICENSE for license details.
package sifive.fpgashells.shell.microsemi

 import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.microsemi._

 abstract class LEDMicrosemiPlacedOverlay(name: String, di: LEDDesignInput, si: LEDShellInput, pins: Seq[String] = Nil)
  extends LEDPlacedOverlay(name, di, si)
{
  def shell: MicrosemiShell
  val width = pins.size

   shell { InModuleBody {
    io := ledSink.bundle // could/should put OBUFs here?
    (pins zip IOPin.of(io)) foreach { case (pin, io) => shell.io_pdc.addPin(io, pin) }
  } }
}
