// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.clocks._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.shell._

abstract class XilinxShell()(implicit p: Parameters) extends Shell
{
  def setBoardPin(io: IOPin, pin: String) {
    addConstraint(s"set_property BOARD_PIN {${pin}} ${portOf(io)}")
  }
  def setPackagePin(io: IOPin, pin: String) {
    addConstraint(s"set_property PACKAGE_PIN ${pin} ${portOf(io)}")
  }
  def setIOStandard(io: IOPin, standard: String) {
    addConstraint(s"set_property IOSTANDARD ${standard} ${portOf(io)}")
  }
  def setIOB(io: IOPin) {
    if (io.isOutput) {
      addConstraint(s"set_property IOB TRUE [ get_cells -of_objects [ all_fanin -flat -startpoints_only ${portOf(io)} ] ]")
    } else {
      addConstraint(s"set_property IOB TRUE [ get_cells -of_objects [ all_fanout -flat -endpoints_only ${portOf(io)} ] ]")
    }
  }
  def setSlew(io: IOPin, speed: String) {
    addConstraint(s"set_property SLEW ${speed} ${portOf(io)}")
  }
  def setTermination(io: IOPin, kind: String) {
    addConstraint(s"set_property OFFCHIP_TERM ${kind} ${portOf(io)}")
  }

  def bindLVDSInputClock(io: LVDSClock, resetio: Bool, clock: ClockSourceNode) = {
    val ibufds = Module(new IBUFDS)
    val (c, edge) = clock.out(0)
    ibufds.io.I  := io.p.asUInt
    ibufds.io.IB := io.n.asUInt
    c.clock := ibufds.io.O.asClock
    c.reset := IBUF(resetio)
    addConstraint(s"create_clock -name clock_${io.p.name} -period ${1000/edge.clock.freqMHz} ${portOf(io.p)}")
    addConstraint(s"set_input_jitter ${clockOf(io.p)} 0.5")
  }
}

abstract class Series7Shell()(implicit p: Parameters) extends XilinxShell
{
  val pllFactory = new PLLFactory(this, 7, p => Module(new Series7MMCM(p)))
}

class VC707Shell()(implicit p: Parameters) extends Series7Shell
{
  val sysClock = ClockSourceNode(freqMHz = 200, jitterPS = 50)
  val chiplinkFMC = Overlay { x: ChipLinkOverlayParams => new ChipLinkVC707Overlay(this, x) }
  val migDDR      = Overlay { x: DDROverlayParams      => new DDRVC707Overlay     (this, x) }
  val pcieFMC     = Overlay { x: PCIeOverlayParams     => new PCIeVC707Overlay    (this, x) }

  val topDesign = LazyModule(p(DesignKey)(p.alterPartial {
    case PLLFactoryKey      => pllFactory
    case ClockInputKey      => Seq(sysClock)
    case ChipLinkOverlayKey => Seq(chiplinkFMC)
    case DDROverlayKey      => Seq(migDDR)
    case PCIeOverlayKey     => Seq(pcieFMC)
  }))

  lazy val module = new LazyRawModuleImp(this) {
    val sys_diff_clock_clk = IO(Input(new LVDSClock))
    val reset              = IO(Input(Bool()))
    bindLVDSInputClock(sys_diff_clock_clk, reset, sysClock)

    setBoardPin(sys_diff_clock_clk.p, "clk_p")
    setBoardPin(sys_diff_clock_clk.n, "clk_n")
    setBoardPin(reset, "reset")

    val chiplink = chiplinkFMC.io.map(IO)
    chiplinkFMC.constrainIO(chiplink)

    val ddr = migDDR.io.map(IO)
    migDDR.constrainIO(ddr)

    val pcie = pcieFMC.io.map(IO)
    pcieFMC.constrainIO(pcie)
  }
}
