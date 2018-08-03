// See LICENSE for license details.
package sifive.fpgashells.shell.microsemi.polarfireevalkitshell

import Chisel._
import chisel3.core.{Input, Output, attach}
import chisel3.experimental.{RawModule, Analog, withClockAndReset}

import freechips.rocketchip.config._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.util.{SyncResetSynchronizerShiftReg, ResetCatchAndSync, ElaborationArtefacts, HeterogeneousBag}

import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._

import sifive.fpgashells.ip.microsemi.{CLKINT}

import sifive.fpgashells.devices.microsemi.polarfireddr3._
import sifive.fpgashells.devices.microsemi.polarfireddr4._

import sifive.fpgashells.ip.microsemi.corejtagdebug._
import sifive.fpgashells.ip.microsemi.polarfireccc._
import sifive.fpgashells.ip.microsemi.polarfireinitmonitor._
import sifive.fpgashells.ip.microsemi.polarfirereset._

import sifive.fpgashells.devices.microsemi.polarfireevalkitpciex4._
import sifive.fpgashells.ip.microsemi.polarfirexcvrrefclk._
import sifive.fpgashells.ip.microsemi.polarfiretxpll._

import sifive.fpgashells.ip.microsemi.polarfire_oscillator._
import sifive.fpgashells.ip.microsemi.polarfireclockdivider._
import sifive.fpgashells.ip.microsemi.polarfireglitchlessmux._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.clocks._


//-------------------------------------------------------------------------
// PolarFire Evaluation Kit Shell
//-------------------------------------------------------------------------

trait HasDDR3 { this: PolarFireEvalKitShell =>
  
  require(!p.lift(MemoryMicrosemiDDR3Key).isEmpty)
  val ddr = IO(new PolarFireEvalKitDDR3Pads(p(MemoryMicrosemiDDR3Key)))
  
  def connectMIG(dut: HasMemoryPolarFireEvalKitDDR3ModuleImp): Unit = {
    // Clock & Reset
    dut.polarfireddrsubsys.PLL_REF_CLK := mig_clock_in
    dut.polarfireddrsubsys.SYS_RESET_N := sys_reset_n
    
    mig_clock_out       := dut.polarfireddrsubsys.SYS_CLK
    mig_plllock_out     := dut.polarfireddrsubsys.PLL_LOCK
    ddr_ctrlr_ready     := dut.polarfireddrsubsys.CTRLR_READY
    
    ddr <> dut.polarfireddrsubsys
  }
}

trait HasDDR4 { this: PolarFireEvalKitShell =>
  
  require(!p.lift(MemoryMicrosemiDDR4Key).isEmpty)
  val ddr = IO(new PolarFireEvalKitDDR4Pads(p(MemoryMicrosemiDDR4Key)))
  
  def connectMIG(dut: HasMemoryPolarFireEvalKitDDR4ModuleImp): Unit = {
    // Clock & Reset
    dut.polarfireddrsubsys.PLL_REF_CLK := mig_clock_in
    dut.polarfireddrsubsys.SYS_RESET_N := sys_reset_n
    
    mig_clock_out       := dut.polarfireddrsubsys.SYS_CLK
    mig_plllock_out     := dut.polarfireddrsubsys.PLL_LOCK
    ddr_ctrlr_ready     := dut.polarfireddrsubsys.CTRLR_READY
    
    ddr <> dut.polarfireddrsubsys
  }
}

trait HasPCIe { this: PolarFireEvalKitShell =>
  val pcie = IO(new PolarFireEvalKitPCIeX4Pads)

