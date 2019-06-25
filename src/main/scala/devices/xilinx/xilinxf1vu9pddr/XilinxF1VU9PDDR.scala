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

case class XilinxF1VU9PDDRParams(addresses : Seq[Seq[AddressSet]], instantiate : Seq[Boolean]) {
}

class XilinxF1VU9PDDR(c: XilinxF1VU9PDDRParams)(implicit p: Parameters) extends LazyModule {
  
  val island  = LazyModule(new XilinxF1VU9PDDRIsland(c))
  
	val buffer = LazyModule(new TLBuffer)
  val xbar = LazyModule(new TLXbar)
  xbar.node := buffer.node
  
  island.slavenodes.foreach { n =>
    val toaxi4  = LazyModule(new TLToAXI4(adapterName = Some("mem"), stripBits = 1))
    val indexer = LazyModule(new AXI4IdIndexer(idBits = 4))
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
      // for each item in the intersection, connect it up
  	  val intersection = left.elements.keys.filter{ k => right.elements.contains(k) }
  	  intersection.foreach { k => left.elements(k) <> right.elements(k) }
    }
  }
}

class XilinxF1VU9PDDRIsland(c: XilinxF1VU9PDDRParams)(implicit p: Parameters) extends LazyModule with CrossesToOnlyOneClockDomain {
  //override val compileOptions = chisel3.core.ExplicitCompileOptions.Strict.copy(explicitInvalidate = false)

  val crossing = AsynchronousCrossing(8)
  
  val slavenodes = c.addresses.map { address => AXI4SlaveNode(Seq(AXI4SlavePortParameters(
      slaves = Seq(AXI4SlaveParameters(
      address       = address,
      resources     = (new MemoryDevice).reg,
      regionType    = RegionType.UNCACHED,
      executable    = true,
      supportsWrite = TransferSizes(1, 256*8),
      supportsRead  = TransferSizes(1, 256*8))),
    beatBytes = 8)))
  }
  
  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val analog = new Bundle with F1VU9PDDRIO
      val directioned = new F1VU9PDDRBase
    })

    val blackbox = Module(new sh_ddr(c.instantiate)) // has F1VU9PDDRIO with F1VU9PAXISignals
    val (axi_a,_) = slavenodes(0).in(0)
    val (axi_b,_) = slavenodes(1).in(0)
    val (axi_d,_) = slavenodes(2).in(0)
    val axi = Seq(axi_a, axi_b, axi_d)

    // Connect Analog (inout)
    unsafeBulkConnect(io.analog, blackbox.io)
    // Connect directioned IO
    unsafeBulkConnect(io.directioned, blackbox.io)
    
    // Connect AXI slave node to AXI IO
    blackbox.io.cl_sh_ddr_awid    := VecInit(axi.map{ a: AXI4Bundle => Cat(0.U(12.W), a.aw.bits.id) })
    blackbox.io.cl_sh_ddr_awaddr  := VecInit(axi.map(_.aw.bits.addr)) 
    blackbox.io.cl_sh_ddr_awlen   := VecInit(axi.map(_.aw.bits.len))
    blackbox.io.cl_sh_ddr_awsize  := VecInit(axi.map(_.aw.bits.size))
    blackbox.io.cl_sh_ddr_awburst := VecInit(axi.map(_.aw.bits.burst))
    blackbox.io.cl_sh_ddr_awvalid := VecInit(axi.map(_.aw.valid))
    VecInit(axi.map(_.aw.ready))  := blackbox.io.sh_cl_ddr_awready
    
    // Amazon claims their IO is AXI-4 but Arm says otherwise; there is no WID in AXI-4
    // leave disconnected so Chisel ties it to 0
    //blackbox.io.cl_sh_ddr_wid     :=
    blackbox.io.cl_sh_ddr_wdata       := VecInit(axi.map(_.w.bits.data))
    blackbox.io.cl_sh_ddr_wstrb       := VecInit(axi.map(_.w.bits.strb))
    blackbox.io.cl_sh_ddr_wlast       := VecInit(axi.map(_.w.bits.last))
    blackbox.io.cl_sh_ddr_wvalid      := VecInit(axi.map(_.w.valid))
    VecInit(axi.map(_.w.ready))       := blackbox.io.sh_cl_ddr_wready
    VecInit(axi.map(_.b.bits.id))     := blackbox.io.sh_cl_ddr_bid.map(_(3,0))
    VecInit(axi.map(_.b.bits.resp))   := blackbox.io.sh_cl_ddr_bresp    
    VecInit(axi.map(_.b.valid))       := blackbox.io.sh_cl_ddr_bvalid   
    blackbox.io.cl_sh_ddr_bready      := VecInit(axi.map(_.b.ready))
    blackbox.io.cl_sh_ddr_arid        := VecInit(axi.map{ a: AXI4Bundle => Cat(0.U(12.W), a.ar.bits.id) })
    blackbox.io.cl_sh_ddr_araddr      := VecInit(axi.map(_.ar.bits.addr))
    blackbox.io.cl_sh_ddr_arlen       := VecInit(axi.map(_.ar.bits.len))
    blackbox.io.cl_sh_ddr_arsize      := VecInit(axi.map(_.ar.bits.size))
    blackbox.io.cl_sh_ddr_arburst     := VecInit(axi.map(_.ar.bits.burst))
    blackbox.io.cl_sh_ddr_arvalid     := VecInit(axi.map(_.ar.valid))
    VecInit(axi.map(_.ar.ready))      := blackbox.io.sh_cl_ddr_arready  
    VecInit(axi.map(_.r.bits.id))     := blackbox.io.sh_cl_ddr_rid.map(_(3,0))      
    VecInit(axi.map(_.r.bits.data))   := blackbox.io.sh_cl_ddr_rdata    
    VecInit(axi.map(_.r.bits.resp))   := blackbox.io.sh_cl_ddr_rresp    
    VecInit(axi.map(_.r.bits.last))   := blackbox.io.sh_cl_ddr_rlast    
    VecInit(axi.map(_.r.valid))       := blackbox.io.sh_cl_ddr_rvalid   
    blackbox.io.cl_sh_ddr_rready      := VecInit(axi.map(_.r.ready))
    // also no idea what to do here; we'll leave it disconnected for now
    //io.sh_cl_ddr_is_ready         := blackbox.io.sh_cl_ddr_is_ready 
  }
}
