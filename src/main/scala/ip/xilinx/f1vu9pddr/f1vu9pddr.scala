// See LICENSE for license details.
package sifive.fpgashells.ip.xilinx.f1vu9pddr

import scala.collection.mutable.StringBuilder
import chisel3._
import chisel3.core.ActualDirection
import chisel3.util._
import chisel3.experimental._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.config.Parameters

// BlackBox definition for using Amazon's sh_ddr IP

// Creates a SystemVerilog wrapper for sh_ddr with verilog-compliant
// port definition for integration with Chisel-generated verilog

class ddrwrapper(instantiate: Seq[Boolean]) extends BlackBox(Map(
  "DDR_A_PRESENT" -> (if (instantiate(0)) 1 else 0),
  "DDR_B_PRESENT" -> (if (instantiate(1)) 1 else 0),
  "DDR_D_PRESENT" -> (if (instantiate(2)) 1 else 0))) with HasBlackBoxInline {
  val io = IO(new F1VU9PDDRBase with F1VU9PDDRIO with F1VU9PAXISignals)
  
  // generate sv wrapper from iodef
  val ports = new StringBuilder
  val wires = new StringBuilder
  val assigns = new StringBuilder
  val modinst = new StringBuilder
  for ((name, data) <- io.elements) {
    modinst ++= s"|  .$name($name),\n"
    // get direction
    val direction: String = DataMirror.directionOf(data) match {
      case ActualDirection.Input => "input"
      case ActualDirection.Output => "output logic"
      case ActualDirection.Unspecified => "inout"
      case ActualDirection.Bidirectional(_) => "inout"
    }

    // get width of underlying signals (if in a Vec)
    val rawwidth = data match {
      case vec: Vec[Data] => vec(0).widthOption.getOrElse(1)
      case ele => ele.getWidth
    }
    val widthstring = if (rawwidth == 1) "" else s"[${rawwidth - 1}:0]"
    // create n copies of verilog-compliant io for n-wide vectors
    // i.e.
    //    module mymodule (
    //    ...    
    //      input [7:0] bytearray_0,
    //      input [7:0] bytearray_1,
    //    ...
    //    );
    //    ...
    //      logic [7:0] bytearray [n:0];
    //      assign bytearray[0] = bytearray_0;
    //      assign bytearrya[1] = bytearray_1;
    //    ...
    //
    data match {
      case v: Vec[Data] => {
        // amazon port spec is inconsistent so we can't just make all the "adapter" arrays the same
        // partially a result of defining F1VU9PAXISignals as all Vecs rather than UInts for Vecs of Bools
        // however this would make instantiation in XilinxF1VU9PDDRIsland.scala unnecessarily complicated if
        // fewer than 3 AXI ports are used (which is how this device wrapper was originally designed)
        val newwire = if (rawwidth == 1 && name != "cl_sh_ddr_awvalid") { s"|  logic [${v.length-1}:0] $name;\n" }
                      else                                              { s"|  logic $widthstring $name [${v.length-1}:0];\n" }
        wires ++= newwire
        for (i <- 0 until v.length) {
          ports ++= s"|  $direction $widthstring $name" + s"_$i,\n"
          val newassign = if (direction == "input") { s"|  assign $name[$i] = $name" + s"_$i;\n" }
                          else /*output*/           { s"|  assign $name" + s"_$i = $name[$i];\n" }
          assigns ++= newassign
        }
      }
      case e => ports ++= s"|  $direction $widthstring $name,\n"
    }
  }

  val sv = new StringBuilder
  sv ++= "|module ddrwrapper #(parameter DDR_A_PRESENT = 1, parameter DDR_B_PRESENT = 1, parameter DDR_D_PRESENT = 1) (\n"
  sv ++= ports.delete(ports.length-2,ports.length).toString
  sv ++= "\n|);\n"
  sv ++= wires.toString
  sv ++= assigns.toString 
  sv ++= "|sh_ddr #(.DDR_A_PRESENT(DDR_A_PRESENT), .DDR_B_PRESENT(DDR_B_PRESENT), .DDR_D_PRESENT(DDR_D_PRESENT)) wrapped_module (\n"
  sv ++= modinst.delete(modinst.length-2,modinst.length).toString
  sv ++= "\n|);\n|endmodule"
  setInline("ddrwrapper.sv", sv.toString.stripMargin)
}

