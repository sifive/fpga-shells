// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.util.Cat
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class SPIFlashXilinxPlacedOverlay(name: String, di: SPIFlashDesignInput, si: SPIFlashShellInput)
  extends SPIFlashPlacedOverlay(name, di, si)
{
  def shell: XilinxShell

  //val dqiVec = VecInit.tabulate(4)(j =>tlqspiSink.bundle.dq(j))
  shell { InModuleBody {
    if (!si.vcu118SU) {
      UIntToAnalog(tlqspiSink.bundle.sck  , io.qspi_sck, true.B)
      UIntToAnalog(tlqspiSink.bundle.cs(0), io.qspi_cs , true.B)

      tlqspiSink.bundle.dq.zip(io.qspi_dq).foreach { case(design_dq, io_dq) => 
        UIntToAnalog(design_dq.o, io_dq, design_dq.oe)
        design_dq.i := AnalogToUInt(io_dq)
      }
    } else {
      // If on vcu118, to communicate with Flash, STARTUPE3 primitive needs to be connected and hooked uo tp 
      // spi, rather than a top level connection
      val se3 = Module(new STARTUPE3())
      se3.io.USRDONEO   := true.B
      se3.io.USRDONETS  := true.B
      se3.io.USRCCLKO   := tlqspiSink.bundle.sck.asClock
      se3.io.USRCCLKTS  := false.B
      se3.io.FCSBO      := tlqspiSink.bundle.cs(0)
      se3.io.FCSBTS     := false.B
      se3.io.DO         := Cat(tlqspiSink.bundle.dq.map(_.o))
      se3.io.DTS        := Cat(tlqspiSink.bundle.dq.map(_.oe))
      tlqspiSink.bundle.dq(0).i            := se3.io.DI(0)
      tlqspiSink.bundle.dq(1).i            := se3.io.DI(1)
      tlqspiSink.bundle.dq(2).i            := se3.io.DI(2)
      tlqspiSink.bundle.dq(3).i            := se3.io.DI(3)
      se3.io.GSR        := false.B
      se3.io.GTS        := false.B
      se3.io.KEYCLEARB  := false.B
      se3.io.PACK       := false.B
    }
  } }
}