  def connectPCIe(dut: HasSystemPolarFireEvalKitPCIeX4ModuleImp): Unit = {
    // Clock & Reset
//    dut.pf_eval_kit_pcie.APB_S_PCLK     := hart_clk
    dut.pf_eval_kit_pcie.APB_S_PCLK     := dut_clock
    dut.pf_eval_kit_pcie.APB_S_PRESET_N := UInt("b1")
    
//    dut.pf_eval_kit_pcie.AXI_CLK        := hart_clk_150
    dut.pf_eval_kit_pcie.AXI_CLK        := dut_clock
    dut.pf_eval_kit_pcie.AXI_CLK_STABLE := hart_clk_lock
    
    dut.pf_eval_kit_pcie.PCIE_1_TL_CLK_125MHz   := pcie_tl_clk
    
    dut.pf_eval_kit_pcie.PCIE_1_TX_PLL_REF_CLK  := pf_tx_pll_refclk_to_lane

    dut.pf_eval_kit_pcie.PCIE_1_TX_BIT_CLK := pf_tx_pll_bitclk
    
    dut.pf_eval_kit_pcie.PCIESS_LANE0_CDR_REF_CLK_0 := pcie_refclk
    dut.pf_eval_kit_pcie.PCIESS_LANE1_CDR_REF_CLK_0 := pcie_refclk
    dut.pf_eval_kit_pcie.PCIESS_LANE2_CDR_REF_CLK_0 := pcie_refclk
    dut.pf_eval_kit_pcie.PCIESS_LANE3_CDR_REF_CLK_0 := pcie_refclk

    dut.pf_eval_kit_pcie.PCIE_1_TX_PLL_LOCK := pf_tx_pll_lock

    pcie <> dut.pf_eval_kit_pcie
  }
}
/*
trait HasCoreJTAGDebug { this: PolarFireEvalKitShell =>
  // JTAG inside the FPGA fabric through user JTAG FPGA macro (UJTAG)
  val fpga_jtag = Module(new CoreJtagDebugBlock)
  
  fpga_jtag.io.UTDO_IN_0   := UInt("b0")
  fpga_jtag.io.UTDO_IN_1   := UInt("b0")
  fpga_jtag.io.UTDO_IN_2   := UInt("b0")
  fpga_jtag.io.UTDO_IN_3   := UInt("b0")
  fpga_jtag.io.UTDODRV_0   := UInt("b0")
  fpga_jtag.io.UTDODRV_1   := UInt("b0")
  fpga_jtag.io.UTDODRV_2   := UInt("b0")
  fpga_jtag.io.UTDODRV_3   := UInt("b0")

}
*/


trait HasPFEvalKitChipLink { this: PolarFireEvalKitShell =>

  val chiplink = IO(new WideDataLayerPort(ChipLinkParams(Nil,Nil)))
  val ereset_n = IO(Bool(INPUT))

