// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfireddr4

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box

class PolarFireEvalKitDDR4IODDR(depth : BigInt) extends GenericParameterizedBundle(depth) {

  val A                     = Bits(OUTPUT,14)
  val BA                    = Bits(OUTPUT,2)
  val BG                    = Bits(OUTPUT,2)
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
  val DM_N                  = Bits(OUTPUT,2)
  val ODT                   = Bits(OUTPUT,1)
  
  val DQ                    = Analog(16.W)
  val DQS                   = Analog(2.W)
  val DQS_N                 = Analog(2.W)
}

trait PolarFireEvalKitDDR4IOClocksReset extends Bundle {

  val SYS_RESET_N           = Bool(INPUT)
  val PLL_REF_CLK           = Clock(INPUT)  
  
  val SYS_CLK               = Clock(OUTPUT)  
  val RESET_N               = Bool(OUTPUT)
  val PLL_LOCK              = Bool(OUTPUT)
  
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class DDR4_Subsys(depth : BigInt)(implicit val p:Parameters) extends BlackBox
{
  override def desiredName = "pf_ddr"

  val io = new PolarFireEvalKitDDR4IODDR(depth) with PolarFireEvalKitDDR4IOClocksReset {
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

}
//scalastyle:on
