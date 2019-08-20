// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.pwm._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode

case class PWMOverlayParams(pwmParams: PWMParams, controlBus: TLBusWrapper, intNode: IntInwardNode)(implicit val p: Parameters)
case object PWMOverlayKey extends Field[Seq[DesignOverlay[PWMOverlayParams, TLPWM]]](Nil)

class ShellPWMPortIO extends Bundle {
  val pwm_gpio = Vec(4, Analog(1.W))
}

abstract class PWMOverlay(
  val params: PWMOverlayParams)
    extends IOOverlay[ShellPWMPortIO, TLPWM]
{
  implicit val p = params.p

  def ioFactory = new ShellPWMPortIO

  val tlpwm = PWM.attach(PWMAttachParams(params.pwmParams, params.controlBus, params.intNode))
  val tlpwmSink = shell { tlpwm.ioNode.makeSink }

  val designOutput = tlpwm
}
