// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvc707pciex1

import Chisel._
import freechips.rocketchip.coreplex.{HasInterruptBus, HasSystemBus}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, BufferParams}
import freechips.rocketchip.tilelink.{TLAsyncCrossingSource, TLAsyncCrossingSink}
import freechips.rocketchip.interrupts.IntSyncCrossingSink

trait HasSystemXilinxVC707PCIeX1 extends HasSystemBus with HasInterruptBus {
  val xilinxvc707pcie = LazyModule(new XilinxVC707PCIeX1)

  sbus.fromSyncFIFOMaster(BufferParams.none) := xilinxvc707pcie.crossTLOut := xilinxvc707pcie.master
  xilinxvc707pcie.slave := xilinxvc707pcie.crossTLIn := sbus.toFixedWidthSlaves
  xilinxvc707pcie.control := xilinxvc707pcie.crossTLIn := sbus.toFixedWidthSlaves
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

  outer.xilinxvc707pcie.module.clock := outer.xilinxvc707pcie.module.io.port.axi_aclk_out
  outer.xilinxvc707pcie.module.reset := ~xilinxvc707pcie.axi_aresetn
}
