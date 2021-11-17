// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvc709mig

import Chisel._
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, AddressRange}

case object MemoryXilinxDDRKey extends Field[XilinxVC709MIGParams]

trait HasMemoryXilinxVC709MIG { this: BaseSubsystem =>
  val module: HasMemoryXilinxVC709MIGModuleImp

  val xilinxvc709mig = LazyModule(new XilinxVC709MIG(p(MemoryXilinxDDRKey)))

  xilinxvc709mig.node := mbus.toDRAMController(Some("xilinxvc709mig"))()
}

trait HasMemoryXilinxVC709MIGBundle {
  val xilinxvc709mig: XilinxVC709MIGIO
  def connectXilinxVC709MIGToPads(pads: XilinxVC709MIGPads) {
    pads <> xilinxvc709mig
  }
}

trait HasMemoryXilinxVC709MIGModuleImp extends LazyModuleImp
    with HasMemoryXilinxVC709MIGBundle {
  val outer: HasMemoryXilinxVC709MIG
  val ranges = AddressRange.fromSets(p(MemoryXilinxDDRKey).address)
  require (ranges.size == 1, "DDR range must be contiguous")
  val depth = ranges.head.size
  val xilinxvc709mig = IO(new XilinxVC709MIGIO(depth))

  xilinxvc709mig <> outer.xilinxvc709mig.module.io.port
}
