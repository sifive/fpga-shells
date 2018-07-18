// See LICENSE for license details.
package sifive.fpgashells.devices.microsemi.polarfireevalkitpciex4

import Chisel._
//import freechips.rocketchip.coreplex.{HasInterruptBus, HasSystemBus}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, BufferParams}
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.tilelink.{TLAsyncCrossingSource, TLAsyncCrossingSink}
import freechips.rocketchip.interrupts.IntSyncCrossingSink

//trait HasSystemPolarFireEvalKitPCIeX4 extends HasSystemBus with HasInterruptBus {
trait HasSystemPolarFireEvalKitPCIeX4 { this: BaseSubsystem =>
  val pf_eval_kit_pcie = LazyModule(new PolarFireEvalKitPCIeX4)
  private val name = Some("polarfirepcie")

  sbus.fromMaster(name) { pf_eval_kit_pcie.crossTLOut } := pf_eval_kit_pcie.master
  pf_eval_kit_pcie.slave := sbus.toFixedWidthSlave(name) { pf_eval_kit_pcie.crossTLIn }
  pf_eval_kit_pcie.control := sbus.toFixedWidthSlave(name) { pf_eval_kit_pcie.crossTLIn }
  ibus.fromSync := pf_eval_kit_pcie.crossIntOut := pf_eval_kit_pcie.intnode
}

trait HasSystemPolarFireEvalKitPCIeX4Bundle {
  val pf_eval_kit_pcie: PolarFirePCIeX4Bundle
  def connectPolarFireEvalKitPCIeX4ToPads(pads: PolarFirePCIeX4Pads) {
    pads <> pf_eval_kit_pcie.pads
  }
}

trait HasSystemPolarFireEvalKitPCIeX4ModuleImp extends LazyModuleImp
    with HasSystemPolarFireEvalKitPCIeX4Bundle {
  val outer: HasSystemPolarFireEvalKitPCIeX4
  val pf_eval_kit_pcie = IO(new PolarFirePCIeX4Bundle)

  pf_eval_kit_pcie <> outer.pf_eval_kit_pcie.module.io

  outer.pf_eval_kit_pcie.module.clock := outer.pf_eval_kit_pcie.module.io.extra.AXI_CLK
}
