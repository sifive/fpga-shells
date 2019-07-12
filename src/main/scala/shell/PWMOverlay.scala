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

class FPGAPWMPortIO extends Bundle {
  val pwm_gpio_0 = Output(Bool())
  val pwm_gpio_1 = Output(Bool())
  val pwm_gpio_2 = Output(Bool())
  val pwm_gpio_3 = Output(Bool())
}

abstract class PWMOverlay(
  val params: PWMOverlayParams)
    extends IOOverlay[FPGAPWMPortIO, TLPWM]
{
  implicit val p = params.p

  def ioFactory = new FPGAPWMPortIO

  val tlpwm = PWM.attach(PWMAttachParams(params.pwmParams, params.controlBus, params.intNode))
  val tlpwmSink = shell { tlpwm.ioNode.makeSink }

  val designOutput = tlpwm

  shell { InModuleBody {
    io.pwm_gpio_0 := tlpwmSink.bundle.gpio(0)
    io.pwm_gpio_1 := tlpwmSink.bundle.gpio(1)
    io.pwm_gpio_2 := tlpwmSink.bundle.gpio(2)
    io.pwm_gpio_3 := tlpwmSink.bundle.gpio(3)
  } }
}
