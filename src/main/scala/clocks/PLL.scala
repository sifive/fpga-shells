// See LICENSE for license details.
package sifive.fpgashells.clocks

import Chisel._
import chisel3.core.{Input, Output, attach}
import chisel3.experimental.{Analog}


//========================================================================
// This file contains PLL parameters
//========================================================================
case class InClockParameters(
        freqMHz:  Double,
        jitter: Double = 50, 
        feedback:         Boolean = false
        )

case class OutClockParameters(
        freqMHz:      Double ,
        phaseDeg:  Double = 0,
        dutyCycle: Double = 50, //in percent   
        freqErrorPPM: Double = 10000, 
        phaseErrorDeg: Double = 0
        )

case class PLLParameters( 
        name: String,
        input: InClockParameters,
        req: Seq[OutClockParameters]
        )

trait PLL {
  def getClocks: Seq[Clock]
  def getLocked: Bool
  def getClockNames: Seq[String]
}
