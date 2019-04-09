// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfireinitmonitor

import Chisel._
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.config._

// Black Box for Microsemi PolarFire Clock Conditioning Circuit (CCC) Actel:SgCore:PF_INIT_MONITOR:2.0.103

trait PolarFireInitMonitorIOPads {
  val DEVICE_INIT_DONE  = Bool(OUTPUT)
  val FABRIC_POR_N      = Bool(OUTPUT)
  val PCIE_INIT_DONE    = Bool(OUTPUT)
  val SRAM_INIT_DONE    = Bool(OUTPUT)
  val USRAM_INIT_DONE   = Bool(OUTPUT)
}

class PolarFireInitMonitor(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "polarfire_init_monitor"

  val io = new Bundle with PolarFireInitMonitorIOPads

  ElaborationArtefacts.add(s"${desiredName}.libero.tcl",
    s"""create_design -id Actel:SgCore:PF_INIT_MONITOR:2.0.103 -design_name {polarfire_init_monitor} -config_file {} -params {} -inhibit_configurator 0
       |open_smartdesign -design {polarfire_init_monitor}
       |configure_design -component {polarfire_init_monitor} -library {}
       |fix_vlnv_instance -component {polarfire_init_monitor} -library {} -name {polarfire_init_monitor_0}
       |open_smartdesign -design {polarfire_init_monitor}
       |configure_design -component {polarfire_init_monitor} -library {}
       |""".stripMargin)
}  
