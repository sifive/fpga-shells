// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi

import Chisel._
import chisel3.core.{Input, Output, attach}
import chisel3.experimental.{Analog}
import freechips.rocketchip.util.{ElaborationArtefacts}


//========================================================================
// This file contains common devices for Microsemi PolarFire FPGAs.
//========================================================================

//-------------------------------------------------------------------------
// Clock network macro
//-------------------------------------------------------------------------

class CLKINT() extends BlackBox
{
  val io = new Bundle{
    val A = Clock(INPUT)
    val Y = Clock(OUTPUT)
  }
}

class ICB_CLKINT() extends BlackBox
{
  val io = new Bundle{
    val A = Clock(INPUT)
    val Y = Clock(OUTPUT)
  }
}
