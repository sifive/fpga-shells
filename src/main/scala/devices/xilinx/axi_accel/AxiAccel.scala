// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvcu118mig

import Chisel._
import chisel3.experimental.{Analog,attach}
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.subsystem._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import sifive.fpgashells.ip.xilinx.vcu118mig.{AxiAccelClocksReset, VCU118AccelIO, axiaccel}

case class AxiAccelParams(
  address : Seq[AddressSet]
)

class AxiAccel(c : AxiAccelParams)(implicit p: Parameters) extends LazyModule {
  val AXIDATAWIDTH = 128

  val dtsDevice = new SimpleDevice("axi_accel", Seq("intel,accel_0"))

  val axi_master_node = AXI4MasterNode(Seq(AXI4MasterPortParameters(
      masters = Seq(AXI4MasterParameters(
        name    = "accel_master",
        id      = IdRange(0, 1<<AXIDATAWIDTH),
        aligned = false)))))

  val axi_slave_node = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = List(AddressSet(0x100000000L, 0x7fffffffL)),
      resources     = dtsDevice.reg("axi_slave"),
      executable    = true,
      supportsWrite = TransferSizes(1, 128),
      supportsRead  = TransferSizes(1, 128))),
    beatBytes = 8)))

  val tl_master_node =
    (TLBuffer()
      := TLWidthWidget(AXIDATAWIDTH/8)
      := AXI4ToTL()
      := AXI4UserYanker(Some(4))
      := AXI4Fragmenter()
      := AXI4IdIndexer(idBits=2)
      := AXI4Buffer()
      := axi_master_node)

  val tl_slave_node: TLInwardNode =
    (axi_slave_node
      := AXI4Buffer()
      := AXI4UserYanker()
      := AXI4Deinterleaver(p(CacheBlockBytes))
      := AXI4IdIndexer(idBits=4)
      := TLToAXI4(adapterName = Some("axislave")))

 // val ioNode = BundleBridgeSource(() => (new VCU118AccelIO).flip)

  lazy val module = new LazyModuleImp(this) {
    //val io = IO(new Bundle {
    //  val port = new VCU118AccelIO
    //})

    //MIG black box instantiatu_accel.io.
    val u_accel = Module(new axiaccel)
    val (m, _) = axi_master_node.out(0)
    val (s, _) = axi_slave_node.in(0)

    //global signals
    //aclk                          :=
    //aresetn                       :=
    //slave interface write address
    u_accel.io.s_axi_awid       := s.aw.bits.id
    u_accel.io.s_axi_awaddr     := s.aw.bits.addr
    u_accel.io.s_axi_awlen      := s.aw.bits.len
    u_accel.io.s_axi_awsize     := s.aw.bits.size
    u_accel.io.s_axi_awburst    := s.aw.bits.burst
    u_accel.io.s_axi_awlock     := s.aw.bits.lock
    u_accel.io.s_axi_awcache    := s.aw.bits.cache
    u_accel.io.s_axi_awprot     := s.aw.bits.prot
    u_accel.io.s_axi_awqos      := s.aw.bits.qos
    //u_accel.io.s_axi_awregu_accel.io.   := s.aw.bits.awregu_accel.io.
    //u_accel.io.awuser           := s.aw.bits.user
    u_accel.io.s_axi_awvalid    := s.aw.valid
    s.aw.ready                   := u_accel.io.s_axi_awready
    //slave interface write data ports
    //u_accel.io.s_axi_awid        := s.w.bits.id
    u_accel.io.s_axi_wdata      := s.w.bits.data
    u_accel.io.s_axi_wstrb      := s.w.bits.strb
    u_accel.io.s_axi_wlast      := s.w.bits.last
    //u_accel.io.s_axi_wuser      := s.w.bits.user
    u_accel.io.s_axi_wvalid     := s.w.valid
    s.w.ready                    := u_accel.io.s_axi_wready
    //slave interface write response
    s.b.bits.id                  := u_accel.io.s_axi_bid
    s.b.bits.resp                := u_accel.io.s_axi_bresp
    //s.b.bits.user                := u_accel.io.s_axi_buser
    s.b.valid                    := u_accel.io.s_axi_bvalid
    u_accel.io.s_axi_bready     := s.b.ready
    //slave AXI interface read address ports
    u_accel.io.s_axi_arid       := s.ar.bits.id
    u_accel.io.s_axi_araddr     := s.ar.bits.addr
    u_accel.io.s_axi_arlen      := s.ar.bits.len
    u_accel.io.s_axi_arsize     := s.ar.bits.size
    u_accel.io.s_axi_arburst    := s.ar.bits.burst
    u_accel.io.s_axi_arlock     := s.ar.bits.lock
    u_accel.io.s_axi_arcache    := s.ar.bits.cache
    u_accel.io.s_axi_arprot     := s.ar.bits.prot
    u_accel.io.s_axi_arqos      := s.ar.bits.qos
    //u_accel.io.s_axi_arregu_accel.io.   := s.ar.bits.arregu_accel.io.
    //u_accel.io.s_axi_aruser     := s.ar.bits.user
    u_accel.io.s_axi_arvalid    := s.ar.valid
    s.ar.ready                   := u_accel.io.s_axi_arready
    //slave AXI interface read data ports
    s.r.bits.id                  := u_accel.io.s_axi_rid
    s.r.bits.data                := u_accel.io.s_axi_rdata
    s.r.bits.resp                := u_accel.io.s_axi_rresp
    s.r.bits.last                := u_accel.io.s_axi_rlast
    //s.r.bits.ruser               := u_accel.io.s_axi_ruser
    s.r.valid                    := u_accel.io.s_axi_rvalid
    u_accel.io.s_axi_rready     := s.r.ready

    //m
    //AXI4 signals ordered per AXI4 Specificatu_accel.io. (Release D) Sectu_accel.io. A.2
    //global signals
    //aclk                          :=
    //aresetn                       :=
    //master interface write address
    m.aw.bits.id                 := u_accel.io.m_axi_awid
    m.aw.bits.addr               := u_accel.io.m_axi_awaddr
    m.aw.bits.len                := u_accel.io.m_axi_awlen
    m.aw.bits.size               := u_accel.io.m_axi_awsize
    m.aw.bits.burst              := u_accel.io.m_axi_awburst
    m.aw.bits.lock               := u_accel.io.m_axi_awlock
    m.aw.bits.cache              := u_accel.io.m_axi_awcache
    m.aw.bits.prot               := u_accel.io.m_axi_awprot
    m.aw.bits.qos                := u_accel.io.m_axi_awqos
    //m.aw.bits.regu_accel.io.             := u_accel.io.m_axi_awregu_accel.io.
    //m.aw.bits.user               := u_accel.io.m_axi_awuser
    m.aw.valid                   := u_accel.io.m_axi_awvalid
    u_accel.io.m_axi_awready    := m.aw.ready

    //master interface write data ports
    m.w.bits.data                := u_accel.io.m_axi_wdata
    m.w.bits.strb                := u_accel.io.m_axi_wstrb
    m.w.bits.last                := u_accel.io.m_axi_wlast
    //m.w.bits.user                := u_accel.io.m_axi_wuser
    m.w.valid                    := u_accel.io.m_axi_wvalid
    u_accel.io.m_axi_wready     := m.w.ready

    //master interface write response
    u_accel.io.m_axi_bid        := m.b.bits.id
    u_accel.io.m_axi_bresp      := m.b.bits.resp
    //u_accel.io.m_axi_buser      := m.b.bits.user
    u_accel.io.m_axi_bvalid     := m.b.valid
    m.b.ready                    := u_accel.io.m_axi_bready

    //master AXI interface read address ports
    m.ar.bits.id                 := u_accel.io.m_axi_arid
    m.ar.bits.addr               := u_accel.io.m_axi_araddr
    m.ar.bits.len                := u_accel.io.m_axi_arlen
    m.ar.bits.size               := u_accel.io.m_axi_arsize
    m.ar.bits.burst              := u_accel.io.m_axi_arburst
    m.ar.bits.lock               := u_accel.io.m_axi_arlock
    m.ar.bits.cache              := u_accel.io.m_axi_arcache
    m.ar.bits.prot               := u_accel.io.m_axi_arprot
    m.ar.bits.qos                := u_accel.io.m_axi_arqos
    //m.ar.bits.regu_accel.io.             := u_accel.io.m_axi_arregu_accel.io.
    //m.ar.bits.user               := u_accel.io.s_axi_aruser
    m.ar.valid                   := u_accel.io.m_axi_arvalid
    u_accel.io.m_axi_arready    := m.ar.ready

    //master AXI interface read data ports
    u_accel.io.m_axi_rid        := m.r.bits.id
    u_accel.io.m_axi_rdata      := m.r.bits.data
    u_accel.io.m_axi_rresp      := m.r.bits.resp
    u_accel.io.m_axi_rlast      := m.r.bits.last
    //u_accel.io.s_axi_ruser      := s.bits.ruser
    u_accel.io.m_axi_rvalid     := m.r.valid
    m.r.ready                    := u_accel.io.m_axi_rready}
}

