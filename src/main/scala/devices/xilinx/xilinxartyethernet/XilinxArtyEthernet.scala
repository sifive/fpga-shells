// See LICENSE for license details
package sifive.fpgashells.devices.xilinx.xilinxartyethernet

import chisel3._
import chisel3.util._
import chisel3.experimental._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import sifive.fpgashells.ip.xilinx.artyethernet._
import sifive.fpgashells.shell._
import sifive.fpgashells.clocks._

case class XilinxArtyEthernetParams(baseAddress: BigInt)
case object XilinxArtyEthernetKey extends Field[Seq[XilinxArtyEthernetParams]](Nil)

class XilinxArtyEthernetPads extends ArtyEthernetPrimaryIO {
  val phy_mdio = Analog(1.W)
  val phy_mdc = Output(Bool())
}

class XilinxArtyEthernet(c: XilinxArtyEthernetParams)(implicit p: Parameters) extends LazyModule {
  val device = new SimpleDevice("ethernetlite", Seq("sifive,ethernetlite0"))

  val island  = LazyModule(new XilinxArtyEthernetIsland(c, device.reg))
  val buffer  = LazyModule(new TLBuffer)
  val toaxi4  = LazyModule(new TLToAXI4(adapterName = Some("ethernet")))
  val indexer = LazyModule(new AXI4IdIndexer(idBits = 1))
  val deint   = LazyModule(new AXI4Deinterleaver(p(CacheBlockBytes)))
  val yank    = LazyModule(new AXI4UserYanker)

  island.crossAXI4In(island.node) := yank.node := deint.node := indexer.node := toaxi4.node := buffer.node
  val node: TLInwardNode = buffer.node

  val intnode = IntSourceNode(IntSourcePortSimple(num=1, resources=device.int))

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new ArtyEthernetIO {
      val s_axi_aclk = Input(Bool())
      val s_axi_aresetn = Input(Bool())
    })
    
    io <> island.module.io

    val (int,_) = intnode.out(0)
    int(0) := island.module.io.ip2intc_irpt
  }
}

class XilinxArtyEthernetIsland(c: XilinxArtyEthernetParams, res: Seq[Resource])(implicit p: Parameters) extends LazyModule with CrossesToOnlyOneClockDomain {
  val crossing = AsynchronousCrossing(8)
  val node = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address = AddressSet.misaligned(c.baseAddress, 0x2000),
      resources = (new SimpleDevice("ethernetlite", Seq("sifive,ethernetlite0"))).reg,
      regionType = RegionType.UNCACHED,
      executable = false,
      supportsWrite = TransferSizes(1, 64),
      supportsRead = TransferSizes(1, 64))),
    beatBytes = 8)
  ))

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new ArtyEthernetIO with ArtyEthernetCRI)
    val blackbox = Module(new artyethernet)
    val (axi,_) = node.in(0)

    // connect up IOs
    // since we don't have a bajillion, we'll do it by hand
    io.phy_tx_data := blackbox.io.phy_tx_data
    io.phy_tx_en := blackbox.io.phy_tx_en
    blackbox.io.phy_rx_data := io.phy_rx_data
    blackbox.io.phy_dv := io.phy_dv
    blackbox.io.phy_crs := io.phy_crs
    blackbox.io.phy_col := io.phy_col
    blackbox.io.phy_tx_clk := io.phy_tx_clk
    blackbox.io.phy_rx_clk := io.phy_rx_clk
    io.phy_rst_n := blackbox.io.phy_rst_n

    io.ip2intc_irpt := blackbox.io.ip2intc_irpt
    blackbox.io.s_axi_aclk := io.s_axi_aclk
    blackbox.io.s_axi_aresetn := io.s_axi_aresetn

    // connnect up AXI stuff
    //blackbox.io.s_axi_awid	  := axi.aw.bits.id
    blackbox.io.s_axi_awaddr 	:= axi.aw.bits.addr
    //blackbox.io.s_axi_awlen		:= axi.aw.bits.len
    //blackbox.io.s_axi_awsize  := axi.aw.bits.size
    //blackbox.io.s_axi_awburst	:= axi.aw.bits.burst
    //blackbox.io.s_axi_awcache	:= axi.aw.bits.cache
    blackbox.io.s_axi_awvalid := axi.aw.valid
    axi.aw.ready              := blackbox.io.s_axi_awready
    blackbox.io.s_axi_wdata 	:= axi.w.bits.data
    blackbox.io.s_axi_wstrb 	:= axi.w.bits.strb
    //blackbox.io.s_axi_wlast		:= axi.w.bits.last
    blackbox.io.s_axi_wvalid 	:= axi.w.valid
    axi.w.ready               := blackbox.io.s_axi_wready
    //axi.b.bits.id             := blackbox.io.s_axi_bid
    axi.b.bits.resp           := blackbox.io.s_axi_bresp
    axi.b.valid               := blackbox.io.s_axi_bvalid
    blackbox.io.s_axi_bready 	:= axi.b.ready
    //blackbox.io.s_axi_arid 		:= axi.ar.bits.id
    blackbox.io.s_axi_araddr 	:= axi.ar.bits.addr
    //blackbox.io.s_axi_arlen		:= axi.ar.bits.len
    //blackbox.io.s_axi_arsize  := axi.ar.bits.size
    //blackbox.io.s_axi_arburst	:= axi.ar.bits.burst
    //blackbox.io.s_axi_arcache	:= axi.ar.bits.cache
    blackbox.io.s_axi_arvalid := axi.ar.valid
    axi.ar.ready              := blackbox.io.s_axi_arready
    //axi.r.bits.id             := blackbox.io.s_axi_rid
    axi.r.bits.data           := blackbox.io.s_axi_rdata
    axi.r.bits.resp           := blackbox.io.s_axi_rresp
    //axi.r.bits.last           := blackbox.io.s_axi_rlast
    axi.r.valid               := blackbox.io.s_axi_rvalid
    blackbox.io.s_axi_rready 	:= axi.r.ready
  }
}
