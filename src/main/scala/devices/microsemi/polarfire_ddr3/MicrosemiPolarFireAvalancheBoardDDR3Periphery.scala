// See LICENSE for license details.
package sifive.fpgashells.devices.microsemi.polarfireddr3

import Chisel._
import freechips.rocketchip.config._
//import freechips.rocketchip.coreplex.HasMemoryBus
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, AddressRange}

case object MemoryMicrosemiAvalancheBoardDDR3Key extends Field[PolarFireAvalancheBoardDDR3Params]

//trait HasMemoryPolarFireAvalancheBoardDDR3 extends HasMemoryBus {
trait HasMemoryPolarFireAvalancheBoardDDR3 { this: BaseSubsystem =>
  val module: HasMemoryPolarFireAvalancheBoardDDR3ModuleImp

  val polarfireddrsubsys = LazyModule(new PolarFireAvalancheBoardDDR3(p(MemoryMicrosemiAvalancheBoardDDR3Key)))

  polarfireddrsubsys.node := mbus.toDRAMController(Some("PolarFireDDR"))()
}

trait HasMemoryPolarFireAvalancheBoardDDR3Bundle {
  val polarfireddrsubsys: PolarFireAvalancheBoardDDR3IO
  def connectPolarFireAvalancheBoardDDR3ToPads(pads: PolarFireAvalancheBoardDDR3Pads) {
    pads <> polarfireddrsubsys
  }
}

trait HasMemoryPolarFireAvalancheBoardDDR3ModuleImp extends LazyModuleImp
    with HasMemoryPolarFireAvalancheBoardDDR3Bundle {
  val outer: HasMemoryPolarFireAvalancheBoardDDR3
  val ranges = AddressRange.fromSets(p(MemoryMicrosemiAvalancheBoardDDR3Key).address)
  require (ranges.size == 1, "DDR range must be contiguous")
  val depth = ranges.head.size
  val polarfireddrsubsys = IO(new PolarFireAvalancheBoardDDR3IO(depth))

  polarfireddrsubsys <> outer.polarfireddrsubsys.module.io.port
}
