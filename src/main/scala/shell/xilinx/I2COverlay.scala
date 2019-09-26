// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

 import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

 abstract class I2CXilinxOverlay(params: I2COverlayParams)
  extends I2COverlay(params)
{
  def shell: XilinxShell

  shell { InModuleBody {
    UIntToAnalog(tli2cSink.bundle.scl.out, io.scl, tli2cSink.bundle.scl.oe)
    UIntToAnalog(tli2cSink.bundle.sda.out, io.sda, tli2cSink.bundle.sda.oe)

    tli2cSink.bundle.scl.in := AnalogToUInt(io.scl)
    tli2cSink.bundle.sda.in := AnalogToUInt(io.sda)
  } }
}
