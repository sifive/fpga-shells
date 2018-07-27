// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvc707pciex1

import Chisel._
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, BufferParams}
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts.IntSyncCrossingSink

trait HasSystemXilinxVC707PCIeX1 { this: BaseSubsystem =>
  val xilinxvc707pcie = LazyModule(new XilinxVC707PCIeX1)
  private val cname = "xilinxvc707pcie"
  sbus.coupleFrom(s"master_named_$cname") { _ :=* TLFIFOFixer(TLFIFOFixer.all) :=* xilinxvc707pcie.crossTLOut(xilinxvc707pcie.master) }
  sbus.coupleTo(s"slave_named_$cname") { xilinxvc707pcie.crossTLIn(xilinxvc707pcie.slave) :*= TLWidthWidget(sbus.beatBytes) :*= _ }
  sbus.coupleTo(s"controller_named_$cname") { xilinxvc707pcie.crossTLIn(xilinxvc707pcie.control) :*= TLWidthWidget(sbus.beatBytes) :*= _ }
  ibus.fromSync := xilinxvc707pcie.crossIntOut(xilinxvc707pcie.intnode)
}

trait HasSystemXilinxVC707PCIeX1Bundle {
  val xilinxvc707pcie: XilinxVC707PCIeX1IO
  def connectXilinxVC707PCIeX1ToPads(pads: XilinxVC707PCIeX1Pads) {
    pads <> xilinxvc707pcie
  }
}

trait HasSystemXilinxVC707PCIeX1ModuleImp extends LazyModuleImp
    with HasSystemXilinxVC707PCIeX1Bundle {
  val outer: HasSystemXilinxVC707PCIeX1
  val xilinxvc707pcie = IO(new XilinxVC707PCIeX1IO)

  xilinxvc707pcie <> outer.xilinxvc707pcie.module.io.port
}
