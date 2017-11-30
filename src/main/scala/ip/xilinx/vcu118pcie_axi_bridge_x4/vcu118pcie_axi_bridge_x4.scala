// See LICENSE for license details.
package sifive.fpgashells.ip.xilinx.vcu118pcie_axi_bridge_x4

import Chisel._
import chisel3.{Input,Output}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.util.{ElaborationArtefacts}

sealed trait PCIeConnector
case object EDGE extends PCIeConnector   // PCIe Edge Connector
case object FMCP extends PCIeConnector   // FMC +
  

// Device : XCVU9P-FLGA2104-2L-e
// IP VLNV: xilinx.com:ip:xdma:4.0

// Black Box
// Signals named _exactly_ as per Vivado generated verilog
// s : -{lock, cache, prot, qos}

trait VCU118PCIeAXIBridgeX4IOSerial extends Bundle {
  //serial external pins
  val pci_exp_txp           = Output(UInt(4.W))
  val pci_exp_txn           = Output(UInt(4.W))
  val pci_exp_rxp           = Input(UInt(4.W))
  val pci_exp_rxn           = Input(UInt(4.W))
}

trait VCU118PCIeAXIBridgeX4IOClocksReset extends Bundle {
  //clock, reset, control
  val sys_rst_n             = Input(Bool())

