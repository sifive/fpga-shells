// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.devices.xilinx.xdma._


class XDMATopPads(val numLanes: Int) extends Bundle {
  val refclk = Input(new LVDSClock)
  val lanes = new XDMAPads(numLanes)
}

class XDMABridge(val numLanes: Int) extends Bundle {
  val lanes = new XDMAPads(numLanes)
  val srstn = Input(Bool())
  val O     = Input(Clock())
  val ODIV2 = Input(Clock())
}

abstract class PCIeUltraScaleOverlay(config: XDMAParams, params: PCIeOverlayParams)
  extends PCIeOverlay[XDMATopPads](params)
{
  def shell: UltraScaleShell

  val pcie      = LazyModule(new XDMA(config))
  val bridge    = BundleBridgeSource(() => new XDMABridge(config.lanes))
  val topBridge = shell { bridge.makeSink() }
  val axiClk    = ClockSourceNode(freqMHz = config.axiMHz)
  val areset    = ClockSinkNode(Seq(ClockSinkParameters()))
  areset := params.wrangler := axiClk

  val slaveSide = TLIdentityNode()
  pcie.crossTLIn(pcie.slave)   := slaveSide
  pcie.crossTLIn(pcie.control) := slaveSide
  val node = NodeHandle(slaveSide, pcie.crossTLOut(pcie.master))
  val intnode = pcie.crossIntOut(pcie.intnode)

  def designOutput = (node, intnode)
  def ioFactory = new XDMATopPads(config.lanes)

  InModuleBody {
    val (axi, _) = axiClk.out(0)
    val (ar, _) = areset.in(0)
    val b = bridge.out(0)._1

    pcie.module.clock := ar.clock
    pcie.module.reset := ar.reset

    b.lanes <> pcie.module.io.pads

    axi.clock := pcie.module.io.clocks.axi_aclk
    axi.reset := !pcie.module.io.clocks.axi_aresetn
    pcie.module.io.clocks.sys_rst_n  := b.srstn
    pcie.module.io.clocks.sys_clk    := b.ODIV2
    pcie.module.io.clocks.sys_clk_gt := b.O

    shell.sdc.addGroup(clocks = Seq(s"${name}_ref_clk", "pipe_clk"), pins = Seq(pcie.imp.module.blackbox.io.axi_aclk))
  }

  shell { InModuleBody {
    val b = topBridge.in(0)._1

    val ibufds = Module(new IBUFDS_GTE4)
    ibufds.suggestName(s"${name}_refclk_ibufds")
    ibufds.io.CEB := false.B
    ibufds.io.I   := io.refclk.p
    ibufds.io.IB  := io.refclk.n
    b.O     := ibufds.io.O
    b.ODIV2 := ibufds.io.ODIV2
    b.srstn := !shell.pllReset
    io.lanes <> b.lanes

    shell.sdc.addClock(s"${name}_ref_clk", io.refclk.p, 100)
  } }
}
