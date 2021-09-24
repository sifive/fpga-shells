// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.experimental.{attach, Analog, IO}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.SyncResetSynchronizerShiftReg
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.devices.xilinx.xilinxvcu118mig._
import sifive.fpgashells.devices.xilinx.xdma._
import sifive.fpgashells.ip.xilinx.xxv_ethernet._

class SysClockVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: ClockInputDesignInput, val shellInput: ClockInputShellInput)
  extends LVDSClockInputXilinxPlacedOverlay(name, designInput, shellInput)
{
  val node = shell { ClockSourceNode(freqMHz = 300, jitterPS = 50)(ValName(name)) }

  shell { InModuleBody {
    shell.xdc.addPackagePin(io.p, "G31")
    shell.xdc.addPackagePin(io.n, "F31")
    shell.xdc.addIOStandard(io.p, "DIFF_SSTL12")
    shell.xdc.addIOStandard(io.n, "DIFF_SSTL12")
  } }
}
class SysClockVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: ClockInputShellInput)(implicit val valName: ValName)
  extends ClockInputShellPlacer[VCU118ShellBasicOverlays]
{
  def place(designInput: ClockInputDesignInput) = new SysClockVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}


class RefClockVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: ClockInputDesignInput, val shellInput: ClockInputShellInput)
  extends LVDSClockInputXilinxPlacedOverlay(name, designInput, shellInput) {
  val node = shell { ClockSourceNode(freqMHz = 125, jitterPS = 50)(ValName(name)) }

  shell { InModuleBody {
    shell.xdc.addPackagePin(io.p, "BC9")
    shell.xdc.addPackagePin(io.n, "BC8")
    shell.xdc.addIOStandard(io.p, "LVDS")
    shell.xdc.addIOStandard(io.n, "LVDS")
  } }
}
class RefClockVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: ClockInputShellInput)(implicit val valName: ValName)
  extends ClockInputShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: ClockInputDesignInput) = new RefClockVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class SDIOVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: SPIDesignInput, val shellInput: SPIShellInput)
  extends SDIOXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("BB16", IOPin(io.spi_clk)),
            ("BA10", IOPin(io.spi_cs)),
            ("AW16", IOPin(io.spi_dat(0))),
            ("BC13", IOPin(io.spi_dat(1))),
            ("BF7", IOPin(io.spi_dat(2))),
            ("BC14", IOPin(io.spi_dat(3))))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
      shell.xdc.addIOB(io)
    } }
  } }
}

class SDIOVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: SPIShellInput)(implicit val valName: ValName)
  extends SPIShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: SPIDesignInput) = new SDIOVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class SPIFlashVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: SPIFlashDesignInput, val shellInput: SPIFlashShellInput)
  extends SPIFlashXilinxPlacedOverlay(name, designInput, shellInput)
{

  shell { InModuleBody {
    /*val packagePinsWithPackageIOs = Seq(("AF13", IOPin(io.qspi_sck)),
      ("AJ11", IOPin(io.qspi_cs)),
      ("AP11", IOPin(io.qspi_dq(0))),
      ("AN11", IOPin(io.qspi_dq(1))),
      ("AM11", IOPin(io.qspi_dq(2))),
      ("AL11", IOPin(io.qspi_dq(3))))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
*/
  } }
}
class SPIFlashVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: SPIFlashShellInput)(implicit val valName: ValName)
  extends SPIFlashShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: SPIFlashDesignInput) = new SPIFlashVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class UARTVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: UARTDesignInput, val shellInput: UARTShellInput)
  extends UARTXilinxPlacedOverlay(name, designInput, shellInput, true)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("BD22", IOPin(io.ctsn.get)),
      ("BF24", IOPin(io.rtsn.get)),
      ("BC24", IOPin(io.rxd)),
      ("BE24", IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
  } }
}
class UARTVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: UARTShellInput)(implicit val valName: ValName)
  extends UARTShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: UARTDesignInput) = new UARTVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}
