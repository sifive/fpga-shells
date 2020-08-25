// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class SDIOXilinxPlacedOverlay(name: String, di: SPIDesignInput, si: SPIShellInput)
  extends SPIPlacedOverlay(name, di, si)
{
  def shell: XilinxShell

  InModuleBody {
    val (io, _) = spiSource.out(0)
    val tlspiport = tlspiSink.bundle
    io <> tlspiport
    (0 to 3).foreach { case q =>
      tlspiport.dq(q).i := RegNext(RegNext(io.dq(q).i))
    }
  }

  shell { InModuleBody {
    val sd_spi_sck = spiSink.bundle.sck
    val sd_spi_cs = spiSink.bundle.cs(0)

    val sd_spi_dq_i = Wire(Vec(4, Bool()))
    val sd_spi_dq_o = Wire(Vec(4, Bool()))

    spiSink.bundle.dq.zipWithIndex.foreach {
      case(pin, idx) =>
        sd_spi_dq_o(idx) := pin.o
        pin.i := sd_spi_dq_i(idx)
    }

    UIntToAnalog(sd_spi_sck, io.spi_clk, true.B)
    UIntToAnalog(sd_spi_cs, io.spi_dat(3), true.B)
    UIntToAnalog(sd_spi_dq_o(0), io.spi_cs, true.B)
    sd_spi_dq_i := Seq(false.B, AnalogToUInt(io.spi_dat(0)).asBool, false.B, false.B)
  } }
}
