// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.spi._

case class SDIOOverlayParams(beatBytes: Int, spiParam: SPIParams, devName: Option[String])(implicit val p: Parameters)
case object SDIOOverlayKey extends Field[Seq[DesignOverlay[SDIOOverlayParams, TLSPI]]](Nil)

// SDIO Port. Not sure how generic this is, it might need to move.
class FPGASDIOPortIO extends Bundle {
  val sdio_clk = Output(Bool())
  val sdio_cmd = Output(Bool())
  val sdio_dat_0 = Input(Bool())
  val sdio_dat_1 = Analog(1.W)
  val sdio_dat_2 = Analog(1.W)
  val sdio_dat_3 = Output(Bool())
}

abstract class SDIOOverlay(
  val params: SDIOOverlayParams)
    extends IOOverlay[FPGASDIOPortIO, TLSPI]
{
  implicit val p = params.p

  def ioFactory = new FPGASDIOPortIO
  val tlspi = LazyModule(new TLSPI(params.beatBytes, params.spiParam).suggestName(params.devName))

  val spiSource = BundleBridgeSource(() => new SPIPortIO(params.spiParam))
  val spiSink = shell { spiSource.sink }
  val designOutput = tlspi

  InModuleBody {
    val (io, _) = spiSource.out(0)
    io <> tlspi.module.io.port
    (0 to 3).foreach { case q =>
      tlspi.module.io.port.dq(q).i := RegNext(RegNext(io.dq(q).i))
    }
  }

  shell { InModuleBody {
    val sd_spi_sck = spiSink.io.sck
    val sd_spi_cs = spiSink.io.cs(0)

    val sd_spi_dq_i = Wire(Vec(4, Bool()))
    val sd_spi_dq_o = Wire(Vec(4, Bool()))

    spiSink.io.dq.zipWithIndex.foreach {
      case(pin, idx) =>
        sd_spi_dq_o(idx) := pin.o
        pin.i := sd_spi_dq_i(idx)
    }

    io.sdio_clk := sd_spi_sck
    io.sdio_dat_3 := sd_spi_cs
    io.sdio_cmd := sd_spi_dq_o(0)
    sd_spi_dq_i := Seq(false.B, io.sdio_dat_0, false.B, false.B)
  } }
}
