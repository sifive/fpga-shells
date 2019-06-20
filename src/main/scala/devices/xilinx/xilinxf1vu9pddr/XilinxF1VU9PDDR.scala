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
  
  val island  = LazyModule(new XilinxF1VU9PDDRIsland(c))
  
	val buffer = LazyModule(new TLBuffer)
  val xbar = LazyModule(new TLXbar)
  xbar.node := buffer.node
  
	island.AXIslavenodes.foreach { n =>
    val toaxi4  = LazyModule(new TLToAXI4(adapterName = Some("mem"), stripBits = 1))
    val indexer = LazyModule(new AXI4IdIndexer(idBits = 16))
    val deint   = LazyModule(new AXI4Deinterleaver(p(CacheBlockBytes)))
    val yank    = LazyModule(new AXI4UserYanker)
    island.crossAXI4In(n) := yank.node := deint.node := indexer.node := toaxi4.node := xbar.node
  }
  
  val node: TLInwardNode = buffer.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val analog = new Bundle with F1VU9PDDRIO
      val directioned = new F1VU9PDDRBase
    })

    io <> island.module.io
    
    // connect inferred clock/reset
    //island.module.clock := io.port.clk
    //island.module.reset := !io.port.rst_n
  }
}

// util for automated connection of bundles with common elements
// connects all and only the elements that are common between left and right bundles,
// up to user to safely connect rest of elements
object unsafeBulkConnect {
  def apply[T <: Bundle](left: T, right: T): Unit = {
    // if they're equal, we can go ahead and connect them up
    if (left.elements == right.elements) {
      left <> right
    } else {
      // otherwise, get their intersection
      // for each item in the intersection, if it's a subtype of Bundle, recurse
      // if it's not a subtype of Bundle, then just connect it up
      // this allows for recursive bundles with not all matching names
  	  val intersection = left.elements.keys.filter{ k => right.elements.contains(k) }
  	  intersection.foreach { k => 
        (left.elements(k), right.elements(k)) match {
          case (left_t: T, right_t: T) => { apply(left_t, right_t) }
          case (left_d, right_d) => { left_d <> right_d }
        }
  	  }
    }
  }
}

class XilinxF1VU9PDDRIsland(c: XilinxF1VU9PDDRParams)(implicit p: Parameters) extends LazyModule with CrossesToOnlyOneClockDomain {
  //override val compileOptions = chisel3.core.ExplicitCompileOptions.Strict.copy(explicitInvalidate = false)

