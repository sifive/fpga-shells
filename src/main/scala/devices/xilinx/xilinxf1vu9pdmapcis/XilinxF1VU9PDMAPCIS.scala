// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxf1vu9paxi4pcis

import chisel3._
import chisel3.util._
import chisel3.experimental._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.amba.axi4.AXI4MasterParameters
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.subsystem._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._

case class AXI4PCISParams(name: String, mIDBits: Integer, busBytes: Integer) {}

class AXI4PCISPads extends Bundle {
val sh_cl_dma_pcis_awid     = Input(UInt(6.W))
val sh_cl_dma_pcis_awaddr   = Input(UInt(64.W))
val sh_cl_dma_pcis_awlen    = Input(UInt(8.W))
val sh_cl_dma_pcis_awsize   = Input(UInt(3.W))
val sh_cl_dma_pcis_awvalid  = Input(Bool())
val cl_sh_dma_pcis_awready  = Output(Bool())

val sh_cl_dma_pcis_wdata    = Input(UInt(512.W))
val sh_cl_dma_pcis_wstrb    = Input(UInt(64.W))
val sh_cl_dma_pcis_wlast    = Input(Bool())
val sh_cl_dma_pcis_wvalid   = Input(Bool())
val cl_sh_dma_pcis_wready   = Output(Bool())

val cl_sh_dma_pcis_bid      = Output(UInt(6.W))
val cl_sh_dma_pcis_bresp    = Output(UInt(2.W))
val cl_sh_dma_pcis_bvalid   = Output(Bool())
val sh_cl_dma_pcis_bready   = Input(Bool())

val sh_cl_dma_pcis_arid     = Input(UInt(6.W))
val sh_cl_dma_pcis_araddr   = Input(UInt(64.W))
val sh_cl_dma_pcis_arlen    = Input(UInt(8.W))
val sh_cl_dma_pcis_arsize   = Input(UInt(3.W))
val sh_cl_dma_pcis_arvalid  = Input(Bool())
val cl_sh_dma_pcis_arready  = Output(Bool())

val cl_sh_dma_pcis_rid      = Output(UInt(6.W))
val cl_sh_dma_pcis_rdata    = Output(UInt(512.W))
val cl_sh_dma_pcis_rresp    = Output(UInt(2.W))
val cl_sh_dma_pcis_rlast    = Output(Bool())
val cl_sh_dma_pcis_rvalid   = Output(Bool())
val cl_sh_dma_pcis_rready   = Input(Bool())
}

class XilinxF1VU9PAXI4PCIS(c: AXI4PCISParams)(implicit p: Parameters) extends LazyModule {
  
  val master = AXI4MasterNode(Seq(AXI4MasterPortParameters(
    masters = Seq(AXI4MasterParameters(
      name = c.name,
      id   = IdRange(0, 1 << c.mIDBits),
      aligned = false)))))
  
  val node: TLOutwardNode =
    (TLWidthWidget(c.busBytes)
      := AXI4ToTL()
      := AXI4UserYanker(capMaxFlight=Some(16))
      := AXI4Fragmenter()
      := AXI4IdIndexer(idBits=2)
      := master)

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new AXI4PCISPads)

    val (axi,_) = master.out(0)

    axi.aw.bits.id            := io.sh_cl_dma_pcis_awid
    axi.aw.bits.addr          := io.sh_cl_dma_pcis_awaddr
    axi.aw.bits.len           := io.sh_cl_dma_pcis_awlen
    axi.aw.bits.size          := io.sh_cl_dma_pcis_awsize
    axi.aw.valid              := io.sh_cl_dma_pcis_awvalid
    io.cl_sh_dma_pcis_awready := axi.aw.ready

    axi.w.bits.data           := io.sh_cl_dma_pcis_wdata
    axi.w.bits.strb           := io.sh_cl_dma_pcis_wstrb
    axi.w.bits.last           := io.sh_cl_dma_pcis_wlast
    axi.w.valid               := io.sh_cl_dma_pcis_wvalid
    io.cl_sh_dma_pcis_wready  := axi.w.ready

    io.cl_sh_dma_pcis_bid     := axi.b.bits.id
    io.cl_sh_dma_pcis_bresp   := axi.b.bits.resp
    io.cl_sh_dma_pcis_bvalid  := axi.b.valid
    axi.b.ready               := io.sh_cl_dma_pcis_bready

    axi.ar.bits.id            := io.sh_cl_dma_pcis_arid
    axi.ar.bits.addr          := io.sh_cl_dma_pcis_araddr
    axi.ar.bits.len           := io.sh_cl_dma_pcis_arlen
    axi.ar.bits.size          := io.sh_cl_dma_pcis_arsize
    axi.ar.valid              := io.sh_cl_dma_pcis_arvalid
    io.cl_sh_dma_pcis_arready := axi.ar.ready

    io.cl_sh_dma_pcis_rid     := axi.r.bits.id
    io.cl_sh_dma_pcis_rdata   := axi.r.bits.data
    io.cl_sh_dma_pcis_rresp   := axi.r.bits.resp
    io.cl_sh_dma_pcis_rlast   := axi.r.bits.last
    io.cl_sh_dma_pcis_rvalid  := axi.r.valid
    axi.r.ready               := io.cl_sh_dma_pcis_rready
  }
}
