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


class FPGASPIFlashPortIO extends Bundle {
  val qspi_sck  = Output(Bool())
  val qspi_cs   = Output(Bool())
  val qspi_dq_0 = Analog(1.W)
  val qspi_dq_1 = Analog(1.W)
  val qspi_dq_2 = Analog(1.W)
  val qspi_dq_3 = Analog(1.W)
}


class AnalogToUInt(w: Int = 1) extends BlackBox(Map("WIDTH" -> IntParam(w))) {
  val io = IO(new Bundle {
    val a = Analog(w.W)
    val b = Output(UInt(w.W))
  })
}

object AnalogToUInt {
  def apply(a: Analog): UInt = {
    val a2b = Module(new AnalogToUInt(w = a.getWidth))
    attach(a, a2b.io.a)
    a2b.io.b
  }
}

class UIntToAnalog(w: Int = 1) extends BlackBox(Map("WIDTH" -> IntParam(w))) {
  val io = IO(new Bundle {
    val a = Analog(w.W)
    val b = Input(UInt(w.W))
    val b_en = Input(Bool())
  })
}

object UIntToAnalog {
  def apply(b: UInt, a: Analog, b_en: Bool): Unit = {
    val a2b = Module(new UIntToAnalog(w = a.getWidth))
    attach(a, a2b.io.a)
    a2b.io.b := b
    a2b.io.b_en := b_en
  }
}


abstract class SPIFlashOverlay(
  val params: SPIFlashOverlayParams)
    extends IOOverlay[FPGASPIFlashPortIO, TLSPIFlash]
{
  implicit val p = params.p

  def ioFactory = new FPGASPIFlashPortIO
  val tlqspi = SPI.attachFlash(SPIFlashAttachParams(params.spiFlashParam, params.controlBus, params.memBus, params.intNode))

  val tlqspiSink = shell { tlqspi.ioNode.makeSink }
  val designOutput = tlqspi

  shell { InModuleBody {
    io.qspi_sck := tlqspiSink.bundle.sck
    io.qspi_cs  := tlqspiSink.bundle.cs(0)

    UIntToAnalog(tlqspiSink.bundle.dq(0).o, io.qspi_dq_0, tlqspiSink.bundle.dq(0).oe)
    UIntToAnalog(tlqspiSink.bundle.dq(1).o, io.qspi_dq_1, tlqspiSink.bundle.dq(1).oe)
    UIntToAnalog(tlqspiSink.bundle.dq(2).o, io.qspi_dq_2, tlqspiSink.bundle.dq(2).oe)
    UIntToAnalog(tlqspiSink.bundle.dq(3).o, io.qspi_dq_3, tlqspiSink.bundle.dq(3).oe)

    tlqspiSink.bundle.dq(0).i := AnalogToUInt(io.qspi_dq_0)
    tlqspiSink.bundle.dq(1).i := AnalogToUInt(io.qspi_dq_1)
    tlqspiSink.bundle.dq(2).i := AnalogToUInt(io.qspi_dq_2)
    tlqspiSink.bundle.dq(3).i := AnalogToUInt(io.qspi_dq_3)

  } }
}