  val crossing = AsynchronousCrossing(8)
  
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
    )
  /*
  val node = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
      slaves = Seq(AXI4SlaveParameters(
      address       = c.addresses(0),
      resources     = (new MemoryDevice).reg,
      regionType    = RegionType.UNCACHED,
      executable    = true,
      supportsWrite = TransferSizes(1, 256*8),
      supportsRead  = TransferSizes(1, 256*8))),
      beatBytes = 8)))*/
  
  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val analog = new Bundle with F1VU9PDDRIO
      val directioned = new F1VU9PDDRBase
    })

    val blackbox = Module(new sh_ddr(c.instantiate)) // has F1VU9PDDRIO with F1VU9PAXISignals
    val (a_axi_async,_) = AXIslavenodes(0).in(0)
    val (b_axi_async,_) = AXIslavenodes(1).in(0)
    val (d_axi_async,_) = AXIslavenodes(2).in(0)
    val axi = Seq(a_axi_async, b_axi_async, d_axi_async)
    
    //-----------------------------
    // DDR connections
    //-----------------------------
    // Analog (inout) connections
    attach(blackbox.io.M_A_DQ,      io.analog.M_A_DQ)
    attach(blackbox.io.M_A_ECC,     io.analog.M_A_ECC)
    attach(blackbox.io.M_A_DQS_DP,  io.analog.M_A_DQS_DP)
    attach(blackbox.io.M_A_DQS_DN,  io.analog.M_A_DQS_DN)
    attach(blackbox.io.M_B_DQ,      io.analog.M_B_DQ)
    attach(blackbox.io.M_B_ECC,     io.analog.M_B_ECC)
    attach(blackbox.io.M_B_DQS_DP,  io.analog.M_B_DQS_DP)
    attach(blackbox.io.M_B_DQS_DN,  io.analog.M_B_DQS_DN)
    attach(blackbox.io.M_D_DQ,      io.analog.M_D_DQ)
    attach(blackbox.io.M_D_ECC,     io.analog.M_D_ECC)
    attach(blackbox.io.M_D_DQS_DP,  io.analog.M_D_DQS_DP)
    attach(blackbox.io.M_D_DQS_DN,  io.analog.M_D_DQS_DN)
    // OUTPUTS
    // block A
    unsafeBulkConnect(io.directioned, blackbox.io)
    /*
    io.directioned.M_A_ACT_N       := blackbox.io.M_A_ACT_N
    io.directioned.M_A_MA          := blackbox.io.M_A_MA
    io.directioned.M_A_BA          := blackbox.io.M_A_BA
    io.directioned.M_A_BG          := blackbox.io.M_A_BG
    io.directioned.M_A_CKE         := blackbox.io.M_A_CKE
    io.directioned.M_A_ODT         := blackbox.io.M_A_ODT
    io.directioned.M_A_CS_N        := blackbox.io.M_A_CS_N
    io.directioned.M_A_CLK_DN      := blackbox.io.M_A_CLK_DN
    io.directioned.M_A_CLK_DP      := blackbox.io.M_A_CLK_DP
    io.directioned.M_A_PAR         := blackbox.io.M_A_PAR
    io.directioned.cl_RST_DIMM_A_N := blackbox.io.cl_RST_DIMM_A_N
    // block B
    io.directioned.M_B_ACT_N       := blackbox.io.M_B_ACT_N
    io.directioned.M_B_MA          := blackbox.io.M_B_MA
    io.directioned.M_B_BA          := blackbox.io.M_B_BA
    io.directioned.M_B_BG          := blackbox.io.M_B_BG
    io.directioned.M_B_CKE         := blackbox.io.M_B_CKE
    io.directioned.M_B_ODT         := blackbox.io.M_B_ODT
    io.directioned.M_B_CS_N        := blackbox.io.M_B_CS_N
    io.directioned.M_B_CLK_DN      := blackbox.io.M_B_CLK_DN
    io.directioned.M_B_CLK_DP      := blackbox.io.M_B_CLK_DP
    io.directioned.M_B_PAR         := blackbox.io.M_B_PAR
    io.directioned.cl_RST_DIMM_B_N := blackbox.io.cl_RST_DIMM_B_N
    // block D                 .
    io.directioned.M_D_ACT_N       := blackbox.io.M_D_ACT_N
    io.directioned.M_D_MA          := blackbox.io.M_D_MA
    io.directioned.M_D_BA          := blackbox.io.M_D_BA
    io.directioned.M_D_BG          := blackbox.io.M_D_BG
    io.directioned.M_D_CKE         := blackbox.io.M_D_CKE
    io.directioned.M_D_ODT         := blackbox.io.M_D_ODT
    io.directioned.M_D_CS_N        := blackbox.io.M_D_CS_N
    io.directioned.M_D_CLK_DN      := blackbox.io.M_D_CLK_DN
    io.directioned.M_D_CLK_DP      := blackbox.io.M_D_CLK_DP
    io.directioned.M_D_PAR         := blackbox.io.M_D_PAR
    io.directioned.cl_RST_DIMM_D_N := blackbox.io.cl_RST_DIMM_D_N
    // INPUTS
    // block A
    blackbox.io.CLK_300M_DIMM0_DP := io.directioned.CLK_300M_DIMM0_DP
    blackbox.io.CLK_300M_DIMM0_DN := io.directioned.CLK_300M_DIMM0_DN
    // block B
    blackbox.io.CLK_300M_DIMM1_DP := io.directioned.CLK_300M_DIMM1_DP
    blackbox.io.CLK_300M_DIMM1_DN := io.directioned.CLK_300M_DIMM1_DN
    // block D
    blackbox.io.CLK_300M_DIMM3_DP := io.directioned.CLK_300M_DIMM3_DP
    blackbox.io.CLK_300M_DIMM3_DN := io.directioned.CLK_300M_DIMM3_DN
    
    //----------------------------
    // Management
    //----------------------------
    // Clocks/Resets
    blackbox.io.clk         := io.directioned.clk              
    blackbox.io.rst_n       := io.directioned.rst_n            
    blackbox.io.stat_clk    := io.directioned.stat_clk         
    blackbox.io.stat_rst_n  := io.directioned.stat_rst_n
    
    // DDR status
    // block A
    blackbox.io.sh_ddr_stat_addr0     := io.directioned.sh_ddr_stat_addr0  
    blackbox.io.sh_ddr_stat_wdata0    := io.directioned.sh_ddr_stat_wdata0 
    io.directioned.ddr_sh_stat_rdata0 := blackbox.io.ddr_sh_stat_rdata0
    io.directioned.ddr_sh_stat_int0   := blackbox.io.ddr_sh_stat_int0
    blackbox.io.sh_ddr_stat_wr0       := io.directioned.sh_ddr_stat_wr0    
    blackbox.io.sh_ddr_stat_rd0       := io.directioned.sh_ddr_stat_rd0    
    io.directioned.ddr_sh_stat_ack0   := blackbox.io.ddr_sh_stat_ack0
    // block B
    blackbox.io.sh_ddr_stat_addr1     := io.directioned.sh_ddr_stat_addr1  
    blackbox.io.sh_ddr_stat_wdata1    := io.directioned.sh_ddr_stat_wdata1 
    io.directioned.ddr_sh_stat_rdata1 := blackbox.io.ddr_sh_stat_rdata1
    io.directioned.ddr_sh_stat_int1   := blackbox.io.ddr_sh_stat_int1
    blackbox.io.sh_ddr_stat_wr1       := io.directioned.sh_ddr_stat_wr1    
    blackbox.io.sh_ddr_stat_rd1       := io.directioned.sh_ddr_stat_rd1    
    io.directioned.ddr_sh_stat_ack1   := blackbox.io.ddr_sh_stat_ack1
    // block D
    blackbox.io.sh_ddr_stat_addr2     := io.directioned.sh_ddr_stat_addr2  
    blackbox.io.sh_ddr_stat_wdata2    := io.directioned.sh_ddr_stat_wdata2 
    io.directioned.ddr_sh_stat_rdata2 := blackbox.io.ddr_sh_stat_rdata2
    io.directioned.ddr_sh_stat_int2   := blackbox.io.ddr_sh_stat_int2
    blackbox.io.sh_ddr_stat_wr2       := io.directioned.sh_ddr_stat_wr2    
    blackbox.io.sh_ddr_stat_rd2       := io.directioned.sh_ddr_stat_rd2    
    io.directioned.ddr_sh_stat_ack2   := blackbox.io.ddr_sh_stat_ack2
		*/
		//--------------------------
    // AXI
    //--------------------------
		
    blackbox.io.cl_sh_ddr_awid    := Vec(axi.map(_.aw.bits.id))
    blackbox.io.cl_sh_ddr_awaddr  := Vec(axi.map(_.aw.bits.addr)) 
    blackbox.io.cl_sh_ddr_awlen   := Vec(axi.map(_.aw.bits.len))
    blackbox.io.cl_sh_ddr_awsize  := Vec(axi.map(_.aw.bits.size))
    blackbox.io.cl_sh_ddr_awburst := Vec(axi.map(_.aw.bits.burst))
    blackbox.io.cl_sh_ddr_awvalid := Vec(axi.map(_.aw.valid))
    Vec(axi.map(_.aw.ready))      := blackbox.io.sh_cl_ddr_awready
    
    // Amazon claims their IO is AXI-4 but Arm says otherwise; there is no WID in AXI-4
    // leave disconnected so Chisel ties it to 0
    //blackbox.io.cl_sh_ddr_wid     :=
    blackbox.io.cl_sh_ddr_wdata   := Vec(axi.map(_.w.bits.data))
    blackbox.io.cl_sh_ddr_wstrb   := Vec(axi.map(_.w.bits.strb))
    blackbox.io.cl_sh_ddr_wlast   := Vec(axi.map(_.w.bits.last))
    blackbox.io.cl_sh_ddr_wvalid  := Vec(axi.map(_.w.valid))
    Vec(axi.map(_.w.ready))       := blackbox.io.sh_cl_ddr_wready
    Vec(axi.map(_.b.bits.id))     := blackbox.io.sh_cl_ddr_bid      
    Vec(axi.map(_.b.bits.resp))   := blackbox.io.sh_cl_ddr_bresp    
    Vec(axi.map(_.b.valid))       := blackbox.io.sh_cl_ddr_bvalid   
    blackbox.io.cl_sh_ddr_bready  := Vec(axi.map(_.b.ready))
    blackbox.io.cl_sh_ddr_arid    := Vec(axi.map(_.ar.bits.id)) 
    blackbox.io.cl_sh_ddr_araddr  := Vec(axi.map(_.ar.bits.addr))
    blackbox.io.cl_sh_ddr_arlen   := Vec(axi.map(_.ar.bits.len))
    blackbox.io.cl_sh_ddr_arsize  := Vec(axi.map(_.ar.bits.size))
    blackbox.io.cl_sh_ddr_arburst := Vec(axi.map(_.ar.bits.burst))
    blackbox.io.cl_sh_ddr_arvalid := Vec(axi.map(_.ar.valid))
    Vec(axi.map(_.ar.ready))      := blackbox.io.sh_cl_ddr_arready  
    Vec(axi.map(_.r.bits.id))     := blackbox.io.sh_cl_ddr_rid      
    Vec(axi.map(_.r.bits.data))   := blackbox.io.sh_cl_ddr_rdata    
    Vec(axi.map(_.r.bits.resp))   := blackbox.io.sh_cl_ddr_rresp    
    Vec(axi.map(_.r.bits.last))   := blackbox.io.sh_cl_ddr_rlast    
    Vec(axi.map(_.r.valid))       := blackbox.io.sh_cl_ddr_rvalid   
    blackbox.io.cl_sh_ddr_rready  := Vec(axi.map(_.r.ready))
    // also no idea what to do here; we'll leave it disconnected for now
    //io.sh_cl_ddr_is_ready         := blackbox.io.sh_cl_ddr_is_ready 
  }
}
