// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfirexcvrrefclk

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box for Microsemi:SgCore:PF_XCVR_REF_CLK:1.0.103

trait PolarFireTransceiverRefClkIOPads {
    val REF_CLK_PAD_P   = Bool(INPUT)
    val REF_CLK_PAD_N   = Bool(INPUT)
    val REF_CLK         = Clock(OUTPUT)
    val FAB_REF_CLK     = Clock(OUTPUT)
}

class PolarFireTransceiverRefClk(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "transceiver_refclk"

  val io = new Bundle with PolarFireTransceiverRefClkIOPads
  
  ElaborationArtefacts.add(s"${desiredName}.libero.tcl",
    s"""create_design -id Actel:SgCore:PF_XCVR_REF_CLK:1.0.103 -design_name {${desiredName}} -config_file {} -params {} -inhibit_configurator 0
       |open_smartdesign -design {${desiredName}}
       |configure_design -component {${desiredName}} -library {}
       |configure_vlnv_instance -component {${desiredName}} -library {} -name {${desiredName}_0} -params { \\
       |  "ENABLE_FAB_CLK_0:1" \\
       |} -validate_rules 0
       |fix_vlnv_instance -component {${desiredName}} -library {} -name {${desiredName}_0}
       |open_smartdesign -design {${desiredName}}
       |configure_design -component {${desiredName}} -library {}
       |""".stripMargin)
}
