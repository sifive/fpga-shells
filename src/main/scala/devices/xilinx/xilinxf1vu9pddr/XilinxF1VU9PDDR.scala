// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxf1vu9pddr

import chisel3._
import chisel3.util._
import chisel3.experimental._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.subsystem._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import sifive.fpgashells.ip.xilinx.f1vu9pddr._
import sifive.blocks.devices.pinctrl._
import sifive.fpgashells.ip.xilinx._


case class XilinxF1VU9PDDRParams(addresses : Seq[Seq[AddressSet]], instantiate : Seq[Boolean]) {
  require(addresses.length == 3, "must specify 3 addresses")
  require(instantiate.length == 3, "must specify whether or not to instantiate all 3 DDRs")
}

class XilinxF1VU9PDDR(c: XilinxF1VU9PDDRParams)(implicit p: Parameters) extends LazyModule {
  
  val buffer = LazyModule(new TLBuffer)
  val toaxi4  = LazyModule(new TLToAXI4(adapterName = Some("mem"), stripBits = 1))
  val indexer = LazyModule(new AXI4IdIndexer(idBits = 16))
  val deint   = LazyModule(new AXI4Deinterleaver(p(CacheBlockBytes)))
  val yank    = LazyModule(new AXI4UserYanker)
  val island  = LazyModule(new XilinxF1VU9PDDRIsland(c))
  
  val node: TLInwardNode = island.crossAXI4In(island.node) := yank.node := deint.node := indexer.node := toaxi4.node := buffer.node
  

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val port = new F1VU9PDDRSignals
    })

    io.port <> island.module.io.port
    
    // connect inferred clock/reset
    //island.module.clock := io.port.clk
    //island.module.reset := !io.port.rst_n
  }
}

// Connects Vec(n, Analog(1.W)) to Vec(n, BasePin)
// used to connect BlackBox and then to break out Analog IO at the shell level
// BasePins are required since BundleBridges don't support mixed Analog/Directioned IO
class nbitAnalog2BasePin
object nbitAnalog2BasePin {
  def apply(analog: Vec[Analog], basepin: Seq[BasePin]): Unit = {
    val n = basepin.length
    // shouldn't really need this check but ya never know
    require (n == analog.length, s"analog and basepin must both be ${n} bits long")
    (analog zip basepin).foreach { case (a,b) =>
      IOBUF(a,b)
      KEEPER(a)
    }
  }
}

