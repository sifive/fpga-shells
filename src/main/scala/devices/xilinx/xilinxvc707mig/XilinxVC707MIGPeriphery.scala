// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvc707mig

import Chisel._
import freechips.rocketchip.config._
import freechips.rocketchip.coreplex.HasMemoryBus
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, AddressRange}

case object MemoryXilinxDDRKey extends Field[XilinxVC707MIGParams]

trait HasMemoryXilinxVC707MIG extends HasMemoryBus {
  val module: HasMemoryXilinxVC707MIGModuleImp

  val xilinxvc707mig = LazyModule(new XilinxVC707MIG(p(MemoryXilinxDDRKey)))

  require(nMemoryChannels == 1, "Coreplex must have 1 master memory port")
  xilinxvc707mig.node := memBuses.head.toDRAMController
}

trait HasMemoryXilinxVC707MIGBundle {
  val xilinxvc707mig: XilinxVC707MIGIO
  def connectXilinxVC707MIGToPads(pads: XilinxVC707MIGPads) {
    pads <> xilinxvc707mig
  }
}

trait HasMemoryXilinxVC707MIGModuleImp extends LazyModuleImp
    with HasMemoryXilinxVC707MIGBundle {
  val outer: HasMemoryXilinxVC707MIG
  val ranges = AddressRange.fromSets(p(MemoryXilinxDDRKey).address)
  require (ranges.size == 1, "DDR range must be contiguous")
  val depth = ranges.head.size
  val xilinxvc707mig = IO(new XilinxVC707MIGIO(depth))

  xilinxvc707mig <> outer.xilinxvc707mig.module.io.port
}