// Bank 127 QSFP,and VCU108 only have one QSFP quad
class QSFP1VCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: EthernetDesignInput, val shellInput: EthernetShellInput)
  extends EthernetUltraScalePlacedOverlay(name, designInput, shellInput, XXVEthernetParams(name = name, speed   = 10, dclkMHz = 125))
{
  val dclkSource = shell { BundleBridgeSource(() => Clock()) }
  val dclkSink = dclkSource.makeSink()
  InModuleBody {
    dclk := dclkSink.bundle
  }
  shell { InModuleBody {
    dclkSource.bundle := shell.ref_clock.get.get.overlayOutput.node.out(0)._1.clock
    shell.xdc.addPackagePin(io.tx_p, "AK42")
    shell.xdc.addPackagePin(io.tx_n, "AK43")
    shell.xdc.addPackagePin(io.rx_p, "AG45")
    shell.xdc.addPackagePin(io.rx_n, "AG46")
    shell.xdc.addPackagePin(io.refclk_p, "AG34")
    shell.xdc.addPackagePin(io.refclk_n, "AH35")
  } }
}
class QSFP1VCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: EthernetShellInput)(implicit val valName: ValName)
  extends EthernetShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: EthernetDesignInput) = new QSFP1VCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}
//VCU 108 do not have QSFP2
class QSFP2VCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: EthernetDesignInput, val shellInput: EthernetShellInput)
  extends EthernetUltraScalePlacedOverlay(name, designInput, shellInput, XXVEthernetParams(name = name, speed   = 10, dclkMHz = 125))
{
  val dclkSource = shell { BundleBridgeSource(() => Clock()) }
  val dclkSink = dclkSource.makeSink()
  InModuleBody {
    dclk := dclkSink.bundle
  }
  shell { InModuleBody {
//    dclkSource.bundle := shell.ref_clock.get.get.overlayOutput.node.out(0)._1.clock
//    shell.xdc.addPackagePin(io.tx_p, "L5")
//    shell.xdc.addPackagePin(io.tx_n, "L4")
//    shell.xdc.addPackagePin(io.rx_p, "T2")
//    shell.xdc.addPackagePin(io.rx_n, "T1")
//    shell.xdc.addPackagePin(io.refclk_p, "R9")
//    shell.xdc.addPackagePin(io.refclk_n, "R8")
  } }
}
class QSFP2VCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: EthernetShellInput)(implicit val valName: ValName)
  extends EthernetShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: EthernetDesignInput) = new QSFP2VCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}

object LEDVCU108PinConstraints {
  val pins = Seq("AT32", "AV34", "AY30", "BB32", "BF32", "AV36", "AY35", "BA37")
}
class LEDVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: LEDDesignInput, val shellInput: LEDShellInput)
  extends LEDXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(LEDVCU108PinConstraints.pins(shellInput.number)), ioStandard = "LVCMOS12")
class LEDVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: LEDShellInput)(implicit val valName: ValName)
  extends LEDShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: LEDDesignInput) = new LEDVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}

object ButtonVCU108PinConstraints {
  val pins = Seq("E34", "A10", "M22", "D9", "AW27")
}
class ButtonVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: ButtonDesignInput, val shellInput: ButtonShellInput)
  extends ButtonXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(ButtonVCU108PinConstraints.pins(shellInput.number)), ioStandard = "LVCMOS18")
class ButtonVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: ButtonShellInput)(implicit val valName: ValName)
  extends ButtonShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: ButtonDesignInput) = new ButtonVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}

object SwitchVCU108PinConstraints {
  val pins = Seq("BC40", "L19", "C37", "C38")
}
class SwitchVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: SwitchDesignInput, val shellInput: SwitchShellInput)
  extends SwitchXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(SwitchVCU108PinConstraints.pins(shellInput.number)), ioStandard = "LVCMOS12")
class SwitchVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: SwitchShellInput)(implicit val valName: ValName)
  extends SwitchShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: SwitchDesignInput) = new SwitchVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class ChipLinkVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: ChipLinkDesignInput, val shellInput: ChipLinkShellInput)
  extends ChipLinkXilinxPlacedOverlay(name, designInput, shellInput, rxPhase= -120, txPhase= -90, rxMargin=0.6, txMargin=0.5)
{
  val ereset_n = shell { InModuleBody {
    val ereset_n = IO(Analog(1.W))
    ereset_n.suggestName("ereset_n")
    val pin = IOPin(ereset_n, 0)
    shell.xdc.addPackagePin(pin, "P32")
    shell.xdc.addIOStandard(pin, "LVCMOS18")
    shell.xdc.addTermination(pin, "NONE")
    shell.xdc.addPullup(pin)

    val iobuf = Module(new IOBUF)
    iobuf.suggestName("chiplink_ereset_iobuf")
    attach(ereset_n, iobuf.io.IO)
    iobuf.io.T := true.B // !oe
    iobuf.io.I := false.B

    iobuf.io.O
  } }

  shell { InModuleBody {
    val dir1 = Seq("R32", "U32", "U31", /* clk, rst, send */
      "T33",  "R33",  "P35", "P36",  "N33", "M33", "N34", "N35",
      "M37", "L38", "N38", "M38", "P37", "N37", "L34", "K34",
      "M35", "L35", "M36", "L36", "N32", "M32", "Y31", "W31",
      "R31", "P31", "T30",  "T31",  "L33",  "K33",  "T34", "T35")

    val dir2 = Seq("AK34", "AG33", "AG32", /* clk, rst, send */
      "AJ32", "AK32", "AL32", "AM32", "AT39", "AT40", "AR37", "AT37",
      "AT35", "AT36", "AL30", "AL31", "AN33", "AP33", "AM36", "AN36",
      "AP36", "AP37", "AL29", "AM29", "AP35", "AR35", "AL35", "AL36",
      "AP38", "AR38", "AJ30", "AJ31", "AN34", "AN35", "AG31", "AH31")

    (IOPin.of(io.b2c) zip dir1) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
    (IOPin.of(io.c2b) zip dir2) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
  } }
}
class ChipLinkVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: ChipLinkShellInput)(implicit val valName: ValName)
  extends ChipLinkShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: ChipLinkDesignInput) = new ChipLinkVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}

// TODO: JTAG is untested
class JTAGDebugVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: JTAGDebugDesignInput, val shellInput: JTAGDebugShellInput)
  extends JTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val pin_locations = Map(
      "PMOD_J52" -> Seq("AW16",      "BF7",      "BC13",      "BC14",      "BA10"),
      "PMOD_J53" -> Seq( "J20",       "T23",       "J24",       "P22",       "N22"),
      "FMC_J2"   -> Seq("AJ31",      "AP38",      "AR38",      "AN35",      "AJ30"))
    val pins      = Seq(io.jtag_TCK, io.jtag_TMS, io.jtag_TDI, io.jtag_TDO, io.srst_n)

    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))

    val pin_voltage:String = if(shellInput.location.get == "PMOD_J53") "LVCMOS12" else "LVCMOS18"

    (pin_locations(shellInput.location.get) zip pins) foreach { case (pin_location, ioport) =>
      val io = IOPin(ioport)
      shell.xdc.addPackagePin(io, pin_location)
      shell.xdc.addIOStandard(io, pin_voltage)
      shell.xdc.addPullup(io)
      shell.xdc.addIOB(io)
    }
  } }
}
class JTAGDebugVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: JTAGDebugShellInput)(implicit val valName: ValName)
  extends JTAGDebugShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: JTAGDebugDesignInput) = new JTAGDebugVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class cJTAGDebugVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: cJTAGDebugDesignInput, val shellInput: cJTAGDebugShellInput)
  extends cJTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCKC", IOPin(io.cjtag_TCKC), 10)
    shell.sdc.addGroup(clocks = Seq("JTCKC"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.cjtag_TCKC))
    val packagePinsWithPackageIOs = Seq(("AR37", IOPin(io.cjtag_TCKC)),
                                     ("AM36", IOPin(io.cjtag_TMSC)),
                                      ("AT37", IOPin(io.srst_n)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
    } }
    shell.xdc.addPullup(IOPin(io.cjtag_TCKC))
    shell.xdc.addPullup(IOPin(io.srst_n))
  } }
}
class cJTAGDebugVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: cJTAGDebugShellInput)(implicit val valName: ValName)
  extends cJTAGDebugShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: cJTAGDebugDesignInput) = new cJTAGDebugVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class JTAGDebugBScanVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: JTAGDebugBScanDesignInput, val shellInput: JTAGDebugBScanShellInput)
  extends JTAGDebugBScanXilinxPlacedOverlay(name, designInput, shellInput)
class JTAGDebugBScanVCU108ShellPlacer(val shell: VCU108ShellBasicOverlays, val shellInput: JTAGDebugBScanShellInput)(implicit val valName: ValName)
  extends JTAGDebugBScanShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: JTAGDebugBScanDesignInput) = new JTAGDebugBScanVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}

