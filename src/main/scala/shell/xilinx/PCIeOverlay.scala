// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._

class PCIeVC707Overlay(override val shell: VC707Shell, params: PCIeOverlayParams)(implicit valName: ValName)
    extends PCIeOverlay[XilinxVC707PCIeX1Pads](shell, params)
{
  val pcieBridge = BundleBridge(new XilinxVC707PCIeX1)
  val slaveSide = TLIdentityNode()
  val topIONode = shell { pcieBridge.ioNode.sink }
  val axiClk    = shell { ClockSourceNode(freqMHz = 125) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := params.wrangler := axiClk

  val pcie = pcieBridge.child
  pcie.slave   := pcie.crossTLIn := slaveSide
  pcie.control := pcie.crossTLIn := slaveSide
  def nodes = (NodeHandle(slaveSide, pcie.crossTLOut := pcie.master), pcie.crossIntOut := pcie.intnode)

  def io = new XilinxVC707PCIeX1Pads
  def constrainIO(pcie: XilinxVC707PCIeX1Pads) = {
    val (axi, _) = axiClk.out(0)
    val (ar, _) = areset.in(0)
    val port = topIONode.io.port
    pcie <> port
    axi.clock := port.axi_aclk_out
    axi.reset := port.mmcm_lock
    port.axi_aresetn := ar.reset
    port.axi_ctl_aresetn := ar.reset

    shell.setPackagePin(pcie.REFCLK_rxp, "A10")
    shell.setPackagePin(pcie.REFCLK_rxn, "A9")
    shell.setPackagePin(pcie.pci_exp_txp, "H4")
    shell.setPackagePin(pcie.pci_exp_txn, "H3")
    shell.setPackagePin(pcie.pci_exp_rxp, "G6")
    shell.setPackagePin(pcie.pci_exp_rxn, "G5")

    shell.addConstraint(s"create_clock -name pcie_ref_clk -period 10 ${shell.portOf(pcie.REFCLK_rxp)}")
    shell.addConstraint(s"set_input_jitter ${shell.clockOf(pcie.REFCLK_rxp)} 0.5")
  }

  shell.pllFactory.describeGroup("pcie_clock", "[get_clocks -include_generated_clocks -of_objects [get_pins -hier -filter {name =~ *pcie*TXOUTCLK}]]")
}
