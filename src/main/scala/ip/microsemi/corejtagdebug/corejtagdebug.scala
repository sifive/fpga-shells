// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.corejtagdebug

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box for Microsemi DirectCore IP block Actel:DirectCore:COREJTAGDEBUG:2.0.100

trait CoreJtagDebugIOJTAGPads extends Bundle {

  val TCK       = Clock(INPUT)
  val TDI       = Bool(INPUT)
  val TMS       = Bool(INPUT)
  val TRSTB     = Bool(INPUT)
  val TDO       = Bool(OUTPUT)
}

trait CoreJtagDebugIOTarget extends Bundle {

  val TGT_TCK   = Clock(OUTPUT)   
  val TGT_TDI   = Bool(OUTPUT)
  val TGT_TMS   = Bool(OUTPUT)
  val TGT_TRST  = Bool(OUTPUT)
  val TGT_TDO   = Bool(INPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class CoreJtagDebugBlock(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "corejtagdebug_wrapper"

  val io = new CoreJtagDebugIOJTAGPads with CoreJtagDebugIOTarget {
    // chain inputs
    val UTDO_IN_0    = Bool(INPUT)
    val UTDO_IN_1    = Bool(INPUT)
    val UTDO_IN_2    = Bool(INPUT)
    val UTDO_IN_3    = Bool(INPUT)
    val UTDODRV_0    = Bool(INPUT)
    val UTDODRV_1    = Bool(INPUT)
    val UTDODRV_2    = Bool(INPUT)
    val UTDODRV_3    = Bool(INPUT)
    
    // chain outputs
    val UTDI_OUT     = Bool(OUTPUT)
    val URSTB_OUT    = Bool(OUTPUT)
    val UIREG_OUT    = Bits(OUTPUT,8)
    val UDRUPD_OUT   = Bool(OUTPUT)
    val UDRSH_OUT    = Bool(OUTPUT)    
    val UDRCK_OUT    = Bool(OUTPUT)
    val UDRCAP_OUT   = Bool(OUTPUT)
  }
  
  ElaborationArtefacts.add(
    "Libero.corejtagdebug.tcl",
    """ 
create_design -id Actel:DirectCore:COREJTAGDEBUG:2.0.100 -design_name {corejtagdebug_wrapper} -config_file {} -params {} -inhibit_configurator 0 
open_smartdesign -design {corejtagdebug_wrapper}
configure_design -component {corejtagdebug_wrapper} -library {} 
configure_vlnv_instance -component {corejtagdebug_wrapper} -library {} -name {corejtagdebug_wrapper_0} -params {"IR_CODE:0x55" "ACTIVE_HIGH_TGT_RESET:1"} -validate_rules 0 
fix_vlnv_instance -component {corejtagdebug_wrapper} -library {} -name {corejtagdebug_wrapper_0} 
open_smartdesign -design {corejtagdebug_wrapper}
configure_design -component {corejtagdebug_wrapper} -library {} 
"""
  )

}
//scalastyle:on
