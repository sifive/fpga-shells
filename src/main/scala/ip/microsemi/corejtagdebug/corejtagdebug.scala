// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.corejtagdebug

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box for Microsemi DirectCore IP block Actel:DirectCore:COREJTAGDEBUG:3.0.100

trait CoreJtagDebugIOJTAGPads extends Bundle {

  val TCK       = Clock(INPUT)
  val TDI       = Bool(INPUT)
  val TMS       = Bool(INPUT)
  val TRSTB     = Bool(INPUT)
  val TDO       = Bool(OUTPUT)
}

trait CoreJtagDebugIOTarget extends Bundle {

  val TGT_TCK_0    = Clock(OUTPUT)   
  val TGT_TDI_0    = Bool(OUTPUT)
  val TGT_TMS_0    = Bool(OUTPUT)
  val TGT_TRSTB_0  = Bool(OUTPUT)
  val TGT_TDO_0    = Bool(INPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class CoreJtagDebugBlock(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "corejtagdebug_wrapper"

  val io = new CoreJtagDebugIOJTAGPads with CoreJtagDebugIOTarget { }
  
  ElaborationArtefacts.add(
    "Libero.corejtagdebug.tcl",
    """ 
 
create_design -id Actel:DirectCore:COREJTAGDEBUG:3.0.100 -design_name {corejtagdebug_wrapper} -config_file {} -params {} -inhibit_configurator 0 
open_smartdesign -design {corejtagdebug_wrapper}
configure_design -component {corejtagdebug_wrapper} -library {} 
configure_vlnv_instance -component {corejtagdebug_wrapper} -library {} -name {corejtagdebug_wrapper_0} -params {"FAMILY:26"  \
"IR_CODE_TGT_0:0x55"  \
"IR_CODE_TGT_1:0x56"  \
"IR_CODE_TGT_2:0x57"  \
"IR_CODE_TGT_3:0x58"  \
"IR_CODE_TGT_4:0x59"  \
"IR_CODE_TGT_5:0x5a"  \
"IR_CODE_TGT_6:0x5b"  \
"IR_CODE_TGT_7:0x5c"  \
"IR_CODE_TGT_8:0x5d"  \
"IR_CODE_TGT_9:0x5e"  \
"IR_CODE_TGT_10:0x5f"  \
"IR_CODE_TGT_11:0x60"  \
"IR_CODE_TGT_12:0x61"  \
"IR_CODE_TGT_13:0x62"  \
"IR_CODE_TGT_14:0x63"  \
"IR_CODE_TGT_15:0x64"  \
"NUM_DEBUG_TGTS:1"  \
"Testbench:User"  \
"TGT_ACTIVE_HIGH_RESET_0:true"  \
"TGT_ACTIVE_HIGH_RESET_1:true"  \
"TGT_ACTIVE_HIGH_RESET_2:true"  \
"TGT_ACTIVE_HIGH_RESET_3:true"  \
"TGT_ACTIVE_HIGH_RESET_4:true"  \
"TGT_ACTIVE_HIGH_RESET_5:true"  \
"TGT_ACTIVE_HIGH_RESET_6:true"  \
"TGT_ACTIVE_HIGH_RESET_7:true"  \
"TGT_ACTIVE_HIGH_RESET_8:true"  \
"TGT_ACTIVE_HIGH_RESET_9:true"  \
"TGT_ACTIVE_HIGH_RESET_10:true"  \
"TGT_ACTIVE_HIGH_RESET_11:true"  \
"TGT_ACTIVE_HIGH_RESET_12:true"  \
"TGT_ACTIVE_HIGH_RESET_13:true"  \
"TGT_ACTIVE_HIGH_RESET_14:true"  \
"TGT_ACTIVE_HIGH_RESET_15:true"  \
"UJTAG_BYPASS:false" } -validate_rules 0 
fix_vlnv_instance -component {corejtagdebug_wrapper} -library {} -name {corejtagdebug_wrapper_0} 
open_smartdesign -design {corejtagdebug_wrapper}
configure_design -component {corejtagdebug_wrapper} -library {}
"""
  )

}
//scalastyle:on