case object VCU108DDRSize extends Field[BigInt](0x40000000L * 2) // 2GB
//fpga-shells/src/main/scala/devices/xilinx/xilinxvcu118mig/XilinxVCU118MIG.scala
class DDRVCU108PlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: DDRDesignInput, val shellInput: DDRShellInput)
  extends DDRPlacedOverlay[XilinxVCU118MIGPads](name, designInput, shellInput)
{
  val size = p(VCU108DDRSize)

  val migParams = XilinxVCU118MIGParams(address = AddressSet.misaligned(di.baseAddress, size))
  val mig = LazyModule(new XilinxVCU118MIG(migParams))
  val ioNode = BundleBridgeSource(() => mig.module.io.cloneType)
  val topIONode = shell { ioNode.makeSink() }
  val ddrUI     = shell { ClockSourceNode(freqMHz = 200) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := designInput.wrangler := ddrUI

  def overlayOutput = DDROverlayOutput(ddr = mig.node)
  def ioFactory = new XilinxVCU118MIGPads(size)

  InModuleBody { ioNode.bundle <> mig.module.io }

  shell { InModuleBody {
    require (shell.sys_clock.get.isDefined, "Use of DDRVCU108Overlay depends on SysClockVCU108Overlay")
    val (sys, _) = shell.sys_clock.get.get.overlayOutput.node.out(0)
    val (ui, _) = ddrUI.out(0)
    val (ar, _) = areset.in(0)
    val port = topIONode.bundle.port
    io <> port
    ui.clock := port.c0_ddr4_ui_clk
    ui.reset := /*!port.mmcm_locked ||*/ port.c0_ddr4_ui_clk_sync_rst
    port.c0_sys_clk_i := sys.clock.asUInt
    port.sys_rst := sys.reset // pllReset
    port.c0_ddr4_aresetn := !ar.reset

    val allddrpins = Seq(  "C30", "D32", "B30", "C33", "E32", "A29", "C29",
      "E29", "A30", "C32", "A31", "A33", "F29", "B32", "D29", "B31", "B33",
      "F33", "G30", "F30", "M28", "E33", "D31", "E31", "K29", "D30", "J31",
      "J37", "H40", "F38", "H39",  "K37", "G40", "F39",  "F40",  "F36", "J36",
      "F35", "J35", "G37", "H35", "G36", "H37", "C39", "A38", "B40", "D40",
      "E38", "B38", "E37", "C40", "C34", "A34", "D34", "A35", "A36", "C35",
      "B35", "D35", "N27", "R27", "N24", "R24", "P24", "P26", "P27", "T24",
      "K27", "L26", "J27", "K28", "K26", "M25", "J26", "L28", "E27", "E28",
      "E26", "H27", "F25", "F28", "G25", "G27", "B28", "A28", "B25", "B27",
      "D25", "C27", "C25", "D26", "G38", "G35", "A40", "B37", "N25", "L25",
      "G28", "A26", "H38", "H34", "A39", "B36", "P25", "L24", "H28", "B26",
      "J39", "F34", "E39", "D37", "T26", "M27", "G26", "D27")

    (IOPin.of(io) zip allddrpins) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
  } }

  shell.sdc.addGroup(pins = Seq(mig.island.module.blackbox.io.c0_ddr4_ui_clk))
}
class DDRVCU108ShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: DDRShellInput)(implicit val valName: ValName)
  extends DDRShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: DDRDesignInput) = new DDRVCU108PlacedOverlay(shell, valName.name, designInput, shellInput)
}
// There is no this kind of pcie in VCU108
//class PCIeVCU108FMCPlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: PCIeDesignInput, val shellInput: PCIeShellInput)
//  extends PCIeUltraScalePlacedOverlay(name, designInput, shellInput, XDMAParams(
//    name     = "fmc_xdma",
//    location = "X0Y0",
//    bars     = designInput.bars,
//    control  = designInput.ecam,
//    bases    = designInput.bases,
//    lanes    = 4))
//{
//  shell { InModuleBody {
////    // Work-around incorrectly pre-assigned pins
////    IOPin.of(io).foreach { shell.xdc.addPackagePin(_, "") }
////
////    // We need some way to connect both of these to reach x8
////    val ref126 = Seq("V38",  "V39")  /* [pn] GBT0 Bank 126 */
////    val ref121 = Seq("AK38", "AK39") /* [pn] GBT0 Bank 121 */
////    val ref = ref126
////
////    // Bank 126 (DP5, DP6, DP4, DP7), Bank 121 (DP3, DP2, DP1, DP0)
////    val rxp = Seq("U45", "R45", "W45", "N45", "AJ45", "AL45", "AN45", "AR45") /* [0-7] */
////    val rxn = Seq("U46", "R46", "W46", "N46", "AJ46", "AL46", "AN46", "AR46") /* [0-7] */
////    val txp = Seq("P42", "M42", "T42", "K42", "AL40", "AM42", "AP42", "AT42") /* [0-7] */
////    val txn = Seq("P43", "M43", "T43", "K43", "AL41", "AM43", "AP43", "AT43") /* [0-7] */
//
//    def bind(io: Seq[IOPin], pad: Seq[String]) {
//      (io zip pad) foreach { case (io, pad) => shell.xdc.addPackagePin(io, pad) }
//    }
//
//    bind(IOPin.of(io.refclk), ref)
//    // We do these individually so that zip falls off the end of the lanes:
//    bind(IOPin.of(io.lanes.pci_exp_txp), txp)
//    bind(IOPin.of(io.lanes.pci_exp_txn), txn)
//    bind(IOPin.of(io.lanes.pci_exp_rxp), rxp)
//    bind(IOPin.of(io.lanes.pci_exp_rxn), rxn)
//  } }
//}
//class PCIeVCU108FMCShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: PCIeShellInput)(implicit val valName: ValName)
//  extends PCIeShellPlacer[VCU108ShellBasicOverlays] {
//  def place(designInput: PCIeDesignInput) = new PCIeVCU108FMCPlacedOverlay(shell, valName.name, designInput, shellInput)
//}

