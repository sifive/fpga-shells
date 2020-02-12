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

//another one that makes the controller... remove this
case class PWMShellInput(index: Int = 0)
case class PWMDesignInput(pwmParams: PWMParams)(implicit val p: Parameters)
case class PWMOverlayOutput(pwm: ModuleValue[PWMPortIO])
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

  val tlpwmSource = BundleBridgeSource(() => new PWMPortIO(di.pwmParams))
  val tlpwmSink = shell { tlpwmSource.makeSink }

  def overlayOutput = PWMOverlayOutput(pwm = InModuleBody{ tlpwmSource.bundle })
}
