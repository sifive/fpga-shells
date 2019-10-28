// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.spi._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode

//This should not do the controller placement either
case class SDIOShellInput()
case class SDIODesignInput(spiParam: SPIParams, controlBus: TLBusWrapper, intNode: IntInwardNode)(implicit val p: Parameters)
case class SDIOOverlayOutput(spi: TLSPI)
case object SDIOOverlayKey extends Field[Seq[DesignPlacer[SDIODesignInput, SDIOShellInput, SDIOOverlayOutput]]](Nil)
trait SDIOShellPlacer[Shell] extends ShellPlacer[SDIODesignInput, SDIOShellInput, SDIOOverlayOutput]

// SDIO Port. Not sure how generic this is, it might need to move.
class FPGASDIOPortIO extends Bundle {
  val sdio_clk = Output(Bool())
  val sdio_cmd = Output(Bool())
  val sdio_dat_0 = Input(Bool())
  val sdio_dat_1 = Analog(1.W)
  val sdio_dat_2 = Analog(1.W)
  val sdio_dat_3 = Output(Bool())
}

abstract class SDIOPlacedOverlay(
  val name: String, val di: SDIODesignInput, val si: SDIOShellInput)
    extends IOPlacedOverlay[FPGASDIOPortIO, SDIODesignInput, SDIOShellInput, SDIOOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new FPGASDIOPortIO
  val tlspi = SPI.attach(SPIAttachParams(di.spiParam, di.controlBus, di.intNode))
  val tlspiSink = tlspi.ioNode.makeSink

  val spiSource = BundleBridgeSource(() => new SPIPortIO(di.spiParam))
  val spiSink = shell { spiSource.makeSink }
  def overlayOutput = SDIOOverlayOutput(spi = tlspi)

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

    io.sdio_clk := sd_spi_sck
    io.sdio_dat_3 := sd_spi_cs
    io.sdio_cmd := sd_spi_dq_o(0)
    sd_spi_dq_i := Seq(false.B, io.sdio_dat_0, false.B, false.B)
  } }
}
