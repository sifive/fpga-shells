// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class JTAGDebugXilinxOverlay(params: JTAGDebugOverlayParams)
  extends JTAGDebugOverlay(params)
{
  def shell: XilinxShell

  shell { InModuleBody {
    jtagDebugSink.bundle.TCK := AnalogToUInt(io.jtag_TCK).asBool.asClock
    jtagDebugSink.bundle.TMS := AnalogToUInt(io.jtag_TMS)
    jtagDebugSink.bundle.TDI := AnalogToUInt(io.jtag_TDI)
    UIntToAnalog(jtagDebugSink.bundle.TDO.data,io.jtag_TDO,jtagDebugSink.bundle.TDO.driven)
  } }
}
