// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfiretxpll

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box for Microsemi:SgCore:PF_TX_PLL:1.0.109

trait PolarFireTxPLLIOPads extends Bundle {

    val REF_CLK         = Clock(INPUT)
    val BIT_CLK         = Clock(OUTPUT)
    val CLK_125         = Clock(OUTPUT)
    val REF_CLK_TO_LANE = Clock(OUTPUT)
    val LOCK            = Bool(OUTPUT)
    val PLL_LOCK        = Bool(OUTPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class PolarFireTxPLL(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "transmit_pll"

  val io = new PolarFireTxPLLIOPads {
  }

  ElaborationArtefacts.add(
    "Libero.polarfire_tx_pll.tcl",
    """ 
create_design -id Actel:SgCore:PF_TX_PLL:1.0.109 -design_name {transmit_pll} -config_file {} -params {} -inhibit_configurator 0
open_smartdesign -design {transmit_pll}
configure_design -component {transmit_pll} -library {}
configure_vlnv_instance -component {transmit_pll} -library {} -name {transmit_pll_0} -params {"TxPLL_REF:100" "TxPLL_OUT:2500"} -validate_rules 0 
fix_vlnv_instance -component {transmit_pll} -library {} -name {transmit_pll_0} 
open_smartdesign -design {transmit_pll}
configure_design -component {transmit_pll} -library {} 
"""
  )
  
}
//scalastyle:on
