// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import freechips.rocketchip.tilelink._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.clocks._
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
    oddr.io.C  := tx.clock.asUInt
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

class ChipLinkVC707Overlay(val shell: VC707Shell, val name: String, params: ChipLinkOverlayParams)
  extends ChipLinkXilinxOverlay(params)
{
  shell { InModuleBody {
    val dir1 = Seq("AF39", "AJ41", "AJ40", /* clk, rst, send */
                   "AD40", "AD41", "AF41", "AG41", "AK39", "AL39", "AJ42", "AK42",
                   "AL41", "AL42", "AF42", "AG42", "AD38", "AE38", "AC40", "AC41",
                   "AD42", "AE42", "AJ38", "AK38", "AB41", "AB42", "Y42",  "AA42",
                   "Y39",  "AA39", "W40",  "Y40",  "AB38", "AB39", "AC38", "AC39")
    val dir2 = Seq("U39", "R37", "T36", /* clk, rst, send */
                   "U37", "U38", "U36", "T37", "U32", "U33", "V33", "V34",
                   "P35", "P36", "W32", "W33", "R38", "R39", "U34", "T35",
                   "R33", "R34", "N33", "N34", "P32", "P33", "V35", "V36",
                   "W36", "W37", "T32", "R32", "V39", "V40", "P37", "P38")
    val dirB2C = Seq(IOPin(io.b2c.clk), IOPin(io.b2c.rst), IOPin(io.b2c.send)) ++
                 IOPin.of(io.b2c.data)
    val dirC2B = Seq(IOPin(io.c2b.clk), IOPin(io.c2b.rst), IOPin(io.c2b.send)) ++
                 IOPin.of(io.c2b.data)
    (dirB2C zip dir1) foreach { case (io, pin) => shell.setPackagePin(io, pin) }
    (dirC2B zip dir2) foreach { case (io, pin) => shell.setPackagePin(io, pin) }

    val (rxIn, _) = rxI.out(0)
    rxIn.reset := shell.sysClock.out(0)._1.reset
  } }
}