  val axi_aclk              = Output(Clock())
  val axi_aresetn           = Output(Bool())
  val axi_ctl_aresetn       = Output(Bool())
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class vcu118pcie_axi_bridge_x4() extends BlackBox
{
  val io = new Bundle with VCU118PCIeAXIBridgeX4IOSerial
                      with VCU118PCIeAXIBridgeX4IOClocksReset {

    val cfg_ltssm_state       = Output(UInt(6.W))
    val user_lnk_up           = Output(Bool())

    val sys_clk_gt            = Input(Bool())   //REFCLK
    val sys_clk               = Input(Bool())   //DRP Clock. Same frequency as sys_clk_gt if <250Mhz

    //interrupt
    val interrupt_out         = Bool(OUTPUT)

    //axi slave
    //-{lock, cache, prot, qos}
    //slave interface write address
    val s_axib_awid            = Bits(INPUT,4)
    val s_axib_awaddr          = Bits(INPUT,32)
    val s_axib_awregion        = Bits(INPUT,4)
    val s_axib_awlen           = Bits(INPUT,8)
    val s_axib_awsize          = Bits(INPUT,3)
    val s_axib_awburst         = Bits(INPUT,2)
    //val s_axib_awlock        = Bool(INPUT)
    //val s_axib_awcache       = Bits(INPUT,4)
    //val s_axib_awprot        = Bits(INPUT,3)
    //val s_axib_awqos         = Bits(INPUT,4)
    val s_axib_awvalid         = Bool(INPUT)
    val s_axib_awready         = Bool(OUTPUT)
    //slave interface write data
    val s_axib_wdata           = Bits(INPUT,64)
    val s_axib_wstrb           = Bits(INPUT,8)
    val s_axib_wlast           = Bool(INPUT)
    val s_axib_wvalid          = Bool(INPUT)
    val s_axib_wready          = Bool(OUTPUT)
    //slave interface write response
    val s_axib_bready          = Bool(INPUT)
    val s_axib_bid             = Bits(OUTPUT,4)
    val s_axib_bresp           = Bits(OUTPUT,2)
    val s_axib_bvalid          = Bool(OUTPUT)
    //slave interface read address
    val s_axib_arid            = Bits(INPUT,4)
    val s_axib_araddr          = Bits(INPUT,32)
    val s_axib_arregion        = Bits(INPUT,4)
    val s_axib_arlen           = Bits(INPUT,8)
    val s_axib_arsize          = Bits(INPUT,3)
    val s_axib_arburst         = Bits(INPUT,2)
    //val s_axib_arlock        = Bits(INPUT,1)
    //val s_axib_arcache       = Bits(INPUT,4)
    //val s_axib_arprot        = Bits(INPUT,3)
    //val s_axib_arqos         = Bits(INPUT,4)
    val s_axib_arvalid         = Bool(INPUT)
    val s_axib_arready         = Bool(OUTPUT)
    //slave interface read data
    val s_axib_rready          = Bool(INPUT)
    val s_axib_rid             = Bits(OUTPUT,4)
    val s_axib_rdata           = Bits(OUTPUT,64)
    val s_axib_rresp           = Bits(OUTPUT,2)
    val s_axib_rlast           = Bool(OUTPUT)
    val s_axib_rvalid          = Bool(OUTPUT)

    //axi master
    //-{id,region,qos}
    //slave interface write address ports
    //val m_axib_awid          = Bits(OUTPUT,4)
    val m_axib_awaddr          = Bits(OUTPUT,32)
    //val m_axib_awregion      = Bits(OUTPUT,4)
    val m_axib_awlen           = Bits(OUTPUT,8)
    val m_axib_awsize          = Bits(OUTPUT,3)
    val m_axib_awburst         = Bits(OUTPUT,2)
    val m_axib_awlock          = Bool(OUTPUT)
    val m_axib_awcache         = Bits(OUTPUT,4)
    val m_axib_awprot          = Bits(OUTPUT,3)
    //val m_axib_awqos         = Bits(OUTPUT,4)
    val m_axib_awvalid         = Bool(OUTPUT)
    val m_axib_awready         = Bool(INPUT)
    //slave interface write data ports
    val m_axib_wdata           = Bits(OUTPUT,64)
    val m_axib_wstrb           = Bits(OUTPUT,8)
    val m_axib_wlast           = Bool(OUTPUT)
    val m_axib_wvalid          = Bool(OUTPUT)
    val m_axib_wready          = Bool(INPUT)
    //slave interface write response ports
    val m_axib_bready          = Bool(OUTPUT)
    //val m_axib_bid           = Bits(INPUT,4)
    val m_axib_bresp           = Bits(INPUT,2)
    val m_axib_bvalid          = Bool(INPUT)
    //slave interface read address ports
    //val m_axib_arid          = Bits(OUTPUT,4)
    val m_axib_araddr          = Bits(OUTPUT,32)
    //val m_axib_arregion      = Bits(OUTPUT,4)
    val m_axib_arlen           = Bits(OUTPUT,8)
    val m_axib_arsize          = Bits(OUTPUT,3)
    val m_axib_arburst         = Bits(OUTPUT,2)
    val m_axib_arlock          = Bits(OUTPUT,1)
    val m_axib_arcache         = Bits(OUTPUT,4)
    val m_axib_arprot          = Bits(OUTPUT,3)
    //val m_axib_arqos         = Bits(OUTPUT,4)
    val m_axib_arvalid         = Bool(OUTPUT)
    val m_axib_arready         = Bool(INPUT)
    //slave interface read data ports
    val m_axib_rready          = Bool(OUTPUT)
    //val m_axib_rid           = Bits(INPUT,4)
    val m_axib_rdata           = Bits(INPUT,64)
    val m_axib_rresp           = Bits(INPUT,2)
    val m_axib_rlast           = Bool(INPUT)
    val m_axib_rvalid          = Bool(INPUT)

    //axi lite slave for control
    val s_axil_awaddr      = Bits(INPUT,32)
    val s_axil_awvalid     = Bool(INPUT)
    val s_axil_awready     = Bool(OUTPUT)
    val s_axil_wdata       = Bits(INPUT,32)
    val s_axil_wstrb       = Bits(INPUT,4)
    val s_axil_wvalid      = Bool(INPUT)
    val s_axil_wready      = Bool(OUTPUT)
    val s_axil_bresp       = Bits(OUTPUT,2)
    val s_axil_bvalid      = Bool(OUTPUT)
    val s_axil_bready      = Bool(INPUT)
    val s_axil_araddr      = Bits(INPUT,32)
    val s_axil_arvalid     = Bool(INPUT)
    val s_axil_arready     = Bool(OUTPUT)
    val s_axil_rdata       = Bits(OUTPUT,32)
    val s_axil_rresp       = Bits(OUTPUT,2)
    val s_axil_rvalid      = Bool(OUTPUT)
    val s_axil_rready      = Bool(INPUT)
 }
}
//scalastyle:off

//wrap vc707_axi_to_pcie_x1 black box in Nasti Bundles

class VCU118PCIeAXIBridgeX4(pcieConnector : PCIeConnector)(implicit p:Parameters) extends LazyModule
{
  val device = new SimpleDevice("pci", Seq("xlnx,axi-pcie-host-1.00.a")) {
    override def describe(resources: ResourceBindings): Description = {
      val Description(name, mapping) = super.describe(resources)
      val intc = "pcie_intc"
      def ofInt(x: Int) = Seq(ResourceInt(BigInt(x)))
      def ofMap(x: Int) = Seq(0, 0, 0, x).flatMap(ofInt) ++ Seq(ResourceReference(intc)) ++ ofInt(x)
      val extra = Map(
        "#address-cells"     -> ofInt(3),
        "#size-cells"        -> ofInt(2),
        "#interrupt-cells"   -> ofInt(1),
        "device_type"        -> Seq(ResourceString("pci")),
        "interrupt-map-mask" -> Seq(0, 0, 0, 7).flatMap(ofInt),
        "interrupt-map"      -> Seq(1, 2, 3, 4).flatMap(ofMap),
        "ranges"             -> resources("ranges").map { case Binding(_, ResourceAddress(address, perms)) =>
                                                               ResourceMapping(address, BigInt(0x02000000) << 64, perms) },
        "interrupt-controller" -> Seq(ResourceMap(labels = Seq(intc), value = Map(
          "interrupt-controller" -> Nil,
          "#address-cells"       -> ofInt(0),
          "#interrupt-cells"     -> ofInt(1)))))
      Description(name, mapping ++ extra)
    }
  }

