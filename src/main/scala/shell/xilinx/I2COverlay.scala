// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

 import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

 abstract class I2CXilinxPlacedOverlay(name: String, di: I2CDesignInput, si: I2CShellInput)
  extends I2CPlacedOverlay(name, di, si)
{
  def shell: XilinxShell

  shell { InModuleBody {
    UIntToAnalog(tli2cSink.bundle.scl.out, io.scl, tli2cSink.bundle.scl.oe)
    UIntToAnalog(tli2cSink.bundle.sda.out, io.sda, tli2cSink.bundle.sda.oe)

    tli2cSink.bundle.scl.in := AnalogToUInt(io.scl)
    tli2cSink.bundle.sda.in := AnalogToUInt(io.sda)
  } }
}