class XilinxF1VU9PDDRIsland(c: XilinxF1VU9PDDRParams)(implicit p: Parameters) extends LazyModule with CrossesToOnlyOneClockDomain {
  val crossing = AsynchronousCrossing(8)
  /*
  val AXIslavenodes = c.addresses.map(
    address => AXI4SlaveNode(Seq(AXI4SlavePortParameters(
      slaves = Seq(AXI4SlaveParameters(
      address       = address,
      resources     = (new MemoryDevice).reg,
      regionType    = RegionType.UNCACHED,
      executable    = true,
      supportsWrite = TransferSizes(1, 256*8),
      supportsRead  = TransferSizes(1, 256*8))),
      beatBytes = 8)))
    )*/
  val node = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
      slaves = Seq(AXI4SlaveParameters(
      address       = c.addresses(0),
      resources     = (new MemoryDevice).reg,
      regionType    = RegionType.UNCACHED,
      executable    = true,
      supportsWrite = TransferSizes(1, 256*8),
      supportsRead  = TransferSizes(1, 256*8))),
      beatBytes = 8)))
  
  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val port = new F1VU9PDDRSignals
    })

    val blackbox = Module(new F1VU9PDDRBlackBox(c.instantiate)) // has F1VU9PDDRIO with F1VU9PAXISignals
    val (axi_async,_) = node.in(0)
    //val (b_axi_async,_) = AXIslavenodes(1).in(0)
    //val (d_axi_async,_) = AXIslavenodes(2).in(0)
    //val axi = Seq(a_axi_async, b_axi_async, d_axi_async)
    
    //-----------------------------
    // DDR connections
    //-----------------------------
    // Analog (inout) connections
    nbitAnalog2BasePin(blackbox.io.M_A_DQ,      io.port.M_A_DQ_bp)
    nbitAnalog2BasePin(blackbox.io.M_A_ECC,     io.port.M_A_ECC_bp)
    nbitAnalog2BasePin(blackbox.io.M_A_DQS_DP,  io.port.M_A_DQS_DP_bp)
    nbitAnalog2BasePin(blackbox.io.M_A_DQS_DN,  io.port.M_A_DQS_DN_bp)
    nbitAnalog2BasePin(blackbox.io.M_B_DQ,      io.port.M_B_DQ_bp)
    nbitAnalog2BasePin(blackbox.io.M_B_ECC,     io.port.M_B_ECC_bp)
    nbitAnalog2BasePin(blackbox.io.M_B_DQS_DP,  io.port.M_B_DQS_DP_bp)
    nbitAnalog2BasePin(blackbox.io.M_B_DQS_DN,  io.port.M_B_DQS_DN_bp)
    nbitAnalog2BasePin(blackbox.io.M_D_DQ,      io.port.M_D_DQ_bp)
    nbitAnalog2BasePin(blackbox.io.M_D_ECC,     io.port.M_D_ECC_bp)
    nbitAnalog2BasePin(blackbox.io.M_D_DQS_DP,  io.port.M_D_DQS_DP_bp)
    nbitAnalog2BasePin(blackbox.io.M_D_DQS_DN,  io.port.M_D_DQS_DN_bp)
    // OUTPUTS
    // block A
    io.port.M_A_ACT_N       := blackbox.io.M_A_ACT_N
    io.port.M_A_MA          := blackbox.io.M_A_MA
    io.port.M_A_BA          := blackbox.io.M_A_BA
    io.port.M_A_BG          := blackbox.io.M_A_BG
    io.port.M_A_CKE         := blackbox.io.M_A_CKE
    io.port.M_A_ODT         := blackbox.io.M_A_ODT
    io.port.M_A_CS_N        := blackbox.io.M_A_CS_N
    io.port.M_A_CLK_DN      := blackbox.io.M_A_CLK_DN
    io.port.M_A_CLK_DP      := blackbox.io.M_A_CLK_DP
    io.port.M_A_PAR         := blackbox.io.M_A_PAR
    io.port.cl_RST_DIMM_A_N := blackbox.io.cl_RST_DIMM_A_N
    // block B
    io.port.M_B_ACT_N       := blackbox.io.M_B_ACT_N
    io.port.M_B_MA          := blackbox.io.M_B_MA
    io.port.M_B_BA          := blackbox.io.M_B_BA
    io.port.M_B_BG          := blackbox.io.M_B_BG
    io.port.M_B_CKE         := blackbox.io.M_B_CKE
    io.port.M_B_ODT         := blackbox.io.M_B_ODT
    io.port.M_B_CS_N        := blackbox.io.M_B_CS_N
    io.port.M_B_CLK_DN      := blackbox.io.M_B_CLK_DN
    io.port.M_B_CLK_DP      := blackbox.io.M_B_CLK_DP
    io.port.M_B_PAR         := blackbox.io.M_B_PAR
    io.port.cl_RST_DIMM_B_N := blackbox.io.cl_RST_DIMM_B_N
    // block D                 .
    io.port.M_D_ACT_N       := blackbox.io.M_D_ACT_N
    io.port.M_D_MA          := blackbox.io.M_D_MA
    io.port.M_D_BA          := blackbox.io.M_D_BA
    io.port.M_D_BG          := blackbox.io.M_D_BG
    io.port.M_D_CKE         := blackbox.io.M_D_CKE
    io.port.M_D_ODT         := blackbox.io.M_D_ODT
    io.port.M_D_CS_N        := blackbox.io.M_D_CS_N
    io.port.M_D_CLK_DN      := blackbox.io.M_D_CLK_DN
    io.port.M_D_CLK_DP      := blackbox.io.M_D_CLK_DP
    io.port.M_D_PAR         := blackbox.io.M_D_PAR
    io.port.cl_RST_DIMM_D_N := blackbox.io.cl_RST_DIMM_D_N
    // INPUTS
    // block A
    blackbox.io.CLK_300M_DIMM0_DP := io.port.CLK_300M_DIMM0_DP
    blackbox.io.CLK_300M_DIMM0_DN := io.port.CLK_300M_DIMM0_DN
    // block B
    blackbox.io.CLK_300M_DIMM1_DP := io.port.CLK_300M_DIMM1_DP
    blackbox.io.CLK_300M_DIMM1_DN := io.port.CLK_300M_DIMM1_DN
    // block D
    blackbox.io.CLK_300M_DIMM3_DP := io.port.CLK_300M_DIMM3_DP
    blackbox.io.CLK_300M_DIMM3_DN := io.port.CLK_300M_DIMM3_DN
    
    //----------------------------
    // Management
    //----------------------------
    // Clocks/Resets
    blackbox.io.clk                 := io.port.clk              
    blackbox.io.rst_n               := io.port.rst_n            
    blackbox.io.stat_clk            := io.port.stat_clk         
    blackbox.io.stat_rst_n          := io.port.stat_rst_n
    
    // DDR status
    // block A
    blackbox.io.sh_ddr_stat_addr0   := io.port.sh_ddr_stat_addr0  
    blackbox.io.sh_ddr_stat_wdata0  := io.port.sh_ddr_stat_wdata0 
    io.port.ddr_sh_stat_rdata0      := blackbox.io.ddr_sh_stat_rdata0
    io.port.ddr_sh_stat_int0        := blackbox.io.ddr_sh_stat_int0
    blackbox.io.sh_ddr_stat_wr0     := io.port.sh_ddr_stat_wr0    
    blackbox.io.sh_ddr_stat_rd0     := io.port.sh_ddr_stat_rd0    
    io.port.ddr_sh_stat_ack0        := blackbox.io.ddr_sh_stat_ack0
    // block B
    blackbox.io.sh_ddr_stat_addr1   := io.port.sh_ddr_stat_addr1  
    blackbox.io.sh_ddr_stat_wdata1  := io.port.sh_ddr_stat_wdata1 
    io.port.ddr_sh_stat_rdata1      := blackbox.io.ddr_sh_stat_rdata1
    io.port.ddr_sh_stat_int1        := blackbox.io.ddr_sh_stat_int1
    blackbox.io.sh_ddr_stat_wr1     := io.port.sh_ddr_stat_wr1    
    blackbox.io.sh_ddr_stat_rd1     := io.port.sh_ddr_stat_rd1    
    io.port.ddr_sh_stat_ack1        := blackbox.io.ddr_sh_stat_ack1
    // block D
    blackbox.io.sh_ddr_stat_addr2   := io.port.sh_ddr_stat_addr2  
    blackbox.io.sh_ddr_stat_wdata2  := io.port.sh_ddr_stat_wdata2 
    io.port.ddr_sh_stat_rdata2      := blackbox.io.ddr_sh_stat_rdata2
    io.port.ddr_sh_stat_int2        := blackbox.io.ddr_sh_stat_int2
    blackbox.io.sh_ddr_stat_wr2     := io.port.sh_ddr_stat_wr2    
    blackbox.io.sh_ddr_stat_rd2     := io.port.sh_ddr_stat_rd2    
    io.port.ddr_sh_stat_ack2        := blackbox.io.ddr_sh_stat_ack2

    //--------------------------
    // AXI
    //--------------------------
    blackbox.io.cl_sh_ddr_awid(0)     := axi_async.aw.bits.id
    blackbox.io.cl_sh_ddr_awaddr(0)   := axi_async.aw.bits.addr 
    blackbox.io.cl_sh_ddr_awlen(0)    := axi_async.aw.bits.len
    blackbox.io.cl_sh_ddr_awsize(0)   := axi_async.aw.bits.size
    blackbox.io.cl_sh_ddr_awburst(0)  := axi_async.aw.bits.burst
    blackbox.io.cl_sh_ddr_awvalid(0)  := axi_async.aw.valid
    axi_async.aw.ready                := blackbox.io.sh_cl_ddr_awready(0)
    
    // Amazon claims their IO is AXI-4 but Arm says otherwise; there is no WID in AXI-4
    // leave disconnected so Chisel ties it to 0
    //blackbox.io.cl_sh_ddr_wid     :=

    blackbox.io.cl_sh_ddr_wdata(0)    := axi_async.w.bits.data
    blackbox.io.cl_sh_ddr_wstrb(0)    := axi_async.w.bits.strb
    blackbox.io.cl_sh_ddr_wlast(0)    := axi_async.w.bits.last
    blackbox.io.cl_sh_ddr_wvalid(0)   := axi_async.w.valid
    axi_async.w.ready                 := blackbox.io.sh_cl_ddr_wready(0)
    axi_async.b.bits.id               := blackbox.io.sh_cl_ddr_bid(0)
    axi_async.b.bits.resp             := blackbox.io.sh_cl_ddr_bresp(0)   
    axi_async.b.valid                 := blackbox.io.sh_cl_ddr_bvalid(0) 
    blackbox.io.cl_sh_ddr_bready(0)   := axi_async.b.ready
    blackbox.io.cl_sh_ddr_arid(0)     := axi_async.ar.bits.id 
    blackbox.io.cl_sh_ddr_araddr(0)   := axi_async.ar.bits.addr
    blackbox.io.cl_sh_ddr_arlen(0)    := axi_async.ar.bits.len
    blackbox.io.cl_sh_ddr_arsize(0)   := axi_async.ar.bits.size
    blackbox.io.cl_sh_ddr_arburst(0)  := axi_async.ar.bits.burst
    blackbox.io.cl_sh_ddr_arvalid(0)  := axi_async.ar.valid
    axi_async.ar.ready                := blackbox.io.sh_cl_ddr_arready(0)
    axi_async.r.bits.id               := blackbox.io.sh_cl_ddr_rid(0)
    axi_async.r.bits.data             := blackbox.io.sh_cl_ddr_rdata(0)
    axi_async.r.bits.resp             := blackbox.io.sh_cl_ddr_rresp(0)
    axi_async.r.bits.last             := blackbox.io.sh_cl_ddr_rlast(0)
    axi_async.r.valid                 := blackbox.io.sh_cl_ddr_rvalid(0)
    blackbox.io.cl_sh_ddr_rready(0)   := axi_async.r.ready

    // also no idea what to do here; we'll leave it disconnected for now
    //io.sh_cl_ddr_is_ready         := blackbox.io.sh_cl_ddr_is_ready   
  }
}