  val slave = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = List(AddressSet(0x60000000L, 0x1fffffffL)),
      resources     = Seq(Resource(device, "ranges")),
      executable    = true,
      supportsWrite = TransferSizes(1, 256),
      supportsRead  = TransferSizes(1, 256))),
    beatBytes = 8)))

  val control = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = List(AddressSet(0x50000000L, 0x03ffffffL)),
      resources     = device.reg("control"),
      supportsWrite = TransferSizes(1, 4),
      supportsRead  = TransferSizes(1, 4),
      interleavedId = Some(0))), // AXI4-Lite never interleaves responses
    beatBytes = 4)))

  val master = AXI4MasterNode(Seq(AXI4MasterPortParameters(
    masters = Seq(AXI4MasterParameters(
      name    = "VCU118 PCIe",
      id      = IdRange(0, 1),
      aligned = false)))))

  val intnode = IntSourceNode(IntSourcePortSimple(resources = device.int))

  lazy val module = new LazyModuleImp(this) {
    // The master on the control port must be AXI-lite
    require (control.edges.in(0).master.endId == 1)
    // Must have exactly the right number of idBits
    require (slave.edges.in(0).bundle.idBits == 4)

    class VCU118PCIeAXIBridgeX4IOBundle extends Bundle with VCU118PCIeAXIBridgeX4IOSerial
                                                  with VCU118PCIeAXIBridgeX4IOClocksReset;

    val io = IO(new Bundle {
      val port = new VCU118PCIeAXIBridgeX4IOBundle
      val REFCLK = Bool(INPUT)
    })

    val blackbox = Module(new vcu118pcie_axi_bridge_x4)

    val (s, _) = slave.in(0)
    val (c, _) = control.in(0)
    val (m, _) = master.out(0)
    val (i, _) = intnode.out(0)

    //to top level
    //VCU118PCIeAXIBridgeX4IOClocksReset
    blackbox.io.sys_rst_n           := io.port.sys_rst_n
    io.port.axi_aclk                := blackbox.io.axi_aclk
    io.port.axi_aresetn             := blackbox.io.axi_aresetn
    io.port.axi_ctl_aresetn         := blackbox.io.axi_ctl_aresetn
    //VCU118PCIeAXIBridgeX4IOSerial
    io.port.pci_exp_txp             := blackbox.io.pci_exp_txp
    io.port.pci_exp_txn             := blackbox.io.pci_exp_txn
    blackbox.io.pci_exp_rxp         := io.port.pci_exp_rxp
    blackbox.io.pci_exp_rxn         := io.port.pci_exp_rxn

    //unused
    // := blackbox.io.cfg_ltssm_state
    // := user_lnk_up

    //REFCLK
    blackbox.io.sys_clk_gt          := io.REFCLK
    blackbox.io.sys_clk             := io.REFCLK

    //i
    i(0)                            := blackbox.io.interrupt_out

    //s
    //AXI4 signals ordered as per AXI4 Specification (Release D) Section A.2
    //-{lock, cache, prot, qos}
    //-{aclk, aresetn, awuser, wid, wuser, buser, ruser}
    //global signals
    //aclk                          :=
    //aresetn                       :=
    //slave interface write address
    blackbox.io.s_axib_awid         := s.aw.bits.id
    blackbox.io.s_axib_awaddr       := s.aw.bits.addr
    blackbox.io.s_axib_awlen        := s.aw.bits.len
    blackbox.io.s_axib_awsize       := s.aw.bits.size
    blackbox.io.s_axib_awburst      := s.aw.bits.burst
    //blackbox.io.s_axib_awlock     := s.aw.bits.lock
    //blackbox.io.s_axib_awcache    := s.aw.bits.cache
    //blackbox.io.s_axib_awprot     := s.aw.bits.prot
    //blackbox.io.s_axib_awqos      := s.aw.bits.qos
    blackbox.io.s_axib_awregion     := UInt(0)
    //blackbox.io.awuser            := s.aw.bits.user
    blackbox.io.s_axib_awvalid      := s.aw.valid
    s.aw.ready                      := blackbox.io.s_axib_awready
    //slave interface write data ports
    //blackbox.io.s_axib_wid        := s.w.bits.id
    blackbox.io.s_axib_wdata        := s.w.bits.data
    blackbox.io.s_axib_wstrb        := s.w.bits.strb
    blackbox.io.s_axib_wlast        := s.w.bits.last
    //blackbox.io.s_axib_wuser      := s.w.bits.user
    blackbox.io.s_axib_wvalid       := s.w.valid
    s.w.ready                       := blackbox.io.s_axib_wready
    //slave interface write response
    s.b.bits.id                     := blackbox.io.s_axib_bid
    s.b.bits.resp                   := blackbox.io.s_axib_bresp
    //s.b.bits.user                 := blackbox.io.s_axib_buser
    s.b.valid                       := blackbox.io.s_axib_bvalid
    blackbox.io.s_axib_bready       := s.b.ready
    //slave AXI interface read address ports
    blackbox.io.s_axib_arid         := s.ar.bits.id
    blackbox.io.s_axib_araddr       := s.ar.bits.addr
    blackbox.io.s_axib_arlen        := s.ar.bits.len
    blackbox.io.s_axib_arsize       := s.ar.bits.size
    blackbox.io.s_axib_arburst      := s.ar.bits.burst
    //blackbox.io.s_axib_arlock     := s.ar.bits.lock
    //blackbox.io.s_axib_arcache    := s.ar.bits.cache
    //blackbox.io.s_axib_arprot     := s.ar.bits.prot
    //blackbox.io.s_axib_arqos      := s.ar.bits.qos
    blackbox.io.s_axib_arregion     := UInt(0)
    //blackbox.io.s_axib_aruser     := s.ar.bits.user
    blackbox.io.s_axib_arvalid      := s.ar.valid
    s.ar.ready                      := blackbox.io.s_axib_arready
    //slave AXI interface read data ports
    s.r.bits.id                     := blackbox.io.s_axib_rid
    s.r.bits.data                   := blackbox.io.s_axib_rdata
    s.r.bits.resp                   := blackbox.io.s_axib_rresp
    s.r.bits.last                   := blackbox.io.s_axib_rlast
    //s.r.bits.ruser                := blackbox.io.s_axib_ruser
    s.r.valid                       := blackbox.io.s_axib_rvalid
    blackbox.io.s_axib_rready       := s.r.ready

    //ctl
    //axi-lite slave interface write address
    blackbox.io.s_axil_awaddr       := c.aw.bits.addr
    blackbox.io.s_axil_awvalid      := c.aw.valid
    c.aw.ready                      := blackbox.io.s_axil_awready
    //axi-lite slave interface write data ports
    blackbox.io.s_axil_wdata        := c.w.bits.data
    blackbox.io.s_axil_wstrb        := c.w.bits.strb
    blackbox.io.s_axil_wvalid       := c.w.valid
    c.w.ready                       := blackbox.io.s_axil_wready
    //axi-lite slave interface write response
    blackbox.io.s_axil_bready       := c.b.ready
    c.b.bits.id                     := UInt(0)
    c.b.bits.resp                   := blackbox.io.s_axil_bresp
    c.b.valid                       := blackbox.io.s_axil_bvalid
    //axi-lite slave AXI interface read address ports
    blackbox.io.s_axil_araddr       := c.ar.bits.addr
    blackbox.io.s_axil_arvalid      := c.ar.valid
    c.ar.ready                      := blackbox.io.s_axil_arready
    //slave AXI interface read data ports
    blackbox.io.s_axil_rready       := c.r.ready
    c.r.bits.id                     := UInt(0)
    c.r.bits.data                   := blackbox.io.s_axil_rdata
    c.r.bits.resp                   := blackbox.io.s_axil_rresp
    c.r.bits.last                   := Bool(true)
    c.r.valid                       := blackbox.io.s_axil_rvalid

    //m
    //AXI4 signals ordered per AXI4 Specification (Release D) Section A.2
    //-{id,region,qos}
    //-{aclk, aresetn, awuser, wid, wuser, buser, ruser}
    //global signals
    //aclk                          :=
    //aresetn                       :=
    //master interface write address
    m.aw.bits.id                    := UInt(0)
    m.aw.bits.addr                  := blackbox.io.m_axib_awaddr
    m.aw.bits.len                   := blackbox.io.m_axib_awlen
    m.aw.bits.size                  := blackbox.io.m_axib_awsize
    m.aw.bits.burst                 := blackbox.io.m_axib_awburst
    m.aw.bits.lock                  := blackbox.io.m_axib_awlock
    m.aw.bits.cache                 := blackbox.io.m_axib_awcache
    m.aw.bits.prot                  := blackbox.io.m_axib_awprot
    m.aw.bits.qos                   := UInt(0)
    //m.aw.bits.region              := blackbox.io.m_axib_awregion
    //m.aw.bits.user                := blackbox.io.m_axib_awuser
    m.aw.valid                      := blackbox.io.m_axib_awvalid
    blackbox.io.m_axib_awready      := m.aw.ready

    //master interface write data ports
    m.w.bits.data                   := blackbox.io.m_axib_wdata
    m.w.bits.strb                   := blackbox.io.m_axib_wstrb
    m.w.bits.last                   := blackbox.io.m_axib_wlast
    //m.w.bits.user                 := blackbox.io.m_axib_wuser
    m.w.valid                       := blackbox.io.m_axib_wvalid
    blackbox.io.m_axib_wready       := m.w.ready

    //master interface write response
    //blackbox.io.m_axib_bid        := m.b.bits.id
    blackbox.io.m_axib_bresp        := m.b.bits.resp
    //blackbox.io.m_axib_buser      := m.b.bits.user
    blackbox.io.m_axib_bvalid       := m.b.valid
    m.b.ready                       := blackbox.io.m_axib_bready

    //master AXI interface read address ports
    m.ar.bits.id                    := UInt(0)
    m.ar.bits.addr                  := blackbox.io.m_axib_araddr
    m.ar.bits.len                   := blackbox.io.m_axib_arlen
    m.ar.bits.size                  := blackbox.io.m_axib_arsize
    m.ar.bits.burst                 := blackbox.io.m_axib_arburst
    m.ar.bits.lock                  := blackbox.io.m_axib_arlock
    m.ar.bits.cache                 := blackbox.io.m_axib_arcache
    m.ar.bits.prot                  := blackbox.io.m_axib_arprot
    m.ar.bits.qos                   := UInt(0)
    //m.ar.bits.region              := blackbox.io.m_axib_arregion
    //m.ar.bits.user                := blackbox.io.s_axib_aruser
    m.ar.valid                      := blackbox.io.m_axib_arvalid
    blackbox.io.m_axib_arready      := m.ar.ready

    //master AXI interface read data ports
    //blackbox.io.m_axib_rid        := m.r.bits.id
    blackbox.io.m_axib_rdata        := m.r.bits.data
    blackbox.io.m_axib_rresp        := m.r.bits.resp
    blackbox.io.m_axib_rlast        := m.r.bits.last
    //blackbox.io.s_axib_ruser      := s.bits.ruser
    blackbox.io.m_axib_rvalid       := m.r.valid
    m.r.ready                       := blackbox.io.m_axib_rready
  }

  val (pcie_blk_locn,select_quad) = pcieConnector match {
    case EDGE => ("""X1Y2""","""GTY_Quad_227""") 
    case FMCP => ("""X0Y3""","""GTY_Quad_126""")
  }

