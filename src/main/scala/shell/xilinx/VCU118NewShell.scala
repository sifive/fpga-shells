// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.experimental.{attach, IO, withClockAndReset}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.SyncResetSynchronizerShiftReg
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.devices.xilinx.xilinxvcu118mig._
//import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._

class SysClockVCU118Overlay(val shell: VCU118Shell, val name: String, params: ClockInputOverlayParams)
  extends LVDSClockInputXilinxOverlay(params)
{
  val node = shell { ClockSourceNode(freqMHz = 250, jitterPS = 50)(ValName(name)) }

  shell { InModuleBody {
    val (c, _) = node.out(0)
    c.reset := shell.pllReset

    shell.xdc.addBoardPin(io.p, "default_250mhz_clk1_p")
    shell.xdc.addBoardPin(io.n, "default_250mhz_clk1_n")
  } }
}

class SDIOVCU118Overlay(val shell: VCU118Shell, val name: String, params: SDIOOverlayParams)
  extends SDIOXilinxOverlay(params)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("AV15", IOPin(io.sdio_clk)),
                                        ("AY15", IOPin(io.sdio_cmd)),
                                        ("AW15", IOPin(io.sdio_dat_0)),
                                        ("AV16", IOPin(io.sdio_dat_1)),
                                        ("AU16", IOPin(io.sdio_dat_2)),
                                        ("AY14", IOPin(io.sdio_dat_3)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}

class UARTVCU118Overlay(val shell: VCU118Shell, val name: String, params: UARTOverlayParams)
  extends UARTXilinxOverlay(params)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("AY25", IOPin(io.ctsn)),
                                        ("BB22", IOPin(io.rtsn)),
                                        ("AW25", IOPin(io.rxd)),
                                        ("BB21", IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
  } }
}

class LEDVCU118Overlay(val shell: VCU118Shell, val name: String, params: LEDOverlayParams)
  extends LEDXilinxOverlay(params, boardPins = Seq.tabulate(8) { i => s"GPIO_LED_${i}_LS" })

class SwitchVCU118Overlay(val shell: VCU118Shell, val name: String, params: SwitchOverlayParams)
  extends SwitchXilinxOverlay(params, boardPins = Seq.tabulate(4) { i => s"GPIO_DIP_SW$i" })

class ChipLinkVCU118Overlay(val shell: VCU118Shell, val name: String, params: ChipLinkOverlayParams)
  extends ChipLinkXilinxOverlay(params, rxPhase= -120, txPhase= -120, rxMargin=0.6, txMargin=0.6)
{
  val ereset_n = shell { InModuleBody {
    val ereset_n = IO(Input(Bool()))
    ereset_n.suggestName("ereset_n")
    shell.xdc.addPackagePin(ereset_n, "BC8")
    shell.xdc.addIOStandard(ereset_n, "LVCMOS18")
    shell.xdc.addTermination(ereset_n, "NONE")
    ereset_n
  } }

  shell { InModuleBody {
    val dir1 = Seq("BC9", "AV8", "AV9", /* clk, rst, send */
                   "AY9",  "BA9",  "BF10", "BF9",  "BC11", "BD11", "BD12", "BE12",
                   "BF12", "BF11", "BE14", "BF14", "BD13", "BE13", "BC15", "BD15",
                   "BE15", "BF15", "BA14", "BB14", "BB13", "BB12", "BA16", "BA15",
                   "BC14", "BC13", "AY8",  "AY7",  "AW8",  "AW7",  "BB16", "BC16")
    val dir2 = Seq("AV14", "AK13", "AK14", /* clk, rst, send */
                   "AR14", "AT14", "AP12", "AR12", "AW12", "AY12", "AW11", "AY10",
                   "AU11", "AV11", "AW13", "AY13", "AN16", "AP16", "AP13", "AR13",
                   "AT12", "AU12", "AK15", "AL15", "AL14", "AM14", "AV10", "AW10",
                   "AN15", "AP15", "AK12", "AL12", "AM13", "AM12", "AJ13", "AJ12")
    (IOPin.of(io.b2c) zip dir1) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
    (IOPin.of(io.c2b) zip dir2) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }

    val (rxIn, _) = rxI.out(0)
    rxIn.reset := shell.pllReset
  } }
}

// TODO: JTAG is untested
class JTAGDebugVCU118Overlay(val shell: VCU118Shell, val name: String, params: JTAGDebugOverlayParams)
  extends JTAGDebugXilinxOverlay(params)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))
    val packagePinsWithPackageIOs = Seq(("P29", IOPin(io.jtag_TCK)),
                                        ("L31", IOPin(io.jtag_TMS)),
                                        ("M31", IOPin(io.jtag_TDI)),
                                        ("R29", IOPin(io.jtag_TDO)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addPullup(io)
    } }
  } }
}

