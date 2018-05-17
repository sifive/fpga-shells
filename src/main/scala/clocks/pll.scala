// See LICENSE for license details.
package sifive.fpgashells.ip.clocks

import Chisel._
import chisel3.core.{Input, Output, attach}
import chisel3.experimental.{Analog}


//========================================================================
// This file contains PLL parameters for Xilinx FPGAs
//========================================================================

case class OutClockParameters(
        freqMHz:      Double ,
        phaseDeg:  Double = 0,
        dutyCycle: Double = 50, //in percent   
        freqPPM: Double = 10000, 
        //phasePPM: Double = 50000 //e.g default value result in error of 9 degrees for phase of 180 degrees
        jitter: Double = 0,
        phaseErrorDeg: Double = 0
        )

case class InClockParameters(
        freqMHz:  Double ,
        freqPPM: Double = 10000, 
        jitter: Double = 50, 
        feedback:         Boolean = false
        )


case class PLLParameters( 
        name: String,
        input: InClockParameters,
        req: Seq[OutClockParameters]
        )
