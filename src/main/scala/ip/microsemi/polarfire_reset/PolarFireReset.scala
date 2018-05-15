// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfirereset

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box for Microsemi PolarFire IP block Actel:DirectCore:CORERESET_PF:2.1.100

trait PolarFireResetIOPads extends Bundle {

    val CLK             = Clock(INPUT)
    val EXT_RST_N       = Bool(INPUT)
    val FF_US_RESTORE   = Bool(INPUT)
    val INIT_DONE       = Bool(INPUT)
    val PLL_LOCK        = Bool(INPUT)
    val SS_BUSY         = Bool(INPUT)
    val FABRIC_RESET_N  = Bool(OUTPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class PolarFireReset(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "polarfire_reset"

  val io = new PolarFireResetIOPads {
  }
  
  
  ElaborationArtefacts.add(
    "Libero.polarfire_reset.tcl",
    """ 
create_design -id Actel:DirectCore:CORERESET_PF:2.1.100 -design_name {polarfire_reset} -config_file {} -params {} -inhibit_configurator 0
open_smartdesign -design {polarfire_reset}
configure_design -component {polarfire_reset} -library {}
fix_vlnv_instance -component {polarfire_reset} -library {} -name {polarfire_reset_0}
open_smartdesign -design {polarfire_reset}
configure_design -component {polarfire_reset} -library {}"""
  )
}
//scalastyle:on