  def constrainChipLink(iofpga: Boolean = false): Unit = {
    val direction0Pins = if(iofpga) "chiplink_b2c"  else "chiplink_c2b"
    val direction1Pins = if(iofpga) "chiplink_c2b"  else "chiplink_b2c"
/*
    ElaborationArtefacts.add(
      """vc707chiplink.vivado.tcl""",
      """set vc707chiplink_vivado_tcl_dir [file dirname [file normalize [info script]]]
         add_files -fileset [current_fileset -constrset] [glob -directory $vc707chiplink_vivado_tcl_dir {*.vc707chiplink.xdc}]"""
    )
*/

/*
    ElaborationArtefacts.add(
      """vc707chiplink.xdc""", s"""
        set_property PACKAGE_PIN AF39 [get_ports ${direction0Pins}_clk]
        set_property PACKAGE_PIN AD40 [get_ports {${direction0Pins}_data[0]}]
        set_property PACKAGE_PIN AD41 [get_ports {${direction0Pins}_data[1]}]
        set_property PACKAGE_PIN AF41 [get_ports {${direction0Pins}_data[2]}]
        set_property PACKAGE_PIN AG41 [get_ports {${direction0Pins}_data[3]}]
        set_property PACKAGE_PIN AK39 [get_ports {${direction0Pins}_data[4]}]
        set_property PACKAGE_PIN AL39 [get_ports {${direction0Pins}_data[5]}]
        set_property PACKAGE_PIN AJ42 [get_ports {${direction0Pins}_data[6]}]
        set_property PACKAGE_PIN AK42 [get_ports {${direction0Pins}_data[7]}]
        set_property PACKAGE_PIN AL41 [get_ports {${direction0Pins}_data[8]}]
        set_property PACKAGE_PIN AL42 [get_ports {${direction0Pins}_data[9]}]
        set_property PACKAGE_PIN AF42 [get_ports {${direction0Pins}_data[10]}]
        set_property PACKAGE_PIN AG42 [get_ports {${direction0Pins}_data[11]}]
        set_property PACKAGE_PIN AD38 [get_ports {${direction0Pins}_data[12]}]
        set_property PACKAGE_PIN AE38 [get_ports {${direction0Pins}_data[13]}]
        set_property PACKAGE_PIN AC40 [get_ports {${direction0Pins}_data[14]}]
        set_property PACKAGE_PIN AC41 [get_ports {${direction0Pins}_data[15]}]
        set_property PACKAGE_PIN AD42 [get_ports {${direction0Pins}_data[16]}]
        set_property PACKAGE_PIN AE42 [get_ports {${direction0Pins}_data[17]}]
        set_property PACKAGE_PIN AJ38 [get_ports {${direction0Pins}_data[18]}]
        set_property PACKAGE_PIN AK38 [get_ports {${direction0Pins}_data[19]}]
        set_property PACKAGE_PIN AB41 [get_ports {${direction0Pins}_data[20]}]
        set_property PACKAGE_PIN AB42 [get_ports {${direction0Pins}_data[21]}]
        set_property PACKAGE_PIN Y42  [get_ports {${direction0Pins}_data[22]}]
        set_property PACKAGE_PIN AA42 [get_ports {${direction0Pins}_data[23]}]
        set_property PACKAGE_PIN Y39  [get_ports {${direction0Pins}_data[24]}]
        set_property PACKAGE_PIN AA39 [get_ports {${direction0Pins}_data[25]}]
        set_property PACKAGE_PIN W40  [get_ports {${direction0Pins}_data[26]}]
        set_property PACKAGE_PIN Y40  [get_ports {${direction0Pins}_data[27]}]
        set_property PACKAGE_PIN AB38 [get_ports {${direction0Pins}_data[28]}]
        set_property PACKAGE_PIN AB39 [get_ports {${direction0Pins}_data[29]}]
        set_property PACKAGE_PIN AC38 [get_ports {${direction0Pins}_data[30]}]
        set_property PACKAGE_PIN AC39 [get_ports {${direction0Pins}_data[31]}]
        set_property PACKAGE_PIN AJ40 [get_ports ${direction0Pins}_send]
        set_property PACKAGE_PIN AJ41 [get_ports ${direction0Pins}_rst]

        set_property PACKAGE_PIN U39 [get_ports ${direction1Pins}_clk]
        set_property PACKAGE_PIN U37 [get_ports {${direction1Pins}_data[0]}]
        set_property PACKAGE_PIN U38 [get_ports {${direction1Pins}_data[1]}]
        set_property PACKAGE_PIN U36 [get_ports {${direction1Pins}_data[2]}]
        set_property PACKAGE_PIN T37 [get_ports {${direction1Pins}_data[3]}]
        set_property PACKAGE_PIN U32 [get_ports {${direction1Pins}_data[4]}]
        set_property PACKAGE_PIN U33 [get_ports {${direction1Pins}_data[5]}]
        set_property PACKAGE_PIN V33 [get_ports {${direction1Pins}_data[6]}]
        set_property PACKAGE_PIN V34 [get_ports {${direction1Pins}_data[7]}]
        set_property PACKAGE_PIN P35 [get_ports {${direction1Pins}_data[8]}]
        set_property PACKAGE_PIN P36 [get_ports {${direction1Pins}_data[9]}]
        set_property PACKAGE_PIN W32 [get_ports {${direction1Pins}_data[10]}]
        set_property PACKAGE_PIN W33 [get_ports {${direction1Pins}_data[11]}]
        set_property PACKAGE_PIN R38 [get_ports {${direction1Pins}_data[12]}]
        set_property PACKAGE_PIN R39 [get_ports {${direction1Pins}_data[13]}]
        set_property PACKAGE_PIN U34 [get_ports {${direction1Pins}_data[14]}]
        set_property PACKAGE_PIN T35 [get_ports {${direction1Pins}_data[15]}]
        set_property PACKAGE_PIN R33 [get_ports {${direction1Pins}_data[16]}]
        set_property PACKAGE_PIN R34 [get_ports {${direction1Pins}_data[17]}]
        set_property PACKAGE_PIN N33 [get_ports {${direction1Pins}_data[18]}]
        set_property PACKAGE_PIN N34 [get_ports {${direction1Pins}_data[19]}]
        set_property PACKAGE_PIN P32 [get_ports {${direction1Pins}_data[20]}]
        set_property PACKAGE_PIN P33 [get_ports {${direction1Pins}_data[21]}]
        set_property PACKAGE_PIN V35 [get_ports {${direction1Pins}_data[22]}]
        set_property PACKAGE_PIN V36 [get_ports {${direction1Pins}_data[23]}]
        set_property PACKAGE_PIN W36 [get_ports {${direction1Pins}_data[24]}]
        set_property PACKAGE_PIN W37 [get_ports {${direction1Pins}_data[25]}]
        set_property PACKAGE_PIN T32 [get_ports {${direction1Pins}_data[26]}]
        set_property PACKAGE_PIN R32 [get_ports {${direction1Pins}_data[27]}]
        set_property PACKAGE_PIN V39 [get_ports {${direction1Pins}_data[28]}]
        set_property PACKAGE_PIN V40 [get_ports {${direction1Pins}_data[29]}]
        set_property PACKAGE_PIN P37 [get_ports {${direction1Pins}_data[30]}]
        set_property PACKAGE_PIN P38 [get_ports {${direction1Pins}_data[31]}]

        set_property PACKAGE_PIN T36 [get_ports ${direction1Pins}_send]
        set_property PACKAGE_PIN R37 [get_ports ${direction1Pins}_rst]

        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[31]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[30]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[29]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[28]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[27]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[26]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[25]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[24]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[23]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[22]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[21]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[20]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[19]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[18]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[17]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[16]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[15]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[14]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[13]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[12]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[11]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[10]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[9]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[8]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[7]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[6]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[5]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[4]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[3]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[2]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[1]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction0Pins}_data[0]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[31]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[30]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[29]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[28]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[27]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[26]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[25]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[24]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[23]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[22]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[21]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[20]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[19]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[18]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[17]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[16]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[15]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[14]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[13]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[12]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[11]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[10]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[9]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[8]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[7]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[6]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[5]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[4]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[3]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[2]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[1]}]
        set_property IOSTANDARD LVCMOS18 [get_ports {${direction1Pins}_data[0]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[31]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[30]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[29]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[28]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[27]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[26]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[25]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[24]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[23]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[22]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[21]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[20]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[19]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[18]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[17]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[16]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[15]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[14]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[13]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[12]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[11]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[10]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[9]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[8]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[7]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[6]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[5]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[4]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[3]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[2]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[1]}]
        set_property SLEW FAST [get_ports {${direction1Pins}_data[0]}]


        set_property IOSTANDARD LVCMOS18 [get_ports ${direction0Pins}_clk]
        set_property IOSTANDARD LVCMOS18 [get_ports ${direction0Pins}_rst]
        set_property IOSTANDARD LVCMOS18 [get_ports ${direction0Pins}_send]
        set_property IOSTANDARD LVCMOS18 [get_ports ${direction1Pins}_clk]
        set_property IOSTANDARD LVCMOS18 [get_ports ${direction1Pins}_rst]
        set_property IOSTANDARD LVCMOS18 [get_ports ${direction1Pins}_send]
        set_property SLEW FAST [get_ports ${direction1Pins}_clk]
        set_property SLEW FAST [get_ports ${direction1Pins}_rst]
        set_property SLEW FAST [get_ports ${direction1Pins}_send]


        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[31]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[30]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[29]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[28]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[27]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[26]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[25]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[24]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[23]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[22]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[21]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[20]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[19]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[18]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[17]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[16]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[15]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[14]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[13]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[12]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[11]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[10]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[9]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[8]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[7]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[6]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[5]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[4]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[3]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[2]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[1]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_data[0]]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_send]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_clk]
        set_property OFFCHIP_TERM NONE [get_ports ${direction1Pins}_rst]

        # Aloe reset sent to FPGA
        set_property IOSTANDARD LVCMOS18 [get_ports ereset_n]
        set_property PACKAGE_PIN AF40    [get_ports ereset_n]

        #Put first level RX/TX flops in IOB
        set_property IOB TRUE [get_cells -of_objects [all_fanout -flat -endpoints_only [get_ports "chiplink_b2c_data*"]]]
        set_property IOB TRUE [get_cells -of_objects [all_fanout -flat -endpoints_only [get_ports "chiplink_b2c_send"]]]
        set_property IOB TRUE [get_cells -of_objects [all_fanin -flat -startpoints_only [get_ports "chiplink_c2b_data*"]]]
        set_property IOB TRUE [get_cells -of_objects [all_fanin -flat -startpoints_only [get_ports "chiplink_c2b_send"]]]
"""
    )
*/
  }

