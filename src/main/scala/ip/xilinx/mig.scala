package sifive.fpgashells.ip.xilinx.mig

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.util.GenericParameterizedBundle
import freechips.rocketchip.config._
import sifive.fpgashells.ip.xilinx.mig._

// Black Box

class MIGIODDR(val depth : BigInt, val fpga: String) extends Bundle() {

  check_depth(fpga,depth)
  val ddr3_addr             = Bits(OUTPUT,if(depth<=0x40000000) 14 else 16)
  val ddr3_ba               = Bits(OUTPUT,3)
  val ddr3_ras_n            = Bool(OUTPUT)
  val ddr3_cas_n            = Bool(OUTPUT)
  val ddr3_we_n             = Bool(OUTPUT)
  val ddr3_reset_n          = Bool(OUTPUT)
  val ddr3_ck_p             = Bits(OUTPUT,1)
  val ddr3_ck_n             = Bits(OUTPUT,1)
  val ddr3_cke              = Bits(OUTPUT,1)
  val ddr3_cs_n             = Bits(OUTPUT,1)
  val ddr3_dm               = Bits(OUTPUT,get_ddr3_dm(fpga))
  val ddr3_odt              = Bits(OUTPUT,1)

  val ddr3_dq               = Analog(get_ddr3_dq(fpga).W)
  val ddr3_dqs_n            = Analog(get_ddr3_dqs(fpga).W)
  val ddr3_dqs_p            = Analog(get_ddr3_dqs(fpga).W)


  def check_depth(fpga: String, depth: BigInt): Unit = fpga match {
    case "arty" => require((depth<=0x10000000L),"Arty100TMIGIODDR supports upto 256 MB depth configuraton")
    case "vc707" => require((depth<=0x100000000L),"VC707MIGIODDR supports upto 4GB depth configuraton")
  }
  def get_ddr3_dm(fpga: String): Int = fpga match {
    case "arty" => 2
    case "vc707" => 8
    case _ => 8
  }

  def get_ddr3_dq(fpga: String): Int = fpga match {
    case "arty" => 16
    case "vc707" => 64
    case _ => 64
  }

  def get_ddr3_dqs(fpga: String): Int = fpga match {
    case "arty" => 2
    case "vc707" => 8
    case _ => 8
  }
}

trait MIGIOClocksReset extends Bundle {
  //inputs
  //"NO_BUFFER" clock source (must be connected to IBUF outside of IP)
  def fpga: String

  val sys_clk_i             = Bool(INPUT)
  val clk_ref_i = if (fpga == "arty") Some(Bool(INPUT)) else None
  //user interface signals
  val ui_clk                = Clock(OUTPUT)
  val ui_clk_sync_rst       = Bool(OUTPUT)
  val mmcm_locked           = Bool(OUTPUT)
  val aresetn               = Bool(INPUT)
  //misc
  val init_calib_complete   = Bool(OUTPUT)
  val sys_rst               = Bool(INPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class mig(depth : BigInt, fpga: String)(implicit val p:Parameters) extends BlackBox
{
 // check_depth(fpga, depth)
  val io = new MIGIODDR(depth, fpga) with MIGIOClocksReset {
    // User interface signals
    val app_sr_req            = Bool(INPUT)
    val app_ref_req           = Bool(INPUT)
    val app_zq_req            = Bool(INPUT)
    val app_sr_active         = Bool(OUTPUT)
    val app_ref_ack           = Bool(OUTPUT)
    val app_zq_ack            = Bool(OUTPUT)
    //axi_s
    //slave interface write address ports
    val s_axi_awid            = Bits(INPUT,4)
    val s_axi_awaddr          = Bits(INPUT,if(depth<=0x40000000) 30 else 32)
    val s_axi_awlen           = Bits(INPUT,8)
    val s_axi_awsize          = Bits(INPUT,3)
    val s_axi_awburst         = Bits(INPUT,2)
    val s_axi_awlock          = Bits(INPUT,1)
    val s_axi_awcache         = Bits(INPUT,4)
    val s_axi_awprot          = Bits(INPUT,3)
    val s_axi_awqos           = Bits(INPUT,4)
    val s_axi_awvalid         = Bool(INPUT)
    val s_axi_awready         = Bool(OUTPUT)
    //slave interface write data ports
    val s_axi_wdata           = Bits(INPUT,64)
    val s_axi_wstrb           = Bits(INPUT,8)
    val s_axi_wlast           = Bool(INPUT)
    val s_axi_wvalid          = Bool(INPUT)
    val s_axi_wready          = Bool(OUTPUT)
    //slave interface write response ports
    val s_axi_bready          = Bool(INPUT)
    val s_axi_bid             = Bits(OUTPUT,4)
    val s_axi_bresp           = Bits(OUTPUT,2)
    val s_axi_bvalid          = Bool(OUTPUT)
    //slave interface read address ports
    val s_axi_arid            = Bits(INPUT,4)
    val s_axi_araddr          = Bits(INPUT,if(depth<=0x40000000) 30 else 32)
    val s_axi_arlen           = Bits(INPUT,8)
    val s_axi_arsize          = Bits(INPUT,3)
    val s_axi_arburst         = Bits(INPUT,2)
    val s_axi_arlock          = Bits(INPUT,1)
    val s_axi_arcache         = Bits(INPUT,4)
    val s_axi_arprot          = Bits(INPUT,3)
    val s_axi_arqos           = Bits(INPUT,4)
    val s_axi_arvalid         = Bool(INPUT)
    val s_axi_arready         = Bool(OUTPUT)
    //slave interface read data ports
    val s_axi_rready          = Bool(INPUT)
    val s_axi_rid             = Bits(OUTPUT,4)
    val s_axi_rdata           = Bits(OUTPUT,64)
    val s_axi_rresp           = Bits(OUTPUT,2)
    val s_axi_rlast           = Bool(OUTPUT)
    val s_axi_rvalid          = Bool(OUTPUT)
    //misc
    val device_temp           = Bits(OUTPUT,12)
  }
  
  val mps = migprjs  

  val migprj = if(fpga == "arty") mps.arty100tmigprj else if(fpga == "vc707" && depth <= 0x40000000) mps.vc707mig1gbprj else mps.vc707mig4gbprj
  val migprjname = """{/mig.prj}"""
  val modulename = """mig"""

  ElaborationArtefacts.add(
    modulename++".vivado.tcl",
    """set migprj """++migprj++"""
   set migprjfile """++migprjname++"""
   set migprjfilepath $ipdir$migprjfile
   set fp [open $migprjfilepath w+]
   puts $fp $migprj
   close $fp
   create_ip -vendor xilinx.com -library ip -name mig_7series -module_name """ ++ modulename ++ """ -dir $ipdir -force
   set_property CONFIG.XML_INPUT_FILE $migprjfilepath [get_ips """ ++ modulename ++ """] """
  )


}
