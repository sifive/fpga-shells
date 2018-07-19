// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfiretxpll

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box for Microsemi:SgCore:PF_TX_PLL:1.0.109

trait PolarFireTxPLLIOPads {
    val REF_CLK         = Clock(INPUT)
    val BIT_CLK         = Clock(OUTPUT)
    val CLK_125         = Clock(OUTPUT)
    val REF_CLK_TO_LANE = Clock(OUTPUT)
    val LOCK            = Bool(OUTPUT)
    val PLL_LOCK        = Bool(OUTPUT)
}

class PolarFireTxPLL(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "transmit_pll"
  val io = new Bundle with PolarFireTxPLLIOPads

  ElaborationArtefacts.add(s"${desiredName}.libero.tcl",
    s"""create_design -id Actel:SgCore:PF_TX_PLL:1.0.112 -design_name {${desiredName}} -config_file {} -params {} -inhibit_configurator 0
       |open_smartdesign -design {${desiredName}}
       |configure_design -component {${desiredName}} -library {}
       |configure_vlnv_instance -component {${desiredName}} -library {} -name {${desiredName}_0} -params {"TxPLL_REF:100" "TxPLL_OUT:2500"} -validate_rules 0
       |fix_vlnv_instance -component {${desiredName}} -library {} -name {${desiredName}_0}
       |open_smartdesign -design {${desiredName}}
       |configure_design -component {${desiredName}} -library {}
       |""".stripMargin)
}
