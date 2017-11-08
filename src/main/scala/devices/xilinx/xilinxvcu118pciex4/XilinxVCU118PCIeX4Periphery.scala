// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvcu118pciex4

import Chisel._
import freechips.rocketchip.coreplex.{HasInterruptBus, HasSystemBus}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, BufferParams}
import freechips.rocketchip.tilelink.{TLAsyncCrossingSource, TLAsyncCrossingSink}
import freechips.rocketchip.interrupts.IntSyncCrossingSink

trait HasSystemXilinxVCU118PCIeX4 extends HasSystemBus with HasInterruptBus {
  val xilinxvcu118pcie = LazyModule(new XilinxVCU118PCIeX4)

  sbus.fromSyncFIFOMaster(BufferParams.none) := xilinxvcu118pcie.crossTLOut := xilinxvcu118pcie.master
  xilinxvcu118pcie.slave := xilinxvcu118pcie.crossTLIn := sbus.toFixedWidthSlaves
  xilinxvcu118pcie.control := xilinxvcu118pcie.crossTLIn := sbus.toFixedWidthSlaves
  ibus.fromSync := xilinxvcu118pcie.crossIntOut := xilinxvcu118pcie.intnode
}

trait HasSystemXilinxVCU118PCIeX4Bundle {
  val xilinxvcu118pcie: XilinxVCU118PCIeX4IO
  def connectXilinxVCU118PCIeX4ToPads(pads: XilinxVCU118PCIeX4Pads) {
    pads <> xilinxvcu118pcie
  }
}

trait HasSystemXilinxVCU118PCIeX4ModuleImp extends LazyModuleImp
    with HasSystemXilinxVCU118PCIeX4Bundle {
  val outer: HasSystemXilinxVCU118PCIeX4
  val xilinxvcu118pcie = IO(new XilinxVCU118PCIeX4IO)

  xilinxvcu118pcie <> outer.xilinxvcu118pcie.module.io.port

  outer.xilinxvcu118pcie.module.clock := outer.xilinxvcu118pcie.module.io.port.axi_aclk
  outer.xilinxvcu118pcie.module.reset := ~xilinxvcu118pcie.axi_aresetn
}
