// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class ChipLinkXilinxOverlay(params: ChipLinkOverlayParams)
  extends ChipLinkOverlay(params, rxPhase=280, txPhase=220)
{
  def shell: XilinxShell

  shell { InModuleBody {
    val (tx, _) = txClock.in(0)
    val (tap, _) = txTap.out(0)
    val rxEdge = rxI.edges.out(0)

    val oddr = Module(new ODDR())
    oddr.suggestName(s"${name}_tx_oddr")
    io.c2b.clk := oddr.io.Q.asClock
    oddr.io.C  := tx.clock
    oddr.io.CE := true.B
    oddr.io.D1 := true.B
    oddr.io.D2 := false.B
    oddr.io.S  := false.B
    // We can't use tx.reset here as it waits for all PLLs to lock,
    // including RX, which depends on this clock being driven.
    // tap.reset only waits for the TX PLL to lock.
    oddr.io.R  := ResetCatchAndSync(tx.clock, tap.reset)

    IOPin.of(io).foreach { shell.xdc.addIOStandard(_, "LVCMOS18") }
    IOPin.of(io).filterNot(_.element eq io.b2c.clk).foreach { shell.xdc.addIOB(_) }
    IOPin.of(io).filter(_.isOutput).foreach { shell.xdc.addSlew(_, "FAST") }

    val timing = IOTiming(
      /* The data signals coming from Aloe have: clock - 1.2 <= transition <= clock + 0.8
       * Let's add 0.3ns of safety for trace jitter+skew on both sides:
       *   min = hold           = - 1.2 - 0.3
       *   max = period - setup =   0.8 + 0.3
       */
      minInput  = -1.5,
      maxInput  =  1.1,
      /* The data signals going to Aloe must have: clock - 1.85 <= NO transition <= clock + 0.65
       * Let's add 0.3ns of safey for trace jitter+skew on both sides:
       *   min = -hold = -0.65 - 0.3
       *   max = setup =  1.85 + 0.3
       */
      minOutput = -0.95,
      maxOutput =  2.15)

    shell.sdc.addClock(s"${name}_b2c_clock", io.b2c.clk, rxEdge.clock.freqMHz, 0.3)
    shell.sdc.addDerivedClock(s"${name}_c2b_clock", oddr.io.C, io.c2b.clk)
    IOPin.of(io).filter(p => p.isInput  && !(p.element eq io.b2c.clk)).foreach { e =>
      shell.sdc.addIOTiming(e, s"${name}_b2c_clock", timing)
    }
    IOPin.of(io).filter(p => p.isOutput && !(p.element eq io.c2b.clk)).foreach { e =>
      shell.sdc.addIOTiming(e, s"${name}_c2b_clock", timing)
    }
  } }
}
