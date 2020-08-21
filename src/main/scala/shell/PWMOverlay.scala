// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog

import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.LogicalTreeNode

import sifive.blocks.devices.pwm._
import sifive.fpgashells.shell.xilinx._

case class PWMShellInput(index: Int = 0) extends ShellInput
case class PWMDesignInput(node: BundleBridgeSource[PWMPortIO])(implicit val p: Parameters)
case class PWMOverlayOutput() extends OverlayOutput
case object PWMOverlayKey extends Field[Seq[TestDesignPlacer[DesignInput, PWMShellInput, PWMOverlayOutput]]](Nil)
trait PWMShellPlacer[Shell] extends ShellPlacer[DesignInput, PWMShellInput, PWMOverlayOutput]

class ShellPWMPortIO extends Bundle {
  val pwm_gpio = Vec(4, Analog(1.W))
}

abstract class PWMPlacedOverlay(
  val name: String, val di: DesignInput, val si: PWMShellInput)
    extends IOPlacedOverlay[ShellPWMPortIO, DesignInput, PWMShellInput, PWMOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new ShellPWMPortIO

  val tlpwmSink = shell { di.node.asInstanceOf[BundleBridgeSource[PWMPortIO]].makeSink }

  def overlayOutput = PWMOverlayOutput()
}
