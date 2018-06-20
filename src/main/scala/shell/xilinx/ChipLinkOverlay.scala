// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._

abstract class ChipLinkXilinxOverlay(params: ChipLinkOverlayParams)
  extends ChipLinkOverlay(params, rxPhase=270, txPhase=210)
{
  def shell: XilinxShell

  shell { InModuleBody {
    val (tx, _) = txClock.in(0)
    val rxEdge = rxI.edges.out(0)

    val oddr = Module(new ODDR())
    oddr.suggestName(s"${name}_tx_oddr")
    io.c2b.clk := oddr.io.Q.asClock
    oddr.io.C  := tx.clock
    oddr.io.CE := true.B
    oddr.io.D1 := true.B
    oddr.io.D2 := false.B
    oddr.io.R  := tx.reset
    oddr.io.S  := false.B

    IOPin.of(io).foreach { shell.setIOStandard(_, "LVCMOS18") }
    IOPin.of(io).filterNot(_.element eq io.b2c.clk).foreach { shell.setIOB(_) }
    IOPin.of(io).filter(_.isOutput).foreach { shell.setSlew(_, "FAST") }
    IOPin.of(io).filter(_.isInput).foreach { shell.setTermination(_, "NONE") }

    val timing = IOTiming(
      /* The data signals coming from Aloe have: clock - 1.2 <= transition <= clock + 0.8
       * Let's add 0.6ns of safety for trace jitter+skew on both sides:
       *   min = hold           = - 1.2 - 0.6
       *   max = period - setup =   0.8 + 0.6
       */
      minInput  = -1.8,
      maxInput  =  1.4,
      /* The data signals going to Aloe must have: clock - 1.85 <= NO transition <= clock + 0.65
       * Let's add 0.6ns of safey for trace jitter+skew on both sides:
       *   min = -hold = -0.65 - 0.6
       *   max = setup =  1.85 + 0.6
       */
      minOutput = -1.25,
      maxOutput =  2.45)

    shell.addConstraint(s"create_clock -name ${name}_b2c_clock -period ${1000/rxEdge.clock.freqMHz} ${shell.portOf(io.b2c.clk)}")
    shell.addConstraint(s"create_generated_clock -name ${name}_c2b_clock -divide_by 1 -source [ get_pins {${name}_tx_oddr/C} ] ${shell.portOf(io.c2b.clk)}")
    IOPin.of(io).filter(p => p.isInput  && !(p.element eq io.b2c.clk)).foreach { e =>
      shell.setIOTiming(e, s"${name}_b2c_clock", timing)
    }
    IOPin.of(io).filter(p => p.isOutput && !(p.element eq io.c2b.clk)).foreach { e =>
      shell.setIOTiming(e, s"${name}_c2b_clock", timing)
    }
  } }
}
