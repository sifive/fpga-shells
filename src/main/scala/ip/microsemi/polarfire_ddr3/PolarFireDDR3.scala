// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfireddr3

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box for Microsemi PolarFire DDR3 controller version 2.1.101

class PolarFireEvalKitDDR3IODDR(depth : BigInt) extends GenericParameterizedBundle(depth) {

  val A                     = Bits(OUTPUT,16)
  val BA                    = Bits(OUTPUT,3)
  val RAS_N                 = Bool(OUTPUT)
  val CAS_N                 = Bool(OUTPUT)
  val WE_N                  = Bool(OUTPUT)
  val CTRLR_READY           = Bool(OUTPUT)
  val SHIELD0               = Bool(OUTPUT)
  val SHIELD1               = Bool(OUTPUT)
  val CK0                   = Bits(OUTPUT,1)
  val CK0_N                 = Bits(OUTPUT,1)
  val CKE                   = Bits(OUTPUT,1)
  val CS_N                  = Bits(OUTPUT,1)
  val DM                    = Bits(OUTPUT,2)
  val ODT                   = Bits(OUTPUT,1)
  val RESET_N               = Bool(OUTPUT)
  
  val DQ                    = Analog(16.W)
  val DQS                   = Analog(2.W)
  val DQS_N                 = Analog(2.W)
}

trait PolarFireEvalKitDDR3IOClocksReset extends Bundle {

  val SYS_RESET_N           = Bool(INPUT)
  val PLL_REF_CLK           = Clock(INPUT)  
  
  val SYS_CLK               = Clock(OUTPUT)  
  val PLL_LOCK              = Bool(OUTPUT)
  
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class DDR3_Subsys(depth : BigInt)(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "pf_ddr"

  val io = new PolarFireEvalKitDDR3IODDR(depth) with PolarFireEvalKitDDR3IOClocksReset {
    //axi slave interface
    //slave interface write address ports
    val axi0_awid             = Bits(INPUT,4)
    val axi0_awaddr           = Bits(INPUT,32)
    val axi0_awlen            = Bits(INPUT,8)
    val axi0_awsize           = Bits(INPUT,3)
    val axi0_awburst          = Bits(INPUT,2)
    val axi0_awlock           = Bits(INPUT,2)
    val axi0_awcache          = Bits(INPUT,4)
    val axi0_awprot           = Bits(INPUT,3)
    val axi0_awvalid          = Bool(INPUT)
    val axi0_awready          = Bool(OUTPUT)
    //slave interface write data ports
    val axi0_wdata            = Bits(INPUT,64)
    val axi0_wstrb            = Bits(INPUT,8)
    val axi0_wlast            = Bool(INPUT)
    val axi0_wvalid           = Bool(INPUT)
    val axi0_wready           = Bool(OUTPUT)
    //slave interface write response ports
    val axi0_bready           = Bool(INPUT)
    val axi0_bid              = Bits(OUTPUT,4)
    val axi0_bresp            = Bits(OUTPUT,2)
    val axi0_bvalid           = Bool(OUTPUT)
    //slave interface read address ports
    val axi0_arid             = Bits(INPUT,4)
    val axi0_araddr           = Bits(INPUT,32)
    val axi0_arlen            = Bits(INPUT,8)
    val axi0_arsize           = Bits(INPUT,3)
    val axi0_arburst          = Bits(INPUT,2)
    val axi0_arlock           = Bits(INPUT,2)
    val axi0_arcache          = Bits(INPUT,4)
    val axi0_arprot           = Bits(INPUT,3)
    val axi0_arvalid          = Bool(INPUT)
    val axi0_arready          = Bool(OUTPUT)
    //slave interface read data ports
    val axi0_rready           = Bool(INPUT)
    val axi0_rid              = Bits(OUTPUT,4)
    val axi0_rdata            = Bits(OUTPUT,64)
    val axi0_rresp            = Bits(OUTPUT,2)
    val axi0_rlast            = Bool(OUTPUT)
    val axi0_rvalid           = Bool(OUTPUT)
    //misc
    val AXI0_AWUSERTAG        = Bits(INPUT,4)
    val AXI0_BUSERTAG         = Bits(OUTPUT,4)
  }

  ElaborationArtefacts.add(
    "AddIPInstance.polarfire_ddr3.libero.tcl",
    """ 
create_design -id Actel:SystemBuilder:PF_DDR3:2.2.109 -design_name {pf_ddr} -config_file {} -params {} -inhibit_configurator 0
open_smartdesign -design pf_ddr
sysbld_configure_page -component pf_ddr -page PF_DDR3_UI -param WIDTH:16 \
                                                         -param CLOCK_DDR:666.666 \
                                                         -param CLOCK_PLL_REFERENCE:111.111 \
                                                         -param CCC_PLL_CLOCK_MULTIPLIER:6 \
                                                         -param ROW_ADDR_WIDTH:16 \
                                                         -param CAS_LATENCY:9 \
                                                         -param RTT_NOM:RZQ6 \
                                                         -param CAS_WRITE_LATENCY:7 \
                                                         -param OUTPUT_DRIVE_STRENGTH:RZQ7 \
                                                         -param TIMING_RAS:36 \
                                                         -param TIMING_RCD:13.5 \
                                                         -param TIMING_RP:13.5 \
                                                         -param TIMING_RC:49.5 \
                                                         -param TIMING_WR:15 \
                                                         -param TIMING_FAW:30 \
                                                         -param TIMING_WTR:5 \
                                                         -param TIMING_RRD:7.5 \
                                                         -param TIMING_RTP:7.5 \
                                                         -param TIMING_RFC:350 \
                                                         -param AXI_ID_WIDTH:6
save_design -component pf_ddr  -library {} -file {}
generate_design -component pf_ddr  -library {} -file {} -generator {} -recursive 1
close_design -component pf_ddr
"""
  )
}
//scalastyle:on
