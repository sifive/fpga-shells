// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfireclockdivider

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box for Actel:SgCore:PF_CLK_DIV:1.0.101

trait PolarFireClockDividerIOPads extends Bundle {

    val CLK_OUT = Clock(OUTPUT)
    val CLK_IN    = Clock(INPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class PolarFireClockDivider(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "pf_clk_divider"

  val io = new PolarFireClockDividerIOPads {
  }
  
  ElaborationArtefacts.add(
    "Libero.polarfire_clock_divider.tcl",
    """ 
create_design -id Actel:SgCore:PF_CLK_DIV:1.0.101 -design_name {pf_clk_divider} -config_file {} -params {} -inhibit_configurator 0
open_smartdesign -design {pf_clk_divider}
configure_design -component {pf_clk_divider} -library {}
configure_vlnv_instance -component {pf_clk_divider} -library {} -name {pf_clk_divider_0} -params {"DIVIDER:2"} -validate_rules 0 
fix_vlnv_instance -component {pf_clk_divider} -library {} -name {pf_clk_divider_0} 
open_smartdesign -design {pf_clk_divider}
configure_design -component {pf_clk_divider} -library {} 
"""
  )
}
//scalastyle:on
