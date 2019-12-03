// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

 import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class PWMXilinxPlacedOverlay(name: String, di: PWMDesignInput, si: PWMShellInput)
  extends PWMPlacedOverlay(name, di, si)
{
  def shell: XilinxShell

  shell { InModuleBody {
    tlpwmSink.bundle.gpio.zip(io.pwm_gpio).foreach { case(design_pwm, io_pwm) =>
      UIntToAnalog(design_pwm, io_pwm, true.B)
    }
  } }
}