  def connectChipLink(dut: { val chiplink: Seq[WideDataLayerPort] } , iofpga: Boolean = false): Unit = {
    constrainChipLink(iofpga)

    chiplink <> dut.chiplink(0)
    //dut.chiplink_xilinx_7series_phy.get.idelayctrl_refclk := sys_clock
  }
}

abstract class PolarFireEvalKitShell(implicit val p: Parameters) extends RawModule {

  //-----------------------------------------------------------------------
  // Interface
  //-----------------------------------------------------------------------
  
  // 50MHz crystal oscillator
  val ref_clk0             = IO(Input(Clock()))

  // 
  val ref_clk_pad_p = IO(Input(Bool()))
  val ref_clk_pad_n = IO(Input(Bool()))
  
  // Reset push-button - active low
  val pf_user_reset_n      = IO(Input(Bool()))
  
  // LED
  val led                  = IO(Vec(8, Output(Bool())))

  // UART
  val uart_tx              = IO(Output(Bool()))
  val uart_rx              = IO(Input(Bool()))

  // SPI Flash
  val spi_flash_reset      = IO(Output(Bool()))
  val spi_flash_sdo        = IO(Output(Bool()))
  val spi_flash_sdi        = IO(Input(Bool()))
  val spi_flash_sck        = IO(Output(Bool()))
  val spi_flash_ss         = IO(Output(Bool()))
  val spi_flash_wp         = IO(Output(Bool()))
  val spi_flash_hold       = IO(Output(Bool()))
  
