// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfireglitchlessmux

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box forMicrosemi PolarFire glitchless mux Actel:SgCore:PF_NGMUX:1.0.101

trait PolarFireGlitchlessMuxIOPads {
    val CLK_OUT = Clock(OUTPUT)
    val CLK0    = Clock(INPUT)
    val CLK1    = Clock(INPUT)
    val SEL     = Bool(INPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class PolarFireGlitchlessMux(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "pf_glitchless_mux"

  val io = new Bundle with PolarFireGlitchlessMuxIOPads
  
  ElaborationArtefacts.add(s"${desiredName}.libero.tcl",
    s"""create_design -id Actel:SgCore:PF_NGMUX:1.0.101 -design_name {${desiredName}} -config_file {} -params {} -inhibit_configurator 0
       |open_smartdesign -design {${desiredName}}
       |configure_design -component {${desiredName}} -library {}
       |fix_vlnv_instance -component {${desiredName}} -library {} -name {${desiredName}_0}
       |open_smartdesign -design {${desiredName}}
       |configure_design -component {${desiredName}} -library {}
       |""".stripMargin)
}
