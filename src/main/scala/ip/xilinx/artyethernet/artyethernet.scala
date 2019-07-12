// See LICENSE for license details
package sifive.fpgashells.ip.xilinx.artyethernet

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.util.{ElaborationArtefacts}
import freechips.rocketchip.config._

class ArtyEthernetPrimaryIO extends Bundle {
	// primary IO
  val phy_tx_data = Output(UInt(4.W))
  val phy_tx_en = Output(Bool())
  val phy_rx_data = Input(UInt(4.W))
  val phy_dv = Input(Bool())
  val phy_rx_er = Input(Bool())
  val phy_crs = Input(Bool())
  val phy_col = Input(Bool())
  val phy_tx_clk = Input(Bool())
  val phy_rx_clk = Input(Bool())
  val phy_rst_n = Output(Bool())
}

class ArtyEthernetIO extends ArtyEthernetPrimaryIO {
	// secondary IO
  val phy_mdio_i = Input(Bool())
  val phy_mdio_o = Output(Bool())
  val phy_mdio_t = Output(Bool())
  val phy_mdc = Output(Bool())
}

trait ArtyEthernetCRI extends Bundle {
	val s_axi_aclk 		= Input(Bool())
	val s_axi_aresetn = Input(Bool())
	val ip2intc_irpt	= Output(Bool())
}

trait ArtyEthernetAXI extends Bundle {
	val s_axi_awaddr 	= Input(UInt(13.W))
	val s_axi_awvalid = Input(Bool())
	val s_axi_awready = Output(Bool())

	val s_axi_wdata 	= Input(UInt(32.W))
	val s_axi_wstrb 	= Input(UInt(4.W))
	val s_axi_wvalid 	= Input(Bool())
	val s_axi_wready 	= Output(Bool())

	val s_axi_bresp 	= Output(UInt(2.W))
	val s_axi_bvalid 	= Output(Bool())
	val s_axi_bready 	= Input(Bool())

	val s_axi_araddr 	= Input(UInt(13.W))
	val s_axi_arvalid = Input(Bool())
	val s_axi_arready = Output(Bool())

	val s_axi_rdata 	= Output(UInt(32.W))
	val s_axi_rresp 	= Output(UInt(2.W))
	val s_axi_rvalid 	= Output(Bool())
	val s_axi_rready 	= Input(Bool())
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class artyethernet()(implicit val p: Parameters) extends BlackBox
{
  val io = new ArtyEthernetIO with ArtyEthernetCRI with ArtyEthernetAXI

  val modulename = "artyethernet"

  ElaborationArtefacts.add(modulename++".vivado.tcl",
   s"""create_ip -vendor xilinx.com -library ip -name axi_ethernetlite -module_name ${modulename} -dir $$ipdir -force
   	|set_property -dict [list 									\\
		| CONFIG.AXI_ACLK_FREQ_MHZ {83.333}		  	  \\
		| CONFIG.C_S_AXI_ID_WIDTH	{4}								\\
		| CONFIG.C_S_AXI_PROTOCOL {AXI4LITE}				\\
		| CONFIG.C_INCLUDE_MDIO {1}									\\
		| CONFIG.C_INCLUDE_INTERNAL_LOOPBACK {0}		\\
		| CONFIG.C_INCLUDE_GLOBAL_BUFFERS {1}				\\
		| CONFIG.C_DUPLEX {1}												\\
		| CONFIG.C_TX_PING_PONG {1}									\\
		| CONFIG.C_RX_PING_PONG {1}									\\
		|] [get_ips ${modulename} ]
		|""".stripMargin)
}
