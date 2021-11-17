// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvc709pcie

import Chisel._
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, BufferParams}
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts.IntSyncCrossingSink

trait HasSystemXilinxVC709PCIe { this: BaseSubsystem =>
  val xilinxvc709pcie = LazyModule(new XilinxVC709PCIe)
  private val cname = "xilinxvc709pcie"
  sbus.coupleFrom(s"master_named_$cname") { _ :=* TLFIFOFixer(TLFIFOFixer.all) :=* xilinxvc709pcie.crossTLOut(xilinxvc709pcie.master) }
  sbus.coupleTo(s"slave_named_$cname") { xilinxvc709pcie.crossTLIn(xilinxvc709pcie.slave) :*= TLWidthWidget(sbus.beatBytes) :*= _ }
  sbus.coupleTo(s"controller_named_$cname") { xilinxvc709pcie.crossTLIn(xilinxvc709pcie.control) :*= TLWidthWidget(sbus.beatBytes) :*= _ }
  ibus.fromSync := xilinxvc709pcie.crossIntOut(xilinxvc709pcie.intnode)
}

trait HasSystemXilinxVC709PCIeBundle {
  val xilinxvc709pcie: XilinxVC709PCIeIO
  def connectXilinxVC709PCIeToPads(pads: XilinxVC709PCIePads) {
    pads <> xilinxvc709pcie
  }
}

trait HasSystemXilinxVC709PCIeModuleImp extends LazyModuleImp
    with HasSystemXilinxVC709PCIeBundle {
  val outer: HasSystemXilinxVC709PCIe
  val xilinxvc709pcie = IO(new XilinxVC709PCIeIO)

  xilinxvc709pcie <> outer.xilinxvc709pcie.module.io.port
}