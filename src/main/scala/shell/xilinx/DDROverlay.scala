// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import freechips.rocketchip.tilelink._
import sifive.fpgashells.clocks._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.shell._
import sifive.fpgashells.devices.xilinx.xilinxvc707mig._

case object VC707DDRSize extends Field[BigInt](0x40000000L) // 1GB
class DDRVC707Overlay(override val shell: VC707Shell, params: DDROverlayParams)(implicit valName: ValName)
    extends DDROverlay[XilinxVC707MIGPads](shell, params)
{
  val size = p(VC707DDRSize)

  val migBridge = BundleBridge(new XilinxVC707MIG(XilinxVC707MIGParams(
    address = AddressSet.misaligned(params.baseAddress, size))))
  val topIONode = shell { migBridge.ioNode.sink }
  val ddrUI     = shell { ClockSourceNode(freqMHz = 200) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := params.wrangler := ddrUI

  def nodes = migBridge.child.node
  def io = new XilinxVC707MIGPads(size)
  def constrainIO(ddr: XilinxVC707MIGPads) = {
    val (sys, _) = shell.sysClock.out(0)
    val (ui, _) = ddrUI.out(0)
    val (ar, _) = areset.in(0)
    val port = topIONode.io.port
    ddr <> port
    ui.clock := port.ui_clk
    ui.reset := port.mmcm_locked
    port.sys_clk_i := sys.clock.asUInt
    port.sys_rst := sys.reset
    port.aresetn := ar.reset
  }

  shell.pllFactory.describeGroup("ddr", "[get_clocks {clk_pll_i}]")
}
