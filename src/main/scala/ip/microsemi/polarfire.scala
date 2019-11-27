// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi

import Chisel._
import chisel3.{Input, Output}
import chisel3.experimental.{Analog, attach}
import freechips.rocketchip.util.{ElaborationArtefacts}


//========================================================================
// This file contains common devices for Microsemi PolarFire FPGAs.
//========================================================================

//-------------------------------------------------------------------------
// Clock network macro
//-------------------------------------------------------------------------

class CLKBUF() extends BlackBox
{
  val io = new Bundle{
    val PAD = Clock(INPUT)
    val Y = Clock(OUTPUT)
  }
}

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
