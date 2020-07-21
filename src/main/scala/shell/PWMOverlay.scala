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

case class PWMShellInput(index: Int = 0)
case class PWMDesignInput(node: BundleBridgeSource[PWMPortIO])(implicit val p: Parameters)
case class PWMOverlayOutput()
case object PWMOverlayKey extends Field[Seq[DesignPlacer[PWMDesignInput, PWMShellInput, PWMOverlayOutput]]](Nil)
trait PWMShellPlacer[Shell] extends ShellPlacer[PWMDesignInput, PWMShellInput, PWMOverlayOutput]

class ShellPWMPortIO extends Bundle {
  val pwm_gpio = Vec(4, Analog(1.W))
}

abstract class PWMPlacedOverlay(
  val name: String, val di: PWMDesignInput, val si: PWMShellInput)
    extends IOPlacedOverlay[ShellPWMPortIO, PWMDesignInput, PWMShellInput, PWMOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new ShellPWMPortIO

  val tlpwmSink = sinkScope { di.node.makeSink }

  def overlayOutput = PWMOverlayOutput()
}
