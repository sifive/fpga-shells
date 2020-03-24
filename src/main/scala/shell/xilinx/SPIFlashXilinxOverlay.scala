// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class SPIFlashXilinxPlacedOverlay(name: String, di: SPIFlashDesignInput, si: SPIFlashShellInput)
  extends SPIFlashPlacedOverlay(name, di, si)
{
  def shell: XilinxShell

  shell { InModuleBody {
    UIntToAnalog(tlqspiSink.bundle.sck  , io.qspi_sck, true.B)
    UIntToAnalog(tlqspiSink.bundle.cs(0), io.qspi_cs , true.B)

    tlqspiSink.bundle.dq.zip(io.qspi_dq).foreach { case(design_dq, io_dq) => 
      UIntToAnalog(design_dq.o, io_dq, design_dq.oe)
      design_dq.i := AnalogToUInt(io_dq)
    }
  } }
}
