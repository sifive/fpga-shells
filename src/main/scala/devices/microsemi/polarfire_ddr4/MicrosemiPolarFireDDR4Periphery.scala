// See LICENSE for license details.
package sifive.fpgashells.devices.microsemi.polarfireddr4

import Chisel._
import freechips.rocketchip.config._
//import freechips.rocketchip.coreplex.HasMemoryBus
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, AddressRange}

case object MemoryMicrosemiDDR4Key extends Field[PolarFireEvalKitDDR4Params]

//trait HasMemoryPolarFireEvalKitDDR4 extends HasMemoryBus {
trait HasMemoryPolarFireEvalKitDDR4 { this: BaseSubsystem =>
  val module: HasMemoryPolarFireEvalKitDDR4ModuleImp

  val polarfireddrsubsys = LazyModule(new PolarFireEvalKitDDR4(p(MemoryMicrosemiDDR4Key)))

  polarfireddrsubsys.node := mbus.toDRAMController(Some("PolarFireDDR"))()
}

trait HasMemoryPolarFireEvalKitDDR4Bundle {
  val polarfireddrsubsys: PolarFireEvalKitDDR4IO
  def connectPolarFireEValKitDDR4ToPads(pads: PolarFireEvalKitDDR4Pads) {
    pads <> polarfireddrsubsys
  }
}

trait HasMemoryPolarFireEvalKitDDR4ModuleImp extends LazyModuleImp
    with HasMemoryPolarFireEvalKitDDR4Bundle {
  val outer: HasMemoryPolarFireEvalKitDDR4
  val ranges = AddressRange.fromSets(p(MemoryMicrosemiDDR4Key).address)
  require (ranges.size == 1, "DDR range must be contiguous")
  val depth = ranges.head.size
  val polarfireddrsubsys = IO(new PolarFireEvalKitDDR4IO(depth))

  polarfireddrsubsys <> outer.polarfireddrsubsys.module.io.port
}
