// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvc707pciex1

import Chisel._
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, BufferParams}
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.tilelink.{TLAsyncCrossingSource, TLAsyncCrossingSink}
import freechips.rocketchip.interrupts.IntSyncCrossingSink

trait HasSystemXilinxVC707PCIeX1 { this: BaseSubsystem =>
  val xilinxvc707pcie = LazyModule(new XilinxVC707PCIeX1)
  private val name = Some("xilinxvc707pcie")
  sbus.fromMaster(name) { xilinxvc707pcie.crossTLOut } := xilinxvc707pcie.master
  xilinxvc707pcie.slave := sbus.toFixedWidthSlave(name) { xilinxvc707pcie.crossTLIn }
  xilinxvc707pcie.control := sbus.toFixedWidthSlave(name) { xilinxvc707pcie.crossTLIn }
  ibus.fromSync := xilinxvc707pcie.crossIntOut := xilinxvc707pcie.intnode
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