//case object VCU118DDRSize extends Field[BigInt](0x40000000L * 2) // 2GB
//class DDRVCU118Overlay(val shell: VCU118Shell, val name: String, params: DDROverlayParams)
//  extends DDROverlay[XilinxVCU118MIGPads](params)
//{
//  val size = p(VCU118DDRSize)
//
//  val migBridge = BundleBridge(new XilinxVCU118MIG(XilinxVCU118MIGParams(
//    address = AddressSet.misaligned(params.baseAddress, size))))
//  val topIONode = shell { migBridge.ioNode.sink }
//  val ddrUI     = shell { ClockSourceNode(freqMHz = 200) }
//  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
//  areset := params.wrangler := ddrUI
//
//  def designOutput = migBridge.child.node
//  def ioFactory = new XilinxVCU118MIGPads(size)
//
//  shell { InModuleBody {
//    require (shell.sys_clock.isDefined, "Use of DDRVCU118Overlay depends on SysClockVCU118Overlay")
//    val (sys, _) = shell.sys_clock.get.node.out(0)
//    val (ui, _) = ddrUI.out(0)
//    val (ar, _) = areset.in(0)
//    val port = topIONode.io.port
//    io <> port
//    ui.clock := port.c0_ddr4_ui_clk
//    ui.reset := /*!port.mmcm_locked ||*/ port.c0_ddr4_ui_clk_sync_rst
//    port.c0_sys_clk_i := sys.clock.asUInt
//    port.sys_rst := sys.reset // pllReset
//    port.c0_ddr4_aresetn := !ar.reset
//  } }
//
//  shell.sdc.addGroup(clocks = Seq("clk_pll_i"))
//}

//class PCIeVCU118Overlay(val shell: VCU118Shell, val name: String, params: PCIeOverlayParams)
//  extends PCIeOverlay[XilinxVCU118PCIeX1Pads](params)
//{
//  val pcieBridge = BundleBridge(new XilinxVCU118PCIeX1)
//  val topIONode = shell { pcieBridge.ioNode.sink }
//  val axiClk    = shell { ClockSourceNode(freqMHz = 125) }
//  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
//  areset := params.wrangler := axiClk
//
//  val pcie = pcieBridge.child
//  val slaveSide = TLIdentityNode()
//  pcie.slave   := pcie.crossTLIn := slaveSide
//  pcie.control := pcie.crossTLIn := slaveSide
//  val node = NodeHandle(slaveSide, pcie.crossTLOut := pcie.master)
//  val intnode = pcie.crossIntOut := pcie.intnode
//
//  def designOutput = (node, intnode)
//  def ioFactory = new XilinxVCU118PCIeX1Pads
//
//  shell { InModuleBody {
//    val (axi, _) = axiClk.out(0)
//    val (ar, _) = areset.in(0)
//    val port = topIONode.io.port
//    io <> port
//    axi.clock := port.axi_aclk_out
//    axi.reset := !port.mmcm_lock
//    port.axi_aresetn := !ar.reset
//    port.axi_ctl_aresetn := !ar.reset
//
//    shell.xdc.addPackagePin(io.REFCLK_rxp, "A10")
//    shell.xdc.addPackagePin(io.REFCLK_rxn, "A9")
//    shell.xdc.addPackagePin(io.pci_exp_txp, "H4")
//    shell.xdc.addPackagePin(io.pci_exp_txn, "H3")
//    shell.xdc.addPackagePin(io.pci_exp_rxp, "G6")
//    shell.xdc.addPackagePin(io.pci_exp_rxn, "G5")
//
//    shell.sdc.addClock(s"${name}_ref_clk", io.REFCLK_rxp, 100)
//  } }
//
//  shell.sdc.addGroup(clocks = Seq("txoutclk", "userclk1"))
//}

class VCU118Shell()(implicit p: Parameters) extends Series7Shell
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  // Order matters; ddr depends on sys_clock
  val sys_clock = Overlay(ClockInputOverlayKey)(new SysClockVCU118Overlay(_, _, _))
  val led       = Overlay(LEDOverlayKey)       (new LEDVCU118Overlay     (_, _, _))
  val switch    = Overlay(SwitchOverlayKey)    (new SwitchVCU118Overlay  (_, _, _))
  val chiplink  = Overlay(ChipLinkOverlayKey)  (new ChipLinkVCU118Overlay(_, _, _))
//  val ddr       = Overlay(DDROverlayKey)       (new DDRVCU118Overlay     (_, _, _))
//  val pcie      = Overlay(PCIeOverlayKey)      (new PCIeVCU118Overlay    (_, _, _))
  val uart      = Overlay(UARTOverlayKey)      (new UARTVCU118Overlay    (_, _, _))
  val sdio      = Overlay(SDIOOverlayKey)      (new SDIOVCU118Overlay    (_, _, _))
  val jtag      = Overlay(JTAGDebugOverlayKey)      (new JTAGDebugVCU118Overlay    (_, _, _))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addPackagePin(reset, "L19")
    xdc.addIOStandard(reset, "LVCMOS12")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset
    pllReset :=
      reset_ibuf.io.O ||
      sys_clock.map(_.reset:Bool).getOrElse(false.B) ||
      chiplink.map(!_.ereset_n).getOrElse(false.B)
  }
}
