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
import sifive.fpgashells.devices.xilinx.ethernet._
import sifive.fpgashells.ip.xilinx.xxv_ethernet._

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

abstract class EthernetUltraScalePlacedOverlay(name: String, di: EthernetDesignInput, si: EthernetShellInput, config: XXVEthernetParams)
  extends EthernetPlacedOverlay(name, di, si)
{
  def shell: UltraScaleShell

  val pcs = LazyModule(new DiplomaticXXVEthernet(config))
  pcs.suggestName(name + "_pcs")

  val padSource = BundleBridgeSource(() => new XXVEthernetPads)
  val padSink = shell { padSource.makeSink() }
  val dclk = InModuleBody { Wire(Clock()) }

  InModuleBody {
    padSource.bundle <> pcs.module.io.pads

    val clocks = pcs.module.io.clocks
    clocks.rx_core_clk_0 := clocks.tx_mii_clk_0
    clocks.dclk          := dclk
    clocks.sys_reset     := Module.reset

    val macIO = pcs.module.io.mac
    val pcsIO = overlayOutput.eth
    macIO.tx_mii_d_0 := pcsIO.tx_d
    macIO.tx_mii_c_0 := pcsIO.tx_c
    pcsIO.rx_d := macIO.rx_mii_d_0
    pcsIO.rx_c := macIO.rx_mii_c_0

    macIO.gt_loopback_in_0 := pcsIO.loopback
    pcsIO.rx_lock := macIO.stat_rx_block_lock_0
    pcsIO.sfp_detect := true.B

    pcsIO.rx_clock := clocks.tx_mii_clk_0
    pcsIO.rx_reset := clocks.user_rx_reset_0
    pcsIO.tx_clock := clocks.tx_mii_clk_0
    pcsIO.tx_reset := clocks.user_tx_reset_0

    // refclk_p is added by the IP xdc's anonymous create_clock [get_pins name_refclk_p]
    shell.sdc.addGroup(clocks = Seq(s"${name}_refclk_p"), pins = Seq(pcs.module.blackbox.io.tx_mii_clk_0))
  }

  shell { InModuleBody {
    val pcsIO = padSink.bundle
    io.tx_p := pcsIO.gt_txp_out_0
    io.tx_n := pcsIO.gt_txn_out_0
    pcsIO.gt_rxp_in_0 := io.rx_p
    pcsIO.gt_rxn_in_0 := io.rx_n
    pcsIO.gt_refclk_p := io.refclk_p
    pcsIO.gt_refclk_n := io.refclk_n
  } }
}

abstract class PCIeUltraScalePlacedOverlay(name: String, di: PCIeDesignInput, si: PCIeShellInput, config: XDMAParams)
  extends PCIePlacedOverlay[XDMATopPads](name, di, si)
{
  def shell: UltraScaleShell

  val pcie      = LazyModule(new XDMA(config))
  val bridge    = BundleBridgeSource(() => new XDMABridge(config.lanes))
  val topBridge = shell { bridge.makeSink() }
  val axiClk    = ClockSourceNode(freqMHz = config.axiMHz)
  val areset    = ClockSinkNode(Seq(ClockSinkParameters()))
  areset := di.wrangler := axiClk

  val slaveSide = TLIdentityNode()
  pcie.crossTLIn(pcie.slave)   := slaveSide
  pcie.crossTLIn(pcie.control) := slaveSide
  val node = NodeHandle(slaveSide, pcie.crossTLOut(pcie.master))
  val intnode = pcie.crossIntOut(pcie.intnode)

  def overlayOutput = PCIeOverlayOutput(node, intnode)
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

    shell.sdc.addGroup(clocks = Seq(s"${name}_ref_clk"), pins = Seq(pcie.imp.module.blackbox.io.axi_aclk))
//    shell.sdc.addGroup(pins = Seq(pcie.imp.module.blackbox.io.sys_clk_gt))
    shell.sdc.addAsyncPath(Seq(pcie.imp.module.blackbox.io.axi_aresetn))
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
