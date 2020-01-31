// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog

import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.LogicalTreeNode

import sifive.blocks.devices.spi._

//This one does controller also
case class SPIFlashShellInput(index: Int = 0)
case class SPIFlashDesignInput(
  spiFlashParam: SPIFlashParams,
  controlBus: TLBusWrapper,
  memBus: TLBusWrapper,
  intNode: IntInwardNode,
  parentLogicalTreeNode: Option[LogicalTreeNode])(implicit val p: Parameters)
case class SPIFlashOverlayOutput(spiflash: TLSPIFlash)
case object SPIFlashOverlayKey extends Field[Seq[DesignPlacer[SPIFlashDesignInput, SPIFlashShellInput, SPIFlashOverlayOutput]]](Nil)
trait SPIFlashShellPlacer[Shell] extends ShellPlacer[SPIFlashDesignInput, SPIFlashShellInput, SPIFlashOverlayOutput]


class ShellSPIFlashPortIO extends Bundle {
  val qspi_sck = Analog(1.W)
  val qspi_cs  = Analog(1.W)
  val qspi_dq  = Vec(4, Analog(1.W))
}

abstract class SPIFlashPlacedOverlay(
  val name: String, val di: SPIFlashDesignInput, val si: SPIFlashShellInput)
    extends IOPlacedOverlay[ShellSPIFlashPortIO, SPIFlashDesignInput, SPIFlashShellInput, SPIFlashOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new ShellSPIFlashPortIO
  val tlqspi = SPI.attachFlash(SPIFlashAttachParams(
    spi = di.spiFlashParam,
    controlBus = di.controlBus,
    memBus = di.memBus,
    intNode = di.intNode,
    parentLogicalTreeNode = di.parentLogicalTreeNode))

  val tlqspiSink = shell { tlqspi.ioNode.makeSink }
  def overlayOutput = SPIFlashOverlayOutput(spiflash = tlqspi)
}
