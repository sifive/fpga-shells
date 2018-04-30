// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfirexcvrrefclk

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box for Microsemi:SgCore:PF_XCVR_REF_CLK:1.0.103

trait PolarFireTransceiverRefClkIOPads extends Bundle {

    val REF_CLK_PAD_P   = Bool(INPUT)
    val REF_CLK_PAD_N   = Bool(INPUT)
    val REF_CLK         = Clock(OUTPUT)
    val FAB_REF_CLK     = Clock(OUTPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class PolarFireTransceiverRefClk(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "transceiver_refclk"

  val io = new PolarFireTransceiverRefClkIOPads {
  }
  
  ElaborationArtefacts.add(
    "Libero.polarfire_xcvr_refclk.tcl",
    """ 
create_design -id Actel:SgCore:PF_XCVR_REF_CLK:1.0.103 -design_name {transceiver_refclk} -config_file {} -params {} -inhibit_configurator 0
open_smartdesign -design {transceiver_refclk}
configure_design -component {transceiver_refclk} -library {} 
configure_vlnv_instance -component {transceiver_refclk} -library {} -name {transceiver_refclk_0} \
    -params {"ENABLE_FAB_CLK_0:1" \
            } -validate_rules 0 

fix_vlnv_instance -component {transceiver_refclk} -library {} -name {transceiver_refclk_0} 
open_smartdesign -design {transceiver_refclk}
configure_design -component {transceiver_refclk} -library {} 
"""
  )

}
//scalastyle:on
