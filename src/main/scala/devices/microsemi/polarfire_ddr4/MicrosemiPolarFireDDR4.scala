// See LICENSE for license details.
package sifive.fpgashells.devices.microsemi.polarfireddr4

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.subsystem._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import sifive.fpgashells.ip.microsemi.polarfireddr4.{PolarFireEvalKitDDR4IOClocksReset, PolarFireEvalKitDDR4IODDR, DDR4_Subsys}

case class PolarFireEvalKitDDR4Params(
  address : Seq[AddressSet]
)

class PolarFireEvalKitDDR4Pads(depth : BigInt) extends PolarFireEvalKitDDR4IODDR(depth) {
  def this(c : PolarFireEvalKitDDR4Params) {
    this(AddressRange.fromSets(c.address).head.size)
  }
}

class PolarFireEvalKitDDR4IO(depth : BigInt) extends PolarFireEvalKitDDR4IODDR(depth) with PolarFireEvalKitDDR4IOClocksReset

class PolarFireEvalKitDDR4Island(c : PolarFireEvalKitDDR4Params)(implicit p: Parameters) extends LazyModule with CrossesToOnlyOneClockDomain {
  val ranges = AddressRange.fromSets(c.address)
  require (ranges.size == 1, "DDR range must be contiguous")
  val offset = ranges.head.base
  val depth = ranges.head.size
  val crossing = AsynchronousCrossing(8)
  require((depth<=0x100000000L),"PF Eval Kit supports upto 4GB depth configuraton")
  
