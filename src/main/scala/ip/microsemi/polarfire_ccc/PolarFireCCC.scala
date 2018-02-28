// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfireccc

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

case class PolarFireCCCParameters(
  name:             String,
  pll_in_freq:      Double  = 50,
  gl0Enabled:       Boolean = true,
  gl1Enabled:       Boolean = false,
  gl2Enabled:       Boolean = false,
  gl0_0_out_freq:   Double  = 111.111,
  gl1_0_out_freq:   Double  = 111.111,
  gl2_0_out_freq:   Double  = 111.111
)

// Black Box for Microsemi PolarFire Clock Conditioning Circuit (CCC) Actel:SgCore:PF_CCC:1.0.112

trait PolarFireCCCIOPads extends Bundle {

    val REF_CLK_0      = Clock(INPUT)
    val OUT0_FABCLK_0  = Clock(OUTPUT)
    val OUT1_FABCLK_0  = Clock(OUTPUT)
    val OUT2_FABCLK_0  = Clock(OUTPUT)
    val PLL_LOCK_0     = Bool(OUTPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class PolarFireCCC(c : PolarFireCCCParameters)(implicit val p:Parameters) extends BlackBox
{
  val modulename = c.name
  override def desiredName = c.name

  val io = new PolarFireCCCIOPads {
  }
  
  
  ElaborationArtefacts.add(
    "Libero." ++ modulename ++".tcl",
    """ 
create_design -id Actel:SgCore:PF_CCC:1.0.112 -design_name {""" ++ modulename ++"""} -config_file {} -params {} -inhibit_configurator 0
open_smartdesign -design {""" ++ modulename ++"""}
configure_design -component {""" ++ modulename ++"""} -library {} 
configure_vlnv_instance -component {""" ++ modulename ++"""} -library {} -name {""" ++ modulename ++"""_0} \
    -params {"PLL_IN_FREQ_0:""" ++ c.pll_in_freq.toString ++ """" \
             "GL0_0_IS_USED:""" ++ c.gl0Enabled.toString ++ """" \
             "GL0_0_OUT_FREQ:""" ++ c.gl0_0_out_freq.toString ++ """" \
             "GL1_0_IS_USED:""" ++ c.gl1Enabled.toString ++ """" \
             "GL1_0_OUT_FREQ:""" ++ c.gl1_0_out_freq.toString ++ """" \
             "GL2_0_IS_USED:""" ++ c.gl2Enabled.toString ++ """" \
             "GL2_0_OUT_FREQ:""" ++ c.gl2_0_out_freq.toString ++ """" \
            } -validate_rules 0 
fix_vlnv_instance -component {""" ++ modulename ++"""} -library {} -name {""" ++ modulename ++"""} 
open_smartdesign -design {""" ++ modulename ++"""}
configure_design -component {""" ++ modulename ++"""} -library {}"""
  )
}
//scalastyle:on