  // JTAG
  val jtag_TRSTB           = IO(Input(Bool()))
  val jtag_TCK             = IO(Input(Clock()))
  val jtag_TMS             = IO(Input(Bool()))
  val jtag_TDI             = IO(Input(Bool()))
  val jtag_TDO             = IO(Output(Bool()))

  //Buttons
  val btn_0                = IO(Analog(1.W))
  val btn_1                = IO(Analog(1.W))
  val btn_2                = IO(Analog(1.W))
  val btn_3                = IO(Analog(1.W))

  //Sliding switches
  val sw_0                 = IO(Analog(1.W))
  val sw_1                 = IO(Analog(1.W))
  val sw_2                 = IO(Analog(1.W))
  val sw_3                 = IO(Analog(1.W))
  val sw_4                 = IO(Analog(1.W))
  val sw_5                 = IO(Analog(1.W))
  val sw_6                 = IO(Analog(1.W))
  val sw_7                 = IO(Analog(1.W))

  // debug
  val debug_io0            = IO(Output(Bool()))
  val debug_io1            = IO(Output(Clock()))
  val debug_io2            = IO(Output(Bool()))
  val debug_io3            = IO(Output(Bool()))
  val debug_io4            = IO(Output(Clock()))
  val debug_io5            = IO(Output(Bool()))
  
  //-----------------------------------------------------------------------
  // Wire declarations
  //-----------------------------------------------------------------------

  val sys_clock       = Wire(Clock())
  val sys_reset_n     = Wire(Bool())

  val dut_clock       = Wire(Clock())
  val dut_reset       = Wire(Bool())
  val dut_reset_i     = Wire(Bool())
  val dut_reset_sync  = Wire(Bool())

  val dut_ndreset     = Wire(Bool())

  val mig_mmcm_locked = Wire(Bool())
  val mig_sys_reset   = Wire(Bool())

  val mig_clock       = Wire(Clock())
  val mig_reset       = Wire(Bool())
  val mig_resetn      = Wire(Bool())
  
  val mig_clock_in    = Wire(Clock())
  val mig_clock_out   = Wire(Clock())
  val mig_plllock_out = Wire(Bool())

  val pcie_dat_reset  = Wire(Bool())
  val pcie_dat_resetn = Wire(Bool())
  val pcie_cfg_reset  = Wire(Bool())
  val pcie_cfg_resetn = Wire(Bool())
  val pcie_dat_clock  = Wire(Clock())
  val pcie_cfg_clock  = Wire(Clock())
  val mmcm_lock_pcie  = Wire(Bool())

