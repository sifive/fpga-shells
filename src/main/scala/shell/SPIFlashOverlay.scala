// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.spi._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import chisel3.experimental._

case class SPIFlashOverlayParams(spiFlashParam: SPIFlashParams, controlBus: TLBusWrapper, memBus: TLBusWrapper, intNode: IntInwardNode)(implicit val p: Parameters)
case object SPIFlashOverlayKey extends Field[Seq[DesignOverlay[SPIFlashOverlayParams, TLSPIFlash]]](Nil)


class ShellSPIFlashPortIO extends Bundle {
  val qspi_sck = Analog(1.W)
  val qspi_cs  = Analog(1.W)
  val qspi_dq  = Vec(4, Analog(1.W))
}

abstract class SPIFlashOverlay(
  val params: SPIFlashOverlayParams)
    extends IOOverlay[ShellSPIFlashPortIO, TLSPIFlash]
{
  implicit val p = params.p

  def ioFactory = new ShellSPIFlashPortIO
  val tlqspi = SPI.attachFlash(SPIFlashAttachParams(params.spiFlashParam, params.controlBus, params.memBus, params.intNode))

  val tlqspiSink = shell { tlqspi.ioNode.makeSink }
  val designOutput = tlqspi
}
