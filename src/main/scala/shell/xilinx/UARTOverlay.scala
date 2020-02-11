// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class UARTXilinxPlacedOverlay(name: String, di: UARTDesignInput, si: UARTShellInput, flowControl: Boolean)
  extends UARTPlacedOverlay(name, di, si, flowControl)
{
  def shell: XilinxShell

  shell { InModuleBody {
    UIntToAnalog(tluartSink.bundle.txd, io.txd, true.B)
    tluartSink.bundle.rxd := AnalogToUInt(io.rxd)
  } }
}
