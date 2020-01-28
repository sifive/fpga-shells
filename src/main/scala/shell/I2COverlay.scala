// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.i2c._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode

//This should NOT do the device placement, just return the bundlebridge
case class I2CShellInput(index: Int = 0)
case class I2CDesignInput(i2cParams: I2CParams, controlBus: TLBusWrapper, intNode: IntInwardNode)(implicit val p: Parameters)
case class I2COverlayOutput(i2c: TLI2C)
trait I2CShellPlacer[Shell] extends ShellPlacer[I2CDesignInput, I2CShellInput, I2COverlayOutput]

case object I2COverlayKey extends Field[Seq[DesignPlacer[I2CDesignInput, I2CShellInput, I2COverlayOutput]]](Nil)

class ShellI2CPortIO extends Bundle {
  val scl = Analog(1.W)
  val sda = Analog(1.W)
}

abstract class I2CPlacedOverlay(
  val name: String, val di: I2CDesignInput, val si: I2CShellInput)
    extends IOPlacedOverlay[ShellI2CPortIO, I2CDesignInput, I2CShellInput, I2COverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new ShellI2CPortIO

  val tli2c = I2C.attach(I2CAttachParams(di.i2cParams, di.controlBus, di.intNode))
  val tli2cSink = shell { tli2c.ioNode.makeSink }

  def overlayOutput = I2COverlayOutput(i2c = tli2c)
}