  val fpga_reset      = Wire(Bool())
  val ddr_ctrlr_ready = Wire(Bool())
  
  //-----------------------------------------------------------------------
  // Differential clock
  //-----------------------------------------------------------------------
  val pf_xcvr_ref_clk = Module(new PolarFireTransceiverRefClk)
  pf_xcvr_ref_clk.io.REF_CLK_PAD_P := ref_clk_pad_p
  pf_xcvr_ref_clk.io.REF_CLK_PAD_N := ref_clk_pad_n
  val pcie_refclk = pf_xcvr_ref_clk.io.REF_CLK

  val pf_tx_pll = Module(new PolarFireTxPLL)
  pf_tx_pll.io.REF_CLK := pcie_refclk
  val pf_tx_pll_bitclk = pf_tx_pll.io.BIT_CLK
  val pf_tx_pll_refclk_to_lane = pf_tx_pll.io.REF_CLK_TO_LANE
  val pf_tx_pll_lock = pf_tx_pll.io.LOCK

  //-----------------------------------------------------------------------
  // DDR3 Subsystem Clocks
  //-----------------------------------------------------------------------
  val ddr3_clk_ccc = Module(new PolarFireCCC(
   PLLParameters(
    name = "ddr3_clk_ccc",
    PLLInClockParameters(50),
    Seq(
      PLLOutClockParameters(111.111)))))
 
  ddr3_clk_ccc.io.REF_CLK_0 := ref_clk0
  val ddr3_clk_in = ddr3_clk_ccc.io.OUT0_FABCLK_0.get
  val ddr3_clk_in_lock = ddr3_clk_ccc.io.PLL_LOCK_0
  mig_clock_in := ddr3_clk_in

  //-----------------------------------------------------------------------
  // Coreplex Clock Generator
  //-----------------------------------------------------------------------
  val hart_clk_ccc = Module(new PolarFireCCC(PLLParameters(
    name = "hart_clk_ccc",
    PLLInClockParameters(166.666),
    Seq(
      PLLOutClockParameters(25),
      PLLOutClockParameters(125),
      PLLOutClockParameters(150)))))

  val hart_clk_25   = hart_clk_ccc.io.OUT0_FABCLK_0.get
  val hart_clk_125  = hart_clk_ccc.io.OUT1_FABCLK_0.get
  val hart_clk_150  = hart_clk_ccc.io.OUT2_FABCLK_0.get
  val hart_clk_lock = hart_clk_ccc.io.PLL_LOCK_0
  
  // DUT clock
  hart_clk_ccc.io.REF_CLK_0 := mig_clock_out
  dut_clock := hart_clk_25

//  debug_io1 := dut_clock
//  debug_io2 := hart_clk_lock
//  debug_io3 := ddr3_clk_in
  
  //-----------------------------------------------------------------------
  // System reset
  //-----------------------------------------------------------------------
  val pf_init_monitor = Module(new PolarFireInitMonitor)

  val pf_reset = Module(new PolarFireReset)
  
  pf_reset.io.CLK           := ddr3_clk_in
  pf_reset.io.PLL_LOCK      := ddr3_clk_in_lock
  pf_reset.io.INIT_DONE     := pf_init_monitor.io.DEVICE_INIT_DONE
  pf_reset.io.EXT_RST_N     := pf_user_reset_n
  
  pf_reset.io.SS_BUSY       := UInt("b0")
  pf_reset.io.FF_US_RESTORE := UInt("b0")
  
  fpga_reset := !pf_reset.io.FABRIC_RESET_N

  sys_reset_n   := pf_reset.io.FABRIC_RESET_N

//  debug_io4 := sys_reset_n
  
  mig_resetn           := !mig_reset
  pcie_dat_resetn      := !pcie_dat_reset
  pcie_cfg_resetn      := !pcie_cfg_reset

  dut_reset_i := !pf_reset.io.FABRIC_RESET_N | !hart_clk_lock | !ddr_ctrlr_ready
  
//  withClockAndReset(hart_clk, fpga_reset) {
//    dut_reset := ResetCatchAndSync(hart_clk, dut_reset_i, 10)
//  }
  withClockAndReset(dut_clock, fpga_reset) {
    dut_reset := ResetCatchAndSync(dut_clock, dut_reset_i, 10)
  }
  
