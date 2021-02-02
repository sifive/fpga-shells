// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxgenesys2mig

import Chisel._
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, AddressRange}

case object MemoryXilinxDDRKey extends Field[XilinxGenesys2MIGParams]

trait HasMemoryXilinxGenesys2MIG { this: BaseSubsystem =>
  val module: HasMemoryXilinxGenesys2MIGModuleImp

  val xilinxgenesys2mig = LazyModule(new XilinxGenesys2MIG(p(MemoryXilinxDDRKey)))

  xilinxgenesys2mig.node := mbus.toDRAMController(Some("xilinxgenesys2mig"))()
}

trait HasMemoryXilinxGenesys2MIGBundle {
  val xilinxgenesys2mig: XilinxGenesys2MIGIO
  def connectXilinxGenesys2MIGToPads(pads: XilinxGenesys2MIGPads) {
    pads <> xilinxgenesys2mig
  }
}

trait HasMemoryXilinxGenesys2MIGModuleImp extends LazyModuleImp
    with HasMemoryXilinxGenesys2MIGBundle {
  val outer: HasMemoryXilinxGenesys2MIG
  val ranges = AddressRange.fromSets(p(MemoryXilinxDDRKey).address)
  require (ranges.size == 1, "DDR range must be contiguous")
  val depth = ranges.head.size
  val xilinxgenesys2mig = IO(new XilinxGenesys2MIGIO(depth))

  xilinxgenesys2mig <> outer.xilinxgenesys2mig.module.io.port
}
