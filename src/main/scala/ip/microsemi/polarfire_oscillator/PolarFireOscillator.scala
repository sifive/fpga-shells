// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfire_oscillator

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box for Microsemi PolarFire internal oscillator Actel:SgCore:PF_OSC:1.0.102

trait PolarFireOscillatorIOPads {
    val RCOSC_160MHZ_GL = Clock(OUTPUT)
}

class PolarFireOscillator(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "pf_oscillator"

  val io = new Bundle with PolarFireOscillatorIOPads
  
  ElaborationArtefacts.add(s"${desiredName}.libero.tcl",
    s"""create_design -id Actel:SgCore:PF_OSC:1.0.102 -design_name {${desiredName}} -config_file {} -params {} -inhibit_configurator 0
       |open_smartdesign -design {${desiredName}}
       |configure_design -component {${desiredName}} -library {}
       |fix_vlnv_instance -component {${desiredName}} -library {} -name {${desiredName}_0}
       |open_smartdesign -design {${desiredName}}
       |configure_design -component {${desiredName}} -library {}
       |""".stripMargin)
}
