// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvcu118mig

import Chisel._
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, AddressRange}

case object MemoryXilinxDDRKey extends Field[XilinxVCU118MIGParams]

trait HasMemoryXilinxVCU118MIG { this: BaseSubsystem =>
  val module: HasMemoryXilinxVCU118MIGModuleImp

  val xilinxvcu118mig = LazyModule(new XilinxVCU118MIG(p(MemoryXilinxDDRKey)))

  xilinxvcu118mig.node := mbus.toDRAMController(Some("xilinxvcu118mig"))()
}

trait HasMemoryXilinxVCU118MIGBundle {
  val xilinxvcu118mig: XilinxVCU118MIGIO
  def connectXilinxVCU118MIGToPads(pads: XilinxVCU118MIGPads) {
    pads <> xilinxvcu118mig
  }
}

trait HasMemoryXilinxVCU118MIGModuleImp extends LazyModuleImp
    with HasMemoryXilinxVCU118MIGBundle {
  val outer: HasMemoryXilinxVCU118MIG
  val ranges = AddressRange.fromSets(p(MemoryXilinxDDRKey).address)
  require (ranges.size == 1, "DDR range must be contiguous")
  val depth = ranges.head.size
  val xilinxvcu118mig = IO(new XilinxVCU118MIGIO(depth))

  xilinxvcu118mig <> outer.xilinxvcu118mig.module.io.port
}
