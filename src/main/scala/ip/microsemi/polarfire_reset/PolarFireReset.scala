// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfirereset

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box for Microsemi PolarFire IP block Actel:DirectCore:CORERESET_PF:2.1.100

trait PolarFireResetIOPads {
    val CLK             = Clock(INPUT)
    val EXT_RST_N       = Bool(INPUT)
    val FF_US_RESTORE   = Bool(INPUT)
    val INIT_DONE       = Bool(INPUT)
    val PLL_LOCK        = Bool(INPUT)
    val SS_BUSY         = Bool(INPUT)
    val FABRIC_RESET_N  = Bool(OUTPUT)
}

class PolarFireReset(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "polarfire_reset"

  val io = new Bundle with PolarFireResetIOPads
  
  ElaborationArtefacts.add(s"${desiredName}.libero.tcl",
    s"""create_design -id Actel:DirectCore:CORERESET_PF:2.1.100 -design_name {${desiredName}} -config_file {} -params {} -inhibit_configurator 0
       |open_smartdesign -design {${desiredName}}
       |configure_design -component {${desiredName}} -library {}
       |fix_vlnv_instance -component {${desiredName}} -library {} -name {${desiredName}_0}
       |open_smartdesign -design {${desiredName}}
       |configure_design -component {${desiredName}} -library {}
       |""".stripMargin)
}
