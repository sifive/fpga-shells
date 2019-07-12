// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.i2c._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode

case class I2COverlayParams(i2cParams: I2CParams, controlBus: TLBusWrapper, intNode: IntInwardNode)(implicit val p: Parameters)
case object I2COverlayKey extends Field[Seq[DesignOverlay[I2COverlayParams, TLI2C]]](Nil)

class FPGAI2CPortIO extends Bundle {
  val scl = Analog(1.W)
  val sda = Analog(1.W)
}

abstract class I2COverlay(
  val params: I2COverlayParams)
    extends IOOverlay[FPGAI2CPortIO, TLI2C]
{
  implicit val p = params.p

  def ioFactory = new FPGAI2CPortIO

  val tli2c = I2C.attach(I2CAttachParams(params.i2cParams, params.controlBus, params.intNode))
  val tli2cSink = shell { tli2c.ioNode.makeSink }

  val designOutput = tli2c

  shell { InModuleBody {
    UIntToAnalog(tli2cSink.bundle.scl.out, io.scl, tli2cSink.bundle.scl.oe)
    UIntToAnalog(tli2cSink.bundle.sda.out, io.sda, tli2cSink.bundle.sda.oe)

    tli2cSink.bundle.scl.in := AnalogToUInt(io.scl)
    tli2cSink.bundle.sda.in := AnalogToUInt(io.sda)
  } }
}
