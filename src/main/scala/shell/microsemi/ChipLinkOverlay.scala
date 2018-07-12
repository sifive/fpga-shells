// See LICENSE for license details.
package sifive.fpgashells.shell.microsemi

import chisel3._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.microsemi._

class HackyHelper extends Module {
  val io = IO(new Bundle {
    val in = Input(Clock())
    val out = Output(Clock())
  })
  io.out := io.in
}

abstract class ChipLinkMicrosemiOverlay(params: ChipLinkOverlayParams)
  extends ChipLinkOverlay(params, rxPhase=240, txPhase=31.5)
{
  def shell: MicrosemiShell

  shell { InModuleBody {
    val (tx, _) = txClock.in(0)
    val (tap, _) = txTap.out(0)
    val rxEdge = rxI.edges.out(0)

    // !!! add back an ODDR
    val helper = Module(new HackyHelper)
    helper.io.in := tx.clock
    io.c2b.clk := helper.io.out

    val periodNs = 1000.0 / rxEdge.clock.freqMHz
    val timing = IOTiming(
      /* The data signals coming from Aloe have: clock - 1.2 <= transition <= clock + 0.8
       * Let's add 0.3ns of safety for trace jitter+skew on both sides:
       *   min = hold           = - 1.2 - 0.3
       *   max = period - setup =   0.8 + 0.3
       * !!! We have to add a period+5 to work around some Libero bug !!!
       */
      minInput  = -1.5 + 5 + periodNs,
      maxInput  =  1.1 + 5 + periodNs,
      /* The data signals going to Aloe must have: clock - 1.85 <= NO transition <= clock + 0.65
       * Let's add 0.3ns of safey for trace jitter+skew on both sides:
       *   min = -hold = -0.65 - 0.3
       *   max = setup =  1.85 + 0.3
       */
      minOutput = -0.95,
      maxOutput =  2.15)

    shell.sdc.addClock(s"${name}_b2c_clock", io.b2c.clk, rxEdge.clock.freqMHz)
    shell.sdc.addDerivedClock(s"${name}_c2b_clock", "rxPLL/rxPLL_0/pll_inst_0/OUT0", io.c2b.clk)
    IOPin.of(io).filter(p => p.isInput  && !(p.element eq io.b2c.clk)).foreach { e =>
      shell.sdc.addIOTiming(e, s"${name}_b2c_clock", timing)
    }
    IOPin.of(io).filter(p => p.isOutput && !(p.element eq io.c2b.clk)).foreach { e =>
      shell.sdc.addIOTiming(e, s"${name}_c2b_clock", timing)
    }
  } }
}