// toplevel interface
class F1VU9PDDRPads extends F1VU9PDDRBase with F1VU9PDDRIO

//----------------
// DDR IOs
//----------------
// F1VU9PDDRIO          - Analogs
// F1VU9PDDRSignals     - dirnIO
// F1VU9PAXISignals     - dirnIO
class F1VU9PDDRBase extends Bundle {
  // Block A
  val CLK_300M_DIMM0_DP  = Input(Bool())
  val CLK_300M_DIMM0_DN  = Input(Bool())
  val M_A_ACT_N          = Output(Bool())
  val M_A_MA             = Output(UInt(17.W))
  val M_A_BA             = Output(UInt(2.W))
  val M_A_BG             = Output(UInt(2.W))
  val M_A_CKE            = Output(Bool())
  val M_A_ODT            = Output(Bool())
  val M_A_CS_N           = Output(Bool())
  val M_A_CLK_DN         = Output(Bool())
  val M_A_CLK_DP         = Output(Bool())
  val M_A_PAR            = Output(Bool())
  val cl_RST_DIMM_A_N    = Output(Bool())
  // Block B
  val CLK_300M_DIMM1_DP  = Input(Bool())
  val CLK_300M_DIMM1_DN  = Input(Bool())
  val M_B_ACT_N          = Output(Bool())
  val M_B_MA             = Output(UInt(17.W))
  val M_B_BA             = Output(UInt(2.W))
  val M_B_BG             = Output(UInt(2.W))
  val M_B_CKE            = Output(Bool())
  val M_B_ODT            = Output(Bool())
  val M_B_CS_N           = Output(Bool())
  val M_B_CLK_DN         = Output(Bool())
  val M_B_CLK_DP         = Output(Bool())
  val M_B_PAR            = Output(Bool())
  val cl_RST_DIMM_B_N    = Output(Bool())
  // Block D
  val CLK_300M_DIMM3_DP  = Input(Bool())
  val CLK_300M_DIMM3_DN  = Input(Bool())
  val M_D_ACT_N          = Output(Bool())
  val M_D_MA             = Output(UInt(17.W))
  val M_D_BA             = Output(UInt(2.W))
  val M_D_BG             = Output(UInt(2.W))
  val M_D_CKE            = Output(Bool())
  val M_D_ODT            = Output(Bool())
  val M_D_CS_N           = Output(Bool())
  val M_D_CLK_DN         = Output(Bool())
  val M_D_CLK_DP         = Output(Bool())
  val M_D_PAR            = Output(Bool())
  val cl_RST_DIMM_D_N    = Output(Bool())
  // Management Interface
  val clk                = Input(Bool())
  val rst_n              = Input(Bool())
  val stat_clk           = Input(Bool())
  val stat_rst_n         = Input(Bool())
  val sh_ddr_stat_addr0  = Input(UInt(8.W))
  val sh_ddr_stat_wr0    = Input(Bool())
  val sh_ddr_stat_rd0    = Input(Bool())
  val sh_ddr_stat_wdata0 = Input(UInt(32.W))
  val ddr_sh_stat_ack0   = Output(Bool())
  val ddr_sh_stat_rdata0 = Output(UInt(32.W))
  val ddr_sh_stat_int0   = Output(UInt(8.W))
  val sh_ddr_stat_addr1  = Input(UInt(8.W))
  val sh_ddr_stat_wr1    = Input(Bool())
  val sh_ddr_stat_rd1    = Input(Bool())
  val sh_ddr_stat_wdata1 = Input(UInt(32.W))
  val ddr_sh_stat_ack1   = Output(Bool())
  val ddr_sh_stat_rdata1 = Output(UInt(32.W))
  val ddr_sh_stat_int1   = Output(UInt(8.W))
  val sh_ddr_stat_addr2  = Input(UInt(8.W))
  val sh_ddr_stat_wr2    = Input(Bool())
  val sh_ddr_stat_rd2    = Input(Bool())
  val sh_ddr_stat_wdata2 = Input(UInt(32.W))
  val ddr_sh_stat_ack2   = Output(Bool())
  val ddr_sh_stat_rdata2 = Output(UInt(32.W))
  val ddr_sh_stat_int2   = Output(UInt(8.W))
}