class PCIeVCU108EdgePlacedOverlay(val shell: VCU108ShellBasicOverlays, name: String, val designInput: PCIeDesignInput, val shellInput: PCIeShellInput)
  extends PCIeUltraScalePlacedOverlay(name, designInput, shellInput, XDMAParams(
    name     = "edge_xdma",
    location = "X1Y2",
    bars     = designInput.bars,
    control  = designInput.ecam,
    bases    = designInput.bases,
    lanes    = 8))
{
  shell { InModuleBody {
    // Work-around incorrectly pre-assigned pins
    IOPin.of(io).foreach { shell.xdc.addPackagePin(_, "") }
    // PCIe Edge connector U2
    // FMC+ J22
    val ref227 = Seq("AL9", "AL8")  /* [pn]  Bank 227 PCIE_CLK2_*/
    val ref = ref227
    // PCIe Edge connector U2 : Bank 227, 226
    val rxp = Seq("AJ4", "AK2", "AM2", "AP2", "AT2", "AV2", "AY2", "BB2") // [0-7]
    val rxn = Seq("AJ3", "AK1", "AM1", "AP1", "AT1", "AV1", "AY1", "BB1") // [0-7]
    val txp = Seq("AP7", "AR5", "AT7", "AU5", "AW5", "BA5", "BC5", "BE5") // [0-7]
    val txn = Seq("AP6", "AR4", "AT6", "AU4", "AW4", "BA4", "BC4", "BE4") // [0-7]



    def bind(io: Seq[IOPin], pad: Seq[String]) {
      (io zip pad) foreach { case (io, pad) => shell.xdc.addPackagePin(io, pad) }
    }

    bind(IOPin.of(io.refclk), ref)
    // We do these individually so that zip falls off the end of the lanes:
    bind(IOPin.of(io.lanes.pci_exp_txp), txp)
    bind(IOPin.of(io.lanes.pci_exp_txn), txn)
    bind(IOPin.of(io.lanes.pci_exp_rxp), rxp)
    bind(IOPin.of(io.lanes.pci_exp_rxn), rxn)

  } }

}
class PCIeVCU108EdgeShellPlacer(shell: VCU108ShellBasicOverlays, val shellInput: PCIeShellInput)(implicit val valName: ValName)
  extends PCIeShellPlacer[VCU108ShellBasicOverlays] {
  def place(designInput: PCIeDesignInput) = new PCIeVCU108EdgePlacedOverlay(shell, valName.name, designInput, shellInput)
}

