// See LICENSE for license details.
package sifive.fpgashells.devices.microsemi.polarfireevalkitpciex4

import Chisel._
//import freechips.rocketchip.coreplex.{HasInterruptBus, HasSystemBus}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, BufferParams}
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.tilelink._

//trait HasSystemPolarFireEvalKitPCIeX4 extends HasSystemBus with HasInterruptBus {
trait HasSystemPolarFireEvalKitPCIeX4 { this: BaseSubsystem =>
  val pf_eval_kit_pcie = LazyModule(new PolarFireEvalKitPCIeX4)
  private val cname = "polarfirepcie"
  sbus.coupleFrom(s"master_named_$cname") { _ :=* TLFIFOFixer(TLFIFOFixer.all) :=* pf_eval_kit_pcie.crossTLOut(pf_eval_kit_pcie.master) }
  sbus.coupleTo(s"slave_named_$cname") { pf_eval_kit_pcie.crossTLIn(pf_eval_kit_pcie.slave) :*= TLWidthWidget(sbus.beatBytes) :*= _ }
  sbus.coupleTo(s"controller_named_$cname") { pf_eval_kit_pcie.crossTLIn(pf_eval_kit_pcie.control) :*= TLWidthWidget(sbus.beatBytes) :*= _ }
  ibus.fromSync := pf_eval_kit_pcie.crossIntOut(pf_eval_kit_pcie.intnode)
}

trait HasSystemPolarFireEvalKitPCIeX4Bundle {
  val pf_eval_kit_pcie: PolarFireEvalKitPCIeX4IO
  def connectPolarFireEvalKitPCIeX4ToPads(pads: PolarFireEvalKitPCIeX4Pads) {
    pads <> pf_eval_kit_pcie
  }
}

trait HasSystemPolarFireEvalKitPCIeX4ModuleImp extends LazyModuleImp
    with HasSystemPolarFireEvalKitPCIeX4Bundle {
  val outer: HasSystemPolarFireEvalKitPCIeX4
  val pf_eval_kit_pcie = IO(new PolarFireEvalKitPCIeX4IO)

  pf_eval_kit_pcie <> outer.pf_eval_kit_pcie.module.io.port

  outer.pf_eval_kit_pcie.module.clock := outer.pf_eval_kit_pcie.module.io.port.AXI_CLK
}