trait F1VU9PDDRIO extends Bundle {
  val M_A_DQ             = Analog(64.W)
  val M_A_ECC            = Analog(8.W)
  val M_A_DQS_DP         = Analog(18.W)
  val M_A_DQS_DN         = Analog(18.W)
  val M_B_DQ             = Analog(64.W)
  val M_B_ECC            = Analog(8.W)
  val M_B_DQS_DP         = Analog(18.W)
  val M_B_DQS_DN         = Analog(18.W)
  val M_D_DQ             = Analog(64.W)
  val M_D_ECC            = Analog(8.W)
  val M_D_DQS_DP         = Analog(18.W)
  val M_D_DQS_DN         = Analog(18.W)
}

trait F1VU9PAXISignals extends Bundle {
  val cl_sh_ddr_awid     = Input(Vec(3, UInt(16.W)))
  val cl_sh_ddr_awaddr   = Input(Vec(3, UInt(64.W)))
  val cl_sh_ddr_awlen    = Input(Vec(3, UInt(8.W)))
  val cl_sh_ddr_awsize   = Input(Vec(3, UInt(3.W)))
  val cl_sh_ddr_awburst  = Input(Vec(3, UInt(2.W)))
  val cl_sh_ddr_awvalid  = Input(Vec(3, Bool()))
  val sh_cl_ddr_awready  = Output(Vec(3, Bool()))
  val cl_sh_ddr_wid      = Input(Vec(3, UInt(16.W)))
  val cl_sh_ddr_wdata    = Input(Vec(3, UInt(512.W)))
  val cl_sh_ddr_wstrb    = Input(Vec(3, UInt(64.W)))
  val cl_sh_ddr_wlast    = Input(Vec(3, Bool()))
  val cl_sh_ddr_wvalid   = Input(Vec(3, Bool()))
  val sh_cl_ddr_wready   = Output(Vec(3, Bool()))
  val sh_cl_ddr_bid      = Output(Vec(3, UInt(16.W)))
  val sh_cl_ddr_bresp    = Output(Vec(3, UInt(2.W)))
  val sh_cl_ddr_bvalid   = Output(Vec(3, Bool()))
  val cl_sh_ddr_bready   = Input(Vec(3, Bool()))
  val cl_sh_ddr_arid     = Input(Vec(3, UInt(16.W)))
  val cl_sh_ddr_araddr   = Input(Vec(3, UInt(64.W)))
  val cl_sh_ddr_arlen    = Input(Vec(3, UInt(8.W)))
  val cl_sh_ddr_arsize   = Input(Vec(3, UInt(3.W)))
  val cl_sh_ddr_arburst  = Input(Vec(3, UInt(2.W)))
  val cl_sh_ddr_arvalid  = Input(Vec(3, Bool()))
  val sh_cl_ddr_arready  = Output(Vec(3, Bool()))
  val sh_cl_ddr_rid      = Output(Vec(3, UInt(16.W)))
  val sh_cl_ddr_rdata    = Output(Vec(3, UInt(512.W)))
  val sh_cl_ddr_rresp    = Output(Vec(3, UInt(2.W)))
  val sh_cl_ddr_rlast    = Output(Vec(3, Bool()))
  val sh_cl_ddr_rvalid   = Output(Vec(3, Bool()))
  val cl_sh_ddr_rready   = Input(Vec(3, Bool()))
  val sh_cl_ddr_is_ready = Output(Vec(3, Bool()))
}