abstract class VCU108ShellBasicOverlays()(implicit p: Parameters) extends UltraScaleShell{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val sys_clock = Overlay(ClockInputOverlayKey, new SysClockVCU108ShellPlacer(this, ClockInputShellInput()))
  val ref_clock = Overlay(ClockInputOverlayKey, new RefClockVCU108ShellPlacer(this, ClockInputShellInput()))
  val led       = Seq.tabulate(8)(i => Overlay(LEDOverlayKey, new LEDVCU108ShellPlacer(this, LEDShellInput(color = "red", number = i))(valName = ValName(s"led_$i"))))
  val switch    = Seq.tabulate(4)(i => Overlay(SwitchOverlayKey, new SwitchVCU108ShellPlacer(this, SwitchShellInput(number = i))(valName = ValName(s"switch_$i"))))
  val button    = Seq.tabulate(5)(i => Overlay(ButtonOverlayKey, new ButtonVCU108ShellPlacer(this, ButtonShellInput(number = i))(valName = ValName(s"button_$i"))))
  val ddr       = Overlay(DDROverlayKey, new DDRVCU108ShellPlacer(this, DDRShellInput()))
  val qsfp1     = Overlay(EthernetOverlayKey, new QSFP1VCU108ShellPlacer(this, EthernetShellInput()))
//  val qsfp2     = Overlay(EthernetOverlayKey, new QSFP2VCU108ShellPlacer(this, EthernetShellInput()))
  val chiplink  = Overlay(ChipLinkOverlayKey, new ChipLinkVCU108ShellPlacer(this, ChipLinkShellInput()))
  //val spi_flash = Overlay(SPIFlashOverlayKey, new SPIFlashVCU108ShellPlacer(this, SPIFlashShellInput()))
  //SPI Flash not functional
}

case object VCU108ShellPMOD extends Field[String]("JTAG")
case object VCU108ShellPMOD2 extends Field[String]("JTAG")

class WithVCU108ShellPMOD(device: String) extends Config((site, here, up) => {
  case VCU108ShellPMOD => device
})

// Change JTAG pinouts to VCU108 J53
// Due to the level shifter is from 1.2V to 3.3V, the frequency of JTAG should be slow down to 1Mhz
class WithVCU108ShellPMOD2(device: String) extends Config((site, here, up) => {
  case VCU108ShellPMOD2 => device
})

class WithVCU108ShellPMODJTAG extends WithVCU108ShellPMOD("JTAG")
class WithVCU108ShellPMODSDIO extends WithVCU108ShellPMOD("SDIO")

// Reassign JTAG pinouts location to PMOD J53
class WithVCU108ShellPMOD2JTAG extends WithVCU108ShellPMOD2("PMODJ53_JTAG")

class VCU108Shell()(implicit p: Parameters) extends VCU108ShellBasicOverlays
{
  val pmod_is_sdio  = p(VCU108ShellPMOD) == "SDIO"
  val pmod_j53_is_jtag = p(VCU108ShellPMOD2) == "PMODJ53_JTAG"
  val jtag_location = Some(if (pmod_is_sdio) (if (pmod_j53_is_jtag) "PMOD_J53" else "FMC_J2") else "PMOD_J52")

  // Order matters; ddr depends on sys_clock
  val uart      = Overlay(UARTOverlayKey, new UARTVCU108ShellPlacer(this, UARTShellInput()))
  val sdio      = if (pmod_is_sdio) Some(Overlay(SPIOverlayKey, new SDIOVCU108ShellPlacer(this, SPIShellInput()))) else None
  val jtag      = Overlay(JTAGDebugOverlayKey, new JTAGDebugVCU108ShellPlacer(this, JTAGDebugShellInput(location = jtag_location)))
  val cjtag     = Overlay(cJTAGDebugOverlayKey, new cJTAGDebugVCU108ShellPlacer(this, cJTAGDebugShellInput()))
  val jtagBScan = Overlay(JTAGDebugBScanOverlayKey, new JTAGDebugBScanVCU108ShellPlacer(this, JTAGDebugBScanShellInput()))
 // val fmc       = Overlay(PCIeOverlayKey, new PCIeVCU108FMCShellPlacer(this, PCIeShellInput()))
  val edge      = Overlay(PCIeOverlayKey, new PCIeVCU108EdgeShellPlacer(this, PCIeShellInput()))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  designParameters(ClockInputOverlayKey).foreach { unused =>
    val source = unused.place(ClockInputDesignInput()).overlayOutput.node
    val sink = ClockSinkNode(Seq(ClockSinkParameters()))
    sink := source
  }

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addPackagePin(reset, "E36")
    xdc.addIOStandard(reset, "LVCMOS12")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    val sysclk: Clock = sys_clock.get() match {
      case Some(x: SysClockVCU108PlacedOverlay) => x.clock
    }

    val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
    sdc.addAsyncPath(Seq(powerOnReset))

    val ereset: Bool = chiplink.get() match {
      case Some(x: ChipLinkVCU108PlacedOverlay) => !x.ereset_n
      case _ => false.B
    }

    pllReset := (reset_ibuf.io.O || powerOnReset || ereset)
  }
}