/*
  val xdc_package_pins = pcieConnector match {
    case EDGE =>
      """
      # PCI Express
      # Edge connector
      set_property PACKAGE_PIN P42 [get_ports {pcie_pci_exp_txp[0]}]
      set_property PACKAGE_PIN P43 [get_ports {pcie_pci_exp_txn[0]}]
      set_property PACKAGE_PIN U45 [get_ports {pcie_pci_exp_rxp[0]}]
      set_property PACKAGE_PIN P46 [get_ports {pcie_pci_exp_rxn[0]}]

      set_property PACKAGE_PIN M42 [get_ports {pcie_pci_exp_txp[1]}]
      set_property PACKAGE_PIN M43 [get_ports {pcie_pci_exp_txn[1]}]
      set_property PACKAGE_PIN R45 [get_ports {pcie_pci_exp_rxp[1]}]
      set_property PACKAGE_PIN R46 [get_ports {pcie_pci_exp_rxn[1]}]

      set_property PACKAGE_PIN T42 [get_ports {pcie_pci_exp_txp[2]}]
      set_property PACKAGE_PIN T43 [get_ports {pcie_pci_exp_txn[2]}]
      set_property PACKAGE_PIN W45 [get_ports {pcie_pci_exp_rxp[2]}]
      set_property PACKAGE_PIN W46 [get_ports {pcie_pci_exp_rxn[2]}]

      set_property PACKAGE_PIN K42 [get_ports {pcie_pci_exp_txp[3]}]
      set_property PACKAGE_PIN K43 [get_ports {pcie_pci_exp_txn[3]}]
      set_property PACKAGE_PIN N45 [get_ports {pcie_pci_exp_rxp[3]}]
      set_property PACKAGE_PIN N46 [get_ports {pcie_pci_exp_rxn[3]}]

      #refclk
      set_property PACKAGE_PIN V38 [get_ports {pcie_REFCLK_rxp}]
      set_property PACKAGE_PIN V39 [get_ports {pcie_REFCLK_rxn}]
      create_clock -name pcie_ref_clk -period 10 [get_ports pcie_REFCLK_rxp]
      set_input_jitter [get_clocks -of_objects [get_ports pcie_REFCLK_rxp]] 0.5
      """
    case FMCP =>
      """
      # PCI Express
      # J22 FMCP_HSPC (FMC + HSPC)
      set_property PACKAGE_PIN P42 [get_ports {pcie_pci_exp_txp[0]}]
      set_property PACKAGE_PIN P43 [get_ports {pcie_pci_exp_txn[0]}]
      set_property PACKAGE_PIN U45 [get_ports {pcie_pci_exp_rxp[0]}]
      set_property PACKAGE_PIN P46 [get_ports {pcie_pci_exp_rxn[0]}]

      set_property PACKAGE_PIN M42 [get_ports {pcie_pci_exp_txp[1]}]
      set_property PACKAGE_PIN M43 [get_ports {pcie_pci_exp_txn[1]}]
      set_property PACKAGE_PIN R45 [get_ports {pcie_pci_exp_rxp[1]}]
      set_property PACKAGE_PIN R46 [get_ports {pcie_pci_exp_rxn[1]}]

      set_property PACKAGE_PIN T42 [get_ports {pcie_pci_exp_txp[2]}]
      set_property PACKAGE_PIN T43 [get_ports {pcie_pci_exp_txn[2]}]
      set_property PACKAGE_PIN W45 [get_ports {pcie_pci_exp_rxp[2]}]
      set_property PACKAGE_PIN W46 [get_ports {pcie_pci_exp_rxn[2]}]

      set_property PACKAGE_PIN K42 [get_ports {pcie_pci_exp_txp[3]}]
      set_property PACKAGE_PIN K43 [get_ports {pcie_pci_exp_txn[3]}]
      set_property PACKAGE_PIN N45 [get_ports {pcie_pci_exp_rxp[3]}]
      set_property PACKAGE_PIN N46 [get_ports {pcie_pci_exp_rxn[3]}]

      #refclk
      set_property PACKAGE_PIN V38 [get_ports {pcie_REFCLK_rxp}]
      set_property PACKAGE_PIN V39 [get_ports {pcie_REFCLK_rxn}]
      create_clock -name pcie_ref_clk -period 10 [get_ports pcie_REFCLK_rxp]
      set_input_jitter [get_clocks -of_objects [get_ports pcie_REFCLK_rxp]] 0.5
      """
  }
*/

  ElaborationArtefacts.add(
    "vcu118pcie_axi_bridge_x4.vivado.tcl",
    """
       #set board_part [get_property BOARD_PART [current_project] ]
       #set_property BOARD_PART {} [current_project] 
       create_ip -name xdma -vendor xilinx.com -library ip -version 4.0 -module_name vcu118pcie_axi_bridge_x4 -dir $ipdir -force
       set_property -dict [list CONFIG.Component_Name {vcu118pcie_axi_bridge_x4}  \
                                CONFIG.functional_mode {AXI_Bridge} \
                                CONFIG.mode_selection {Advanced} \
                                CONFIG.device_port_type {Root_Port_of_PCI_Express_Root_Complex} \
                                CONFIG.pcie_blk_locn {""" + pcie_blk_locn + """} \
                                CONFIG.pl_link_cap_max_link_width {X4} \
                                CONFIG.pl_link_cap_max_link_speed {5.0_GT/s} \
                                CONFIG.axi_addr_width {32} CONFIG.axisten_freq {250} \
                                CONFIG.pf0_device_id {9124} \
                                CONFIG.pf0_base_class_menu {Bridge_device} \
                                CONFIG.pf0_class_code_base {06}  \
                                CONFIG.pf0_sub_class_interface_menu {CardBus_bridge} \
                                CONFIG.pf0_class_code_sub {07} \
                                CONFIG.pf0_class_code_interface {00} \
                                CONFIG.pf0_class_code {060700} \
                                CONFIG.xdma_axilite_slave {true} \
                                CONFIG.en_gt_selection {true} \
                                CONFIG.select_quad {""" + select_quad + """}  \
                                CONFIG.plltype {QPLL1} \
                                CONFIG.type1_membase_memlimit_enable {Enabled} \
                                CONFIG.type1_prefetchable_membase_memlimit {64bit_Enabled} \
                                CONFIG.BASEADDR {0x00000000}  \
                                CONFIG.HIGHADDR {0x001FFFFF} \
                                CONFIG.pf1_class_code {060700} \
                                CONFIG.pf1_base_class_menu {Bridge_device} \
                                CONFIG.pf1_class_code_base {06} \
                                CONFIG.pf1_class_code_sub {07}  \
                                CONFIG.pf1_sub_class_interface_menu {CardBus_bridge} \
                                CONFIG.pf1_class_code_interface {00} \
                                CONFIG.pf0_bar0_type_mqdma {Memory} \
                                CONFIG.pf1_bar0_type_mqdma {Memory} \
                                CONFIG.pf2_bar0_type_mqdma {Memory} \
                                CONFIG.pf3_bar0_type_mqdma {Memory} \
                                CONFIG.pf0_sriov_bar0_type {Memory} \
                                CONFIG.pf1_sriov_bar0_type {Memory} \
                                CONFIG.pf2_sriov_bar0_type {Memory} \
                                CONFIG.pf3_sriov_bar0_type {Memory} \
                                CONFIG.PF0_DEVICE_ID_mqdma {9124} \
                                CONFIG.PF2_DEVICE_ID_mqdma {9124} \
                                CONFIG.PF3_DEVICE_ID_mqdma {9124} \
                                CONFIG.pf0_base_class_menu_mqdma {Bridge_device} \
                                CONFIG.pf0_class_code_base_mqdma {06} \
                                CONFIG.pf0_class_code_mqdma {068000} \
                                CONFIG.pf1_base_class_menu_mqdma {Bridge_device} \
                                CONFIG.pf1_class_code_base_mqdma {06} \
                                CONFIG.pf1_class_code_mqdma {068000} \
                                CONFIG.pf2_base_class_menu_mqdma {Bridge_device} \
                                CONFIG.pf2_class_code_base_mqdma {06} \
                                CONFIG.pf2_class_code_mqdma {068000} \
                                CONFIG.pf3_base_class_menu_mqdma {Bridge_device} \
                                CONFIG.pf3_class_code_base_mqdma {06} \
                                CONFIG.pf3_class_code_mqdma {068000}] [get_ips vcu118pcie_axi_bridge_x4]
      #set_property BOARD_PART $board_part [current_project]
      """ 
  )
}