  //overrided in connectMIG and connect PCIe
  //provide defaults to allow above reset sequencing logic to work without both
  mig_clock            := dut_clock
  pcie_dat_clock       := dut_clock
  pcie_cfg_clock       := dut_clock
  mig_mmcm_locked      := UInt("b1")
  mmcm_lock_pcie       := UInt("b1")
 
  led(4) := dut_ndreset
  led(5) := !pf_user_reset_n
  led(6) := fpga_reset
  led(7) := dut_reset 
  
  //-----------------------------------------------------------------------
  // PCIe Subsystem TL Clock
  //-----------------------------------------------------------------------
  val pf_oscillator = Module(new PolarFireOscillator)
  val pf_clk_divider = Module(new PolarFireClockDivider) 
  val pf_glitchless_mux = Module(new PolarFireGlitchlessMux)
  pf_clk_divider.io.CLK_IN  := pf_oscillator.io.RCOSC_160MHZ_GL
  pf_glitchless_mux.io.CLK0 := pf_clk_divider.io.CLK_OUT
  pf_glitchless_mux.io.CLK1 := hart_clk_125
  pf_glitchless_mux.io.SEL  := pf_init_monitor.io.PCIE_INIT_DONE
  val pcie_tl_clk = pf_glitchless_mux.io.CLK_OUT
  
  //---------------------------------------------------------------------
  // Debug JTAG
  //---------------------------------------------------------------------

  // JTAG inside the FPGA fabric through user JTAG FPGA macro (UJTAG)
/*
  val fpga_jtag = Module(new CoreJtagDebugBlock)
  
  fpga_jtag.io.UTDO_IN_0   := UInt("b0")
  fpga_jtag.io.UTDO_IN_1   := UInt("b0")
  fpga_jtag.io.UTDO_IN_2   := UInt("b0")
  fpga_jtag.io.UTDO_IN_3   := UInt("b0")
  fpga_jtag.io.UTDODRV_0   := UInt("b0")
  fpga_jtag.io.UTDODRV_1   := UInt("b0")
  fpga_jtag.io.UTDODRV_2   := UInt("b0")
  fpga_jtag.io.UTDODRV_3   := UInt("b0")
  
  def connectDebugJTAG(dut: HasPeripheryDebugModuleImp): SystemJTAGIO = {
    val djtag     = dut.debug.systemjtag.get

    djtag.jtag.TCK          := fpga_jtag.io.TGT_TCK
    djtag.jtag.TMS          := fpga_jtag.io.TGT_TMS
    djtag.jtag.TDI          := fpga_jtag.io.TGT_TDI
    fpga_jtag.io.TGT_TDO    := djtag.jtag.TDO.data
    
    fpga_jtag.io.TRSTB  := jtag_TRSTB
    fpga_jtag.io.TCK    := jtag_TCK
    fpga_jtag.io.TMS    := jtag_TMS
    fpga_jtag.io.TDI	  := jtag_TDI
    jtag_TDO            := fpga_jtag.io.TDO

    djtag.mfr_id   := p(JtagDTMKey).idcodeManufId.U(11.W)

    djtag.reset    := fpga_reset
    dut_ndreset    := dut.debug.ndreset
    djtag
  }
*/
  //-----------------------------------------------------------------------
  // UART
  //-----------------------------------------------------------------------

  def connectUART(dut: HasPeripheryUARTModuleImp): Unit = {
    val uartParams = p(PeripheryUARTKey)
    if (!uartParams.isEmpty) {
      // uart connections
      dut.uart(0).rxd := SyncResetSynchronizerShiftReg(uart_rx, 2, init = Bool(true), name=Some("uart_rxd_sync"))
      uart_tx         := dut.uart(0).txd
    }
  }

  //-----------------------------------------------------------------------
  // SPI
  //-----------------------------------------------------------------------

  def connectSPI(dut: HasPeripherySPIModuleImp): Unit = {
    // SPI
    spi_flash_reset := fpga_reset
    spi_flash_wp    := UInt("b0")
    spi_flash_hold  := UInt("b0")
    spi_flash_sck   := dut.spi(0).sck
    spi_flash_ss    := dut.spi(0).cs(0)
    spi_flash_sdo   := dut.spi(0).dq(0).o
    dut.spi(0).dq(0).i := spi_flash_sdi
  }
}
