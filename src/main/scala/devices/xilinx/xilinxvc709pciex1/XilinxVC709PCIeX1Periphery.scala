// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvc709pciex1

import Chisel._
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, BufferParams}
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts.IntSyncCrossingSink

trait HasSystemXilinxVC709PCIeX1 { this: BaseSubsystem =>
  val xilinxvc709pcie = LazyModule(new XilinxVC709PCIeX1)
  private val cname = "xilinxvc709pcie"
  sbus.coupleFrom(s"master_named_$cname") { _ :=* TLFIFOFixer(TLFIFOFixer.all) :=* xilinxvc709pcie.crossTLOut(xilinxvc709pcie.master) }
  sbus.coupleTo(s"slave_named_$cname") { xilinxvc709pcie.crossTLIn(xilinxvc709pcie.slave) :*= TLWidthWidget(sbus.beatBytes) :*= _ }
  sbus.coupleTo(s"controller_named_$cname") { xilinxvc709pcie.crossTLIn(xilinxvc709pcie.control) :*= TLWidthWidget(sbus.beatBytes) :*= _ }
  ibus.fromSync := xilinxvc709pcie.crossIntOut(xilinxvc709pcie.intnode)
}

trait HasSystemXilinxVC709PCIeX1Bundle {
  val xilinxvc709pcie: XilinxVC709PCIeX1IO
  def connectXilinxVC709PCIeX1ToPads(pads: XilinxVC709PCIeX1Pads) {
    pads <> xilinxvc709pcie
  }
}

trait HasSystemXilinxVC709PCIeX1ModuleImp extends LazyModuleImp
    with HasSystemXilinxVC709PCIeX1Bundle {
  val outer: HasSystemXilinxVC709PCIeX1
  val xilinxvc709pcie = IO(new XilinxVC709PCIeX1IO)

  xilinxvc709pcie <> outer.xilinxvc709pcie.module.io.port
}
