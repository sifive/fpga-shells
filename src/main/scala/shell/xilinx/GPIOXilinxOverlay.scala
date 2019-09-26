// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class GPIOXilinxOverlay(params: GPIOOverlayParams)
  extends GPIOOverlay(params)
{
  def shell: XilinxShell

  shell { InModuleBody {
      tlgpioSink.bundle.pins.zipWithIndex.foreach{ case (tlpin, idx) => {
        UIntToAnalog(tlpin.o.oval, io.gpio(idx), tlpin.o.oe)
        tlpin.i.ival := AnalogToUInt(io.gpio(idx))
      } }
  } }
}