  val device = new MemoryDevice
  val node = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
      slaves = Seq(AXI4SlaveParameters(
      address       = c.address,
      resources     = device.reg,
      regionType    = RegionType.UNCACHED,
      executable    = true,
      supportsWrite = TransferSizes(1, 128),
      supportsRead  = TransferSizes(1, 128))),
    beatBytes = 8)))

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val port = new PolarFireEvalKitDDR4IO(depth)
    })

    // DDR block black box instantiation
    val blackbox = Module(new DDR4_Subsys(depth))
    val (axi_async, _) = node.in(0)

    //pins to top level

    //inouts
    attach(io.port.DQ,blackbox.io.DQ)
    attach(io.port.DQS_N,blackbox.io.DQS_N)
    attach(io.port.DQS,blackbox.io.DQS)
 
    //outputs
    io.port.A                 := blackbox.io.A
    io.port.ACT_N             := blackbox.io.ACT_N
    io.port.BA                := blackbox.io.BA
    io.port.BG                := blackbox.io.BG
    io.port.RAS_N             := blackbox.io.RAS_N
    io.port.CAS_N             := blackbox.io.CAS_N
    io.port.WE_N              := blackbox.io.WE_N
    io.port.RESET_N           := blackbox.io.RESET_N
    io.port.CK0               := blackbox.io.CK0
    io.port.CK0_N             := blackbox.io.CK0_N
    io.port.CKE               := blackbox.io.CKE
    io.port.CS_N              := blackbox.io.CS_N
    io.port.DM_N              := blackbox.io.DM_N
    io.port.ODT               := blackbox.io.ODT

    io.port.CTRLR_READY       := blackbox.io.CTRLR_READY
    io.port.SHIELD0           := blackbox.io.SHIELD0
    io.port.SHIELD1           := blackbox.io.SHIELD1

    //inputs
    val awaddr = axi_async.aw.bits.addr - UInt(offset)
    val araddr = axi_async.ar.bits.addr - UInt(offset)

    //slave AXI interface write address ports
    blackbox.io.axi0_awid    := axi_async.aw.bits.id
    blackbox.io.axi0_awaddr  := awaddr //truncated
    blackbox.io.axi0_awlen   := axi_async.aw.bits.len
    blackbox.io.axi0_awsize  := axi_async.aw.bits.size
    blackbox.io.axi0_awburst := axi_async.aw.bits.burst
    blackbox.io.axi0_awlock  := axi_async.aw.bits.lock
    blackbox.io.axi0_awcache := UInt("b0011")
    blackbox.io.axi0_awprot  := axi_async.aw.bits.prot
    blackbox.io.axi0_awvalid := axi_async.aw.valid
    axi_async.aw.ready        := blackbox.io.axi0_awready

    //slave interface write data ports
    blackbox.io.axi0_wdata   := axi_async.w.bits.data
    blackbox.io.axi0_wstrb   := axi_async.w.bits.strb
    blackbox.io.axi0_wlast   := axi_async.w.bits.last
    blackbox.io.axi0_wvalid  := axi_async.w.valid
    axi_async.w.ready         := blackbox.io.axi0_wready

    //slave interface write response
    blackbox.io.axi0_bready  := axi_async.b.ready
    axi_async.b.bits.id       := blackbox.io.axi0_bid
    axi_async.b.bits.resp     := blackbox.io.axi0_bresp
    axi_async.b.valid         := blackbox.io.axi0_bvalid

    //slave AXI interface read address ports
    blackbox.io.axi0_arid    := axi_async.ar.bits.id
    blackbox.io.axi0_araddr  := araddr
    blackbox.io.axi0_arlen   := axi_async.ar.bits.len
    blackbox.io.axi0_arsize  := axi_async.ar.bits.size
    blackbox.io.axi0_arburst := axi_async.ar.bits.burst
    blackbox.io.axi0_arlock  := axi_async.ar.bits.lock
    blackbox.io.axi0_arcache := UInt("b0011")
    blackbox.io.axi0_arprot  := axi_async.ar.bits.prot
    blackbox.io.axi0_arvalid := axi_async.ar.valid
    axi_async.ar.ready        := blackbox.io.axi0_arready

    //slace AXI interface read data ports
    blackbox.io.axi0_rready  := axi_async.r.ready
    axi_async.r.bits.id       := blackbox.io.axi0_rid
    axi_async.r.bits.data     := blackbox.io.axi0_rdata
    axi_async.r.bits.resp     := blackbox.io.axi0_rresp
    axi_async.r.bits.last     := blackbox.io.axi0_rlast
    axi_async.r.valid         := blackbox.io.axi0_rvalid

    //misc
    blackbox.io.AXI0_AWUSERTAG := UInt("b0000")
    blackbox.io.SYS_RESET_N    :=io.port.SYS_RESET_N
    blackbox.io.PLL_REF_CLK    :=io.port.PLL_REF_CLK
    
    io.port.SYS_CLK := blackbox.io.SYS_CLK
    io.port.RESET_N := blackbox.io.RESET_N
    io.port.PLL_LOCK := blackbox.io.PLL_LOCK
  }
}

class PolarFireEvalKitDDR4(c : PolarFireEvalKitDDR4Params)(implicit p: Parameters) extends LazyModule {
  val ranges = AddressRange.fromSets(c.address)
  val depth = ranges.head.size

  val buffer  = LazyModule(new TLBuffer)
  val toaxi4  = LazyModule(new TLToAXI4(adapterName = Some("mem"), stripBits = 1))
  val indexer = LazyModule(new AXI4IdIndexer(idBits = 4))
  val deint   = LazyModule(new AXI4Deinterleaver(p(CacheBlockBytes)))
  val yank    = LazyModule(new AXI4UserYanker)
  val island  = LazyModule(new PolarFireEvalKitDDR4Island(c))

  val node: TLInwardNode =
    island.crossAXI4In(island.node) := yank.node := deint.node := indexer.node := toaxi4.node := buffer.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val port = new PolarFireEvalKitDDR4IO(depth)
    })

    io.port <> island.module.io.port

    // Shove the island
    island.module.clock := io.port.SYS_CLK
    island.module.reset := !io.port.CTRLR_READY
  }
}
