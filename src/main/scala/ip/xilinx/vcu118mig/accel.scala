// See LICENSE for license details.
package sifive.fpgashells.ip.xilinx.vcu118mig

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._

// Black Box

class VCU118AccelIO extends Bundle {
    //axi slave
    //slave interface write address

    val s_axi_awid            = Bits(INPUT,4)
    val s_axi_awaddr          = Bits(INPUT,32)
    val s_axi_awregion        = Bits(INPUT,4)
    val s_axi_awlen           = Bits(INPUT,8)
    val s_axi_awsize          = Bits(INPUT,3)
    val s_axi_awburst         = Bits(INPUT,2)
    val s_axi_awlock          = Bool(INPUT)
    val s_axi_awcache         = Bits(INPUT,4)
    val s_axi_awprot          = Bits(INPUT,3)
    val s_axi_awqos           = Bits(INPUT,4)
    val s_axi_awvalid         = Bool(INPUT)
    val s_axi_awready         = Bool(OUTPUT)
    //slave interface write data
    val s_axi_wdata           = Bits(INPUT,64)
    val s_axi_wstrb           = Bits(INPUT,8)
    val s_axi_wlast           = Bool(INPUT)
    val s_axi_wvalid          = Bool(INPUT)
    val s_axi_wready          = Bool(OUTPUT)
    //slave interface write response
    val s_axi_bready          = Bool(INPUT)
    val s_axi_bid             = Bits(OUTPUT,4)
    val s_axi_bresp           = Bits(OUTPUT,2)
    val s_axi_bvalid          = Bool(OUTPUT)
    //slave interface read address
    val s_axi_arid            = Bits(INPUT,4)
    val s_axi_araddr          = Bits(INPUT,32)
    val s_axi_arregion        = Bits(INPUT,4)
    val s_axi_arlen           = Bits(INPUT,8)
    val s_axi_arsize          = Bits(INPUT,3)
    val s_axi_arburst         = Bits(INPUT,2)
    val s_axi_arlock          = Bits(INPUT,1)
    val s_axi_arcache         = Bits(INPUT,4)
    val s_axi_arprot          = Bits(INPUT,3)
    val s_axi_arqos           = Bits(INPUT,4)
    val s_axi_arvalid         = Bool(INPUT)
    val s_axi_arready         = Bool(OUTPUT)
    //slave interface read data
    val s_axi_rready          = Bool(INPUT)
    val s_axi_rid             = Bits(OUTPUT,4)
    val s_axi_rdata           = Bits(OUTPUT,64)
    val s_axi_rresp           = Bits(OUTPUT,2)
    val s_axi_rlast           = Bool(OUTPUT)
    val s_axi_rvalid          = Bool(OUTPUT)

    //axi master
    //master interface write address ports
    val m_axi_awid            = Bits(OUTPUT,4)
    val m_axi_awaddr          = Bits(OUTPUT,32)
    val m_axi_awregion        = Bits(OUTPUT,4)
    val m_axi_awlen           = Bits(OUTPUT,8)
    val m_axi_awsize          = Bits(OUTPUT,3)
    val m_axi_awburst         = Bits(OUTPUT,2)
    val m_axi_awlock          = Bool(OUTPUT)
    val m_axi_awcache         = Bits(OUTPUT,4)
    val m_axi_awprot          = Bits(OUTPUT,3)
    val m_axi_awqos           = Bits(OUTPUT,4)
    val m_axi_awvalid         = Bool(OUTPUT)
    val m_axi_awready         = Bool(INPUT)
    //master interface write data ports
    val m_axi_wdata           = Bits(OUTPUT,64)
    val m_axi_wstrb           = Bits(OUTPUT,8)
    val m_axi_wlast           = Bool(OUTPUT)
    val m_axi_wvalid          = Bool(OUTPUT)
    val m_axi_wready          = Bool(INPUT)
    //master interface write response ports
    val m_axi_bready          = Bool(OUTPUT)
    val m_axi_bid             = Bits(INPUT,4)
    val m_axi_bresp           = Bits(INPUT,2)
    val m_axi_bvalid          = Bool(INPUT)
    //master interface read address ports
    val m_axi_arid            = Bits(OUTPUT,4)
    val m_axi_araddr          = Bits(OUTPUT,32)
    val m_axi_arregion        = Bits(OUTPUT,4)
    val m_axi_arlen           = Bits(OUTPUT,8)
    val m_axi_arsize          = Bits(OUTPUT,3)
    val m_axi_arburst         = Bits(OUTPUT,2)
    val m_axi_arlock          = Bits(OUTPUT,1)
    val m_axi_arcache         = Bits(OUTPUT,4)
    val m_axi_arprot          = Bits(OUTPUT,3)
    val m_axi_arqos           = Bits(OUTPUT,4)
    val m_axi_arvalid         = Bool(OUTPUT)
    val m_axi_arready         = Bool(INPUT)
    //master interface read data ports
    val m_axi_rready          = Bool(OUTPUT)
    val m_axi_rid             = Bits(INPUT,4)
    val m_axi_rdata           = Bits(INPUT,64)
    val m_axi_rresp           = Bits(INPUT,2)
    val m_axi_rlast           = Bool(INPUT)
    val m_axi_rvalid          = Bool(INPUT)
}

trait AxiAccelClocksReset extends Bundle {
    val axi_clk_in = Bool(INPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class axiaccel(implicit val p:Parameters) extends BlackBox
{
  val io = new VCU118AccelIO with AxiAccelClocksReset {
    //master interface write address ports
  }
}
//scalastyle:on
