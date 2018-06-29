// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.experimental.IO
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.devices.xilinx.xilinxvc707mig._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._

class SysClockVC707Overlay(val shell: VC707Shell, val name: String, params: ClockInputOverlayParams)
  extends LVDSClockInputXilinxOverlay(params)
{
  val node = shell { ClockSourceNode(freqMHz = 200, jitterPS = 50)(ValName(name)) }

  shell { InModuleBody {
    val (c, _) = node.out(0)
    c.reset := shell.pllReset

    shell.xdc.addBoardPin(io.p, "clk_p")
    shell.xdc.addBoardPin(io.n, "clk_n")
  } }
}

class LEDVC707Overlay(val shell: VC707Shell, val name: String, params: LEDOverlayParams)
  extends LEDXilinxOverlay(params, boardPins = Seq.tabulate(8) { i => s"leds_8bits_tri_o_$i" })

class SwitchVC707Overlay(val shell: VC707Shell, val name: String, params: SwitchOverlayParams)
  extends SwitchXilinxOverlay(params, boardPins = Seq.tabulate(8) { i => s"dip_switches_tri_i_$i" })

class ChipLinkVC707Overlay(val shell: VC707Shell, val name: String, params: ChipLinkOverlayParams)
  extends ChipLinkXilinxOverlay(params)
{
  val ereset_n = shell { InModuleBody {
    val ereset_n = IO(Input(Bool()))
    ereset_n.suggestName("ereset_n")
    shell.xdc.addPackagePin(ereset_n, "AF40")
    shell.xdc.addIOStandard(ereset_n, "LVCMOS18")
    shell.xdc.addTermination(ereset_n, "NONE")
    ereset_n
  } }

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
    (dirB2C zip dir1) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
    (dirC2B zip dir2) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }

    val (rxIn, _) = rxI.out(0)
    rxIn.reset := shell.pllReset
  } }
}

case object VC707DDRSize extends Field[BigInt](0x40000000L * 4) // 1GB
class DDRVC707Overlay(val shell: VC707Shell, val name: String, params: DDROverlayParams)
  extends DDROverlay[XilinxVC707MIGPads](params)
{
  val size = p(VC707DDRSize)

  val migBridge = BundleBridge(new XilinxVC707MIG(XilinxVC707MIGParams(
    address = AddressSet.misaligned(params.baseAddress, size))))
  val topIONode = shell { migBridge.ioNode.sink }
  val ddrUI     = shell { ClockSourceNode(freqMHz = 200) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := params.wrangler := ddrUI

  def designOutput = migBridge.child.node
  def ioFactory = new XilinxVC707MIGPads(size)

  shell { InModuleBody {
    require (shell.sys_clock.isDefined, "Use of DDRVC707Overlay depends on SysClockVC707Overlay")
    val (sys, _) = shell.sys_clock.get.node.out(0)
    val (ui, _) = ddrUI.out(0)
    val (ar, _) = areset.in(0)
    val port = topIONode.io.port
    io <> port
    ui.clock := port.ui_clk
    ui.reset := !port.mmcm_locked || port.ui_clk_sync_rst
    port.sys_clk_i := sys.clock.asUInt
    port.sys_rst := sys.reset // pllReset
    port.aresetn := !ar.reset
  } }

  shell.sdc.addGroup(clocks = Seq("clk_pll_i"))
}

class PCIeVC707Overlay(val shell: VC707Shell, val name: String, params: PCIeOverlayParams)
  extends PCIeOverlay[XilinxVC707PCIeX1Pads](params)
{
  val pcieBridge = BundleBridge(new XilinxVC707PCIeX1)
  val topIONode = shell { pcieBridge.ioNode.sink }
  val axiClk    = shell { ClockSourceNode(freqMHz = 125) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := params.wrangler := axiClk

  val pcie = pcieBridge.child
  val slaveSide = TLIdentityNode()
  pcie.slave   := pcie.crossTLIn := slaveSide
  pcie.control := pcie.crossTLIn := slaveSide
  val node = NodeHandle(slaveSide, pcie.crossTLOut := pcie.master)
  val intnode = pcie.crossIntOut := pcie.intnode

  def designOutput = (node, intnode)
  def ioFactory = new XilinxVC707PCIeX1Pads

  shell { InModuleBody {
    val (axi, _) = axiClk.out(0)
    val (ar, _) = areset.in(0)
    val port = topIONode.io.port
    io <> port
    axi.clock := port.axi_aclk_out
    axi.reset := !port.mmcm_lock
    port.axi_aresetn := !ar.reset
    port.axi_ctl_aresetn := !ar.reset

    shell.xdc.addPackagePin(io.REFCLK_rxp, "A10")
    shell.xdc.addPackagePin(io.REFCLK_rxn, "A9")
    shell.xdc.addPackagePin(io.pci_exp_txp, "H4")
    shell.xdc.addPackagePin(io.pci_exp_txn, "H3")
    shell.xdc.addPackagePin(io.pci_exp_rxp, "G6")
    shell.xdc.addPackagePin(io.pci_exp_rxn, "G5")

    shell.sdc.addClock(s"${name}_ref_clk", io.REFCLK_rxp, 100)
  } }

  shell.sdc.addGroup(clocks = Seq("txoutclk", "userclk1"))
}

class VC707Shell()(implicit p: Parameters) extends Series7Shell
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  // Order matters; ddr depends on sys_clock
  val sys_clock = Overlay(ClockInputOverlayKey)(new SysClockVC707Overlay(_, _, _))
  val led       = Overlay(LEDOverlayKey)       (new LEDVC707Overlay     (_, _, _))
  val switch    = Overlay(SwitchOverlayKey)    (new SwitchVC707Overlay  (_, _, _))
  val chiplink  = Overlay(ChipLinkOverlayKey)  (new ChipLinkVC707Overlay(_, _, _))
  val ddr       = Overlay(DDROverlayKey)       (new DDRVC707Overlay     (_, _, _))
  val pcie      = Overlay(PCIeOverlayKey)      (new PCIeVC707Overlay    (_, _, _))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addBoardPin(reset, "reset")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset
    pllReset :=
      reset_ibuf.io.O ||
      sys_clock.map(_.reset:Bool).getOrElse(false.B) ||
      chiplink.map(!_.ereset_n).getOrElse(false.B)
  }
}
