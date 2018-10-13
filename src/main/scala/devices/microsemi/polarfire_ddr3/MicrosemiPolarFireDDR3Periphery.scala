// See LICENSE for license details.
package sifive.fpgashells.devices.microsemi.polarfireddr3

import Chisel._
import freechips.rocketchip.config._
//import freechips.rocketchip.coreplex.HasMemoryBus
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, AddressRange}

case object MemoryMicrosemiDDR3Key extends Field[PolarFireEvalKitDDR3Params]

//trait HasMemoryPolarFireEvalKitDDR3 extends HasMemoryBus {
trait HasMemoryPolarFireEvalKitDDR3 { this: BaseSubsystem =>
  val module: HasMemoryPolarFireEvalKitDDR3ModuleImp

  val polarfireddrsubsys = LazyModule(new PolarFireEvalKitDDR3(p(MemoryMicrosemiDDR3Key)))

  polarfireddrsubsys.node := mbus.toDRAMController(Some("PolarFireDDR"))()
}

trait HasMemoryPolarFireEvalKitDDR3Bundle {
  val polarfireddrsubsys: PolarFireEvalKitDDR3IO
  def connectPolarFireEValKitDDR3ToPads(pads: PolarFireEvalKitDDR3Pads) {
    pads <> polarfireddrsubsys
  }
}

trait HasMemoryPolarFireEvalKitDDR3ModuleImp extends LazyModuleImp
    with HasMemoryPolarFireEvalKitDDR3Bundle {
  val outer: HasMemoryPolarFireEvalKitDDR3
  val ranges = AddressRange.fromSets(p(MemoryMicrosemiDDR3Key).address)
  require (ranges.size == 1, "DDR range must be contiguous")
  val depth = ranges.head.size
  val polarfireddrsubsys = IO(new PolarFireEvalKitDDR3IO(depth))

  polarfireddrsubsys <> outer.polarfireddrsubsys.module.io.port
}
