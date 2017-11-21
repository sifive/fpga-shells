// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvcu118mig

import Chisel._
import freechips.rocketchip.config._
import freechips.rocketchip.coreplex.HasMemoryBus
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, AddressRange}
import sifive.fpgashells.devices.xilinx.xilinxvc707mig.MemoryXilinxDDRKey


trait HasMemoryXilinxVCU118MIG extends HasMemoryBus {
  val module: HasMemoryXilinxVCU118MIGModuleImp

  val xilinxvcu118mig = LazyModule(new XilinxVCU118MIG(p(MemoryXilinxDDRKey)))

  require(nMemoryChannels == 1, "Coreplex must have 1 master memory port")
  xilinxvcu118mig.node := memBuses.head.toDRAMController
}

trait HasMemoryXilinxVCU118MIGBundle {
  val xilinxvcu118mig: XilinxVCU118MIGIO
}

trait HasMemoryXilinxVCU118MIGModuleImp extends LazyModuleImp
    with HasMemoryXilinxVCU118MIGBundle {
  val outer: HasMemoryXilinxVCU118MIG
  val ranges = AddressRange.fromSets(p(MemoryXilinxDDRKey).address)
  require (ranges.size == 1, "DDR range must be contiguous")
  val depth = ranges.head.size
  val xilinxvcu118mig = IO(new XilinxVCU118MIGIO)

  xilinxvcu118mig <> outer.xilinxvcu118mig.module.io.port
}
