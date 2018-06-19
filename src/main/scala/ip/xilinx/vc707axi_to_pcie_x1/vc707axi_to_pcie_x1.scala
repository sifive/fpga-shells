// See LICENSE for license details.
package sifive.fpgashells.ip.xilinx.vc707axi_to_pcie_x1

import Chisel._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.util.{ElaborationArtefacts}

// IP VLNV: xilinx.com:customize_ip:vc707pcietoaxi:1.0
// Black Box
// Signals named _exactly_ as per Vivado generated verilog
// s : -{lock, cache, prot, qos}

trait VC707AXIToPCIeX1IOSerial extends Bundle {
  //serial external pins
  val pci_exp_txp           = Bool(OUTPUT)
  val pci_exp_txn           = Bool(OUTPUT)
  val pci_exp_rxp           = Bool(INPUT)
  val pci_exp_rxn           = Bool(INPUT)
}

trait VC707AXIToPCIeX1IOClocksReset extends Bundle {
  //clock, reset, control
  val axi_aresetn           = Bool(INPUT)
  val axi_aclk_out          = Clock(OUTPUT)
  val axi_ctl_aclk_out      = Clock(OUTPUT)
  val mmcm_lock             = Bool(OUTPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class vc707axi_to_pcie_x1() extends BlackBox
{
  val io = new Bundle with VC707AXIToPCIeX1IOSerial
                      with VC707AXIToPCIeX1IOClocksReset {
    //refclk
    val REFCLK                = Bool(INPUT)

    //clock, reset, control
    val INTX_MSI_Request      = Bool(INPUT)
    val INTX_MSI_Grant        = Bool(OUTPUT)
    val MSI_enable            = Bool(OUTPUT)
    val MSI_Vector_Num        = Bits(INPUT,5)
    val MSI_Vector_Width      = Bits(OUTPUT,3)

    //interrupt
    val interrupt_out         = Bool(OUTPUT)

    //axi slave
    //-{lock, cache, prot, qos}
    //slave interface write address
    val s_axi_awid            = Bits(INPUT,4)
    val s_axi_awaddr          = Bits(INPUT,32)
    val s_axi_awregion        = Bits(INPUT,4)
    val s_axi_awlen           = Bits(INPUT,8)
    val s_axi_awsize          = Bits(INPUT,3)
    val s_axi_awburst         = Bits(INPUT,2)
    //val s_axi_awlock        = Bool(INPUT)
    //val s_axi_awcache       = Bits(INPUT,4)
    //val s_axi_awprot        = Bits(INPUT,3)
    //val s_axi_awqos         = Bits(INPUT,4)
    val s_axi_awvalid         = Bool(INPUT)
    val s_axi_awready         = Bool(OUTPUT)
    //slave interface write data
    val s_axi_wdata           = Bits(INPUT,64)
    val s_axi_wstrb           = Bits(INPUT,8)
    val s_axi_wlast           = Bool(INPUT)
    val s_axi_wvalid          = Bool(INPUT)
    val s_axi_wready          = Bool(OUTPUT)
    //slave interface write response
    val s_axi_bready          = Bool(INPUT)
    val s_axi_bid             = Bits(OUTPUT,4)
    val s_axi_bresp           = Bits(OUTPUT,2)
    val s_axi_bvalid          = Bool(OUTPUT)
    //slave interface read address
    val s_axi_arid            = Bits(INPUT,4)
    val s_axi_araddr          = Bits(INPUT,32)
    val s_axi_arregion        = Bits(INPUT,4)
    val s_axi_arlen           = Bits(INPUT,8)
    val s_axi_arsize          = Bits(INPUT,3)
    val s_axi_arburst         = Bits(INPUT,2)
    //val s_axi_arlock        = Bits(INPUT,1)
    //val s_axi_arcache       = Bits(INPUT,4)
    //val s_axi_arprot        = Bits(INPUT,3)
    //val s_axi_arqos         = Bits(INPUT,4)
    val s_axi_arvalid         = Bool(INPUT)
    val s_axi_arready         = Bool(OUTPUT)
    //slave interface read data
    val s_axi_rready          = Bool(INPUT)
    val s_axi_rid             = Bits(OUTPUT,4)
    val s_axi_rdata           = Bits(OUTPUT,64)
    val s_axi_rresp           = Bits(OUTPUT,2)
    val s_axi_rlast           = Bool(OUTPUT)
    val s_axi_rvalid          = Bool(OUTPUT)

    //axi master
    //-{id,region,qos}
    //slave interface write address ports
    //val m_axi_awid          = Bits(OUTPUT,4)
    val m_axi_awaddr          = Bits(OUTPUT,32)
    //val m_axi_awregion      = Bits(OUTPUT,4)
    val m_axi_awlen           = Bits(OUTPUT,8)
    val m_axi_awsize          = Bits(OUTPUT,3)
    val m_axi_awburst         = Bits(OUTPUT,2)
    val m_axi_awlock          = Bool(OUTPUT)
    val m_axi_awcache         = Bits(OUTPUT,4)
    val m_axi_awprot          = Bits(OUTPUT,3)
    //val m_axi_awqos         = Bits(OUTPUT,4)
    val m_axi_awvalid         = Bool(OUTPUT)
    val m_axi_awready         = Bool(INPUT)
    //slave interface write data ports
    val m_axi_wdata           = Bits(OUTPUT,64)
    val m_axi_wstrb           = Bits(OUTPUT,8)
    val m_axi_wlast           = Bool(OUTPUT)
    val m_axi_wvalid          = Bool(OUTPUT)
    val m_axi_wready          = Bool(INPUT)
    //slave interface write response ports
    val m_axi_bready          = Bool(OUTPUT)
    //val m_axi_bid           = Bits(INPUT,4)
    val m_axi_bresp           = Bits(INPUT,2)
    val m_axi_bvalid          = Bool(INPUT)
    //slave interface read address ports
    //val m_axi_arid          = Bits(OUTPUT,4)
    val m_axi_araddr          = Bits(OUTPUT,32)
    //val m_axi_arregion      = Bits(OUTPUT,4)
    val m_axi_arlen           = Bits(OUTPUT,8)
    val m_axi_arsize          = Bits(OUTPUT,3)
    val m_axi_arburst         = Bits(OUTPUT,2)
    val m_axi_arlock          = Bits(OUTPUT,1)
    val m_axi_arcache         = Bits(OUTPUT,4)
    val m_axi_arprot          = Bits(OUTPUT,3)
    //val m_axi_arqos         = Bits(OUTPUT,4)
    val m_axi_arvalid         = Bool(OUTPUT)
    val m_axi_arready         = Bool(INPUT)
    //slave interface read data ports
    val m_axi_rready          = Bool(OUTPUT)
    //val m_axi_rid           = Bits(INPUT,4)
    val m_axi_rdata           = Bits(INPUT,64)
    val m_axi_rresp           = Bits(INPUT,2)
    val m_axi_rlast           = Bool(INPUT)
    val m_axi_rvalid          = Bool(INPUT)

    //axi lite slave for control
    val s_axi_ctl_awaddr      = Bits(INPUT,32)
    val s_axi_ctl_awvalid     = Bool(INPUT)
    val s_axi_ctl_awready     = Bool(OUTPUT)
    val s_axi_ctl_wdata       = Bits(INPUT,32)
    val s_axi_ctl_wstrb       = Bits(INPUT,4)
    val s_axi_ctl_wvalid      = Bool(INPUT)
    val s_axi_ctl_wready      = Bool(OUTPUT)
    val s_axi_ctl_bresp       = Bits(OUTPUT,2)
    val s_axi_ctl_bvalid      = Bool(OUTPUT)
    val s_axi_ctl_bready      = Bool(INPUT)
    val s_axi_ctl_araddr      = Bits(INPUT,32)
    val s_axi_ctl_arvalid     = Bool(INPUT)
    val s_axi_ctl_arready     = Bool(OUTPUT)
    val s_axi_ctl_rdata       = Bits(OUTPUT,32)
    val s_axi_ctl_rresp       = Bits(OUTPUT,2)
    val s_axi_ctl_rvalid      = Bool(OUTPUT)
    val s_axi_ctl_rready      = Bool(INPUT)
 }
}
//scalastyle:off

//wrap vc707_axi_to_pcie_x1 black box in Nasti Bundles

class VC707AXIToPCIeX1(implicit p:Parameters) extends LazyModule
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
        "ranges"             -> resources("ranges").map(x =>
                                  (x: @unchecked) match { case Binding(_, ResourceAddress(address, perms)) =>
                                                               ResourceMapping(address, BigInt(0x02000000) << 64, perms) }),
        "interrupt-controller" -> Seq(ResourceMap(labels = Seq(intc), value = Map(
          "interrupt-controller" -> Nil,
          "#address-cells"       -> ofInt(0),
          "#interrupt-cells"     -> ofInt(1)))))
      Description(name, mapping ++ extra)
    }
  }

  val slave = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = List(AddressSet(0x40000000L, 0x1fffffffL)),
      resources     = Seq(Resource(device, "ranges")),
      executable    = true,
      supportsWrite = TransferSizes(1, 128),
      supportsRead  = TransferSizes(1, 128))),
    beatBytes = 8)))

  val control = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = List(AddressSet(0x2000000000L, 0x3ffffffL)), // when truncated to 32-bits, is 0
      resources     = device.reg("control"),
      supportsWrite = TransferSizes(1, 4),
      supportsRead  = TransferSizes(1, 4),
      interleavedId = Some(0))), // AXI4-Lite never interleaves responses
    beatBytes = 4)))

  val master = AXI4MasterNode(Seq(AXI4MasterPortParameters(
    masters = Seq(AXI4MasterParameters(
      name    = "VC707 PCIe",
      id      = IdRange(0, 1),
      aligned = false)))))

  val intnode = IntSourceNode(IntSourcePortSimple(resources = device.int))

  lazy val module = new LazyModuleImp(this) {
    // The master on the control port must be AXI-lite
    require (control.edges.in(0).master.endId == 1)
    // Must have exactly the right number of idBits
    require (slave.edges.in(0).bundle.idBits == 4)

    class VC707AXIToPCIeX1IOBundle extends Bundle with VC707AXIToPCIeX1IOSerial
                                                  with VC707AXIToPCIeX1IOClocksReset;

    val io = IO(new Bundle {
      val port = new VC707AXIToPCIeX1IOBundle
      val REFCLK = Bool(INPUT)
    })

    val blackbox = Module(new vc707axi_to_pcie_x1)

    val (s, _) = slave.in(0)
    val (c, _) = control.in(0)
    val (m, _) = master.out(0)
    val (i, _) = intnode.out(0)

    //to top level
    blackbox.io.axi_aresetn         := io.port.axi_aresetn
    io.port.axi_aclk_out            := blackbox.io.axi_aclk_out
    io.port.axi_ctl_aclk_out        := blackbox.io.axi_ctl_aclk_out
    io.port.mmcm_lock               := blackbox.io.mmcm_lock
    io.port.pci_exp_txp             := blackbox.io.pci_exp_txp
    io.port.pci_exp_txn             := blackbox.io.pci_exp_txn
    blackbox.io.pci_exp_rxp         := io.port.pci_exp_rxp
    blackbox.io.pci_exp_rxn         := io.port.pci_exp_rxn
    i(0)                            := blackbox.io.interrupt_out
    blackbox.io.REFCLK              := io.REFCLK

    //s
    //AXI4 signals ordered as per AXI4 Specification (Release D) Section A.2
    //-{lock, cache, prot, qos}
    //-{aclk, aresetn, awuser, wid, wuser, buser, ruser}
    //global signals
    //aclk                          :=
    //aresetn                       :=
    //slave interface write address
    blackbox.io.s_axi_awid          := s.aw.bits.id
    blackbox.io.s_axi_awaddr        := s.aw.bits.addr
    blackbox.io.s_axi_awlen         := s.aw.bits.len
    blackbox.io.s_axi_awsize        := s.aw.bits.size
    blackbox.io.s_axi_awburst       := s.aw.bits.burst
    //blackbox.io.s_axi_awlock      := s.aw.bits.lock
    //blackbox.io.s_axi_awcache     := s.aw.bits.cache
    //blackbox.io.s_axi_awprot      := s.aw.bits.prot
    //blackbox.io.s_axi_awqos       := s.aw.bits.qos
    blackbox.io.s_axi_awregion      := UInt(0)
    //blackbox.io.awuser            := s.aw.bits.user
    blackbox.io.s_axi_awvalid       := s.aw.valid
    s.aw.ready                   := blackbox.io.s_axi_awready
    //slave interface write data ports
    //blackbox.io.s_axi_wid         := s.w.bits.id
    blackbox.io.s_axi_wdata         := s.w.bits.data
    blackbox.io.s_axi_wstrb         := s.w.bits.strb
    blackbox.io.s_axi_wlast         := s.w.bits.last
    //blackbox.io.s_axi_wuser       := s.w.bits.user
    blackbox.io.s_axi_wvalid        := s.w.valid
    s.w.ready                    := blackbox.io.s_axi_wready
    //slave interface write response
    s.b.bits.id                  := blackbox.io.s_axi_bid
    s.b.bits.resp                := blackbox.io.s_axi_bresp
    //s.b.bits.user              := blackbox.io.s_axi_buser
    s.b.valid                    := blackbox.io.s_axi_bvalid
    blackbox.io.s_axi_bready        := s.b.ready
    //slave AXI interface read address ports
    blackbox.io.s_axi_arid          := s.ar.bits.id
    blackbox.io.s_axi_araddr        := s.ar.bits.addr
    blackbox.io.s_axi_arlen         := s.ar.bits.len
    blackbox.io.s_axi_arsize        := s.ar.bits.size
    blackbox.io.s_axi_arburst       := s.ar.bits.burst
    //blackbox.io.s_axi_arlock      := s.ar.bits.lock
    //blackbox.io.s_axi_arcache     := s.ar.bits.cache
    //blackbox.io.s_axi_arprot      := s.ar.bits.prot
    //blackbox.io.s_axi_arqos       := s.ar.bits.qos
    blackbox.io.s_axi_arregion      := UInt(0)
    //blackbox.io.s_axi_aruser      := s.ar.bits.user
    blackbox.io.s_axi_arvalid       := s.ar.valid
    s.ar.ready                   := blackbox.io.s_axi_arready
    //slave AXI interface read data ports
    s.r.bits.id                  := blackbox.io.s_axi_rid
    s.r.bits.data                := blackbox.io.s_axi_rdata
    s.r.bits.resp                := blackbox.io.s_axi_rresp
    s.r.bits.last                := blackbox.io.s_axi_rlast
    //s.r.bits.ruser             := blackbox.io.s_axi_ruser
    s.r.valid                    := blackbox.io.s_axi_rvalid
    blackbox.io.s_axi_rready        := s.r.ready

    //ctl
    //axi-lite slave interface write address
    blackbox.io.s_axi_ctl_awaddr    := c.aw.bits.addr
    blackbox.io.s_axi_ctl_awvalid   := c.aw.valid
    c.aw.ready                 := blackbox.io.s_axi_ctl_awready
    //axi-lite slave interface write data ports
    blackbox.io.s_axi_ctl_wdata     := c.w.bits.data
    blackbox.io.s_axi_ctl_wstrb     := c.w.bits.strb
    blackbox.io.s_axi_ctl_wvalid    := c.w.valid
    c.w.ready                  := blackbox.io.s_axi_ctl_wready
    //axi-lite slave interface write response
    blackbox.io.s_axi_ctl_bready    := c.b.ready
    c.b.bits.id                := UInt(0)
    c.b.bits.resp              := blackbox.io.s_axi_ctl_bresp
    c.b.valid                  := blackbox.io.s_axi_ctl_bvalid
    //axi-lite slave AXI interface read address ports
    blackbox.io.s_axi_ctl_araddr    := c.ar.bits.addr
    blackbox.io.s_axi_ctl_arvalid   := c.ar.valid
    c.ar.ready                 := blackbox.io.s_axi_ctl_arready
    //slave AXI interface read data ports
    blackbox.io.s_axi_ctl_rready    := c.r.ready
    c.r.bits.id                := UInt(0)
    c.r.bits.data              := blackbox.io.s_axi_ctl_rdata
    c.r.bits.resp              := blackbox.io.s_axi_ctl_rresp
    c.r.bits.last              := Bool(true)
    c.r.valid                  := blackbox.io.s_axi_ctl_rvalid

    //m
    //AXI4 signals ordered per AXI4 Specification (Release D) Section A.2
    //-{id,region,qos}
    //-{aclk, aresetn, awuser, wid, wuser, buser, ruser}
    //global signals
    //aclk                          :=
    //aresetn                       :=
    //master interface write address
    m.aw.bits.id                 := UInt(0)
    m.aw.bits.addr               := blackbox.io.m_axi_awaddr
    m.aw.bits.len                := blackbox.io.m_axi_awlen
    m.aw.bits.size               := blackbox.io.m_axi_awsize
    m.aw.bits.burst              := blackbox.io.m_axi_awburst
    m.aw.bits.lock               := blackbox.io.m_axi_awlock
    m.aw.bits.cache              := blackbox.io.m_axi_awcache
    m.aw.bits.prot               := blackbox.io.m_axi_awprot
    m.aw.bits.qos                := UInt(0)
    //m.aw.bits.region           := blackbox.io.m_axi_awregion
    //m.aw.bits.user             := blackbox.io.m_axi_awuser
    m.aw.valid                   := blackbox.io.m_axi_awvalid
    blackbox.io.m_axi_awready    := m.aw.ready

    //master interface write data ports
    m.w.bits.data                := blackbox.io.m_axi_wdata
    m.w.bits.strb                := blackbox.io.m_axi_wstrb
    m.w.bits.last                := blackbox.io.m_axi_wlast
    //m.w.bits.user              := blackbox.io.m_axi_wuser
    m.w.valid                    := blackbox.io.m_axi_wvalid
    blackbox.io.m_axi_wready     := m.w.ready

    //master interface write response
    //blackbox.io.m_axi_bid      := m.b.bits.id
    blackbox.io.m_axi_bresp      := m.b.bits.resp
    //blackbox.io.m_axi_buser    := m.b.bits.user
    blackbox.io.m_axi_bvalid     := m.b.valid
    m.b.ready                    := blackbox.io.m_axi_bready

    //master AXI interface read address ports
    m.ar.bits.id                 := UInt(0)
    m.ar.bits.addr               := blackbox.io.m_axi_araddr
    m.ar.bits.len                := blackbox.io.m_axi_arlen
    m.ar.bits.size               := blackbox.io.m_axi_arsize
    m.ar.bits.burst              := blackbox.io.m_axi_arburst
    m.ar.bits.lock               := blackbox.io.m_axi_arlock
    m.ar.bits.cache              := blackbox.io.m_axi_arcache
    m.ar.bits.prot               := blackbox.io.m_axi_arprot
    m.ar.bits.qos                := UInt(0)
    //m.ar.bits.region           := blackbox.io.m_axi_arregion
    //m.ar.bits.user             := blackbox.io.s_axi_aruser
    m.ar.valid                   := blackbox.io.m_axi_arvalid
    blackbox.io.m_axi_arready    := m.ar.ready

    //master AXI interface read data ports
    //blackbox.io.m_axi_rid      := m.r.bits.id
    blackbox.io.m_axi_rdata      := m.r.bits.data
    blackbox.io.m_axi_rresp      := m.r.bits.resp
    blackbox.io.m_axi_rlast      := m.r.bits.last
    //blackbox.io.s_axi_ruser    := s.bits.ruser
    blackbox.io.m_axi_rvalid     := m.r.valid
    m.r.ready                    := blackbox.io.m_axi_rready
  }
  
  ElaborationArtefacts.add(
    "vc707axi_to_pcie_x1.vivado.tcl",
    """ 
      create_ip -vendor xilinx.com -library ip -version 2.8 -name axi_pcie -module_name vc707axi_to_pcie_x1 -dir $ipdir -force
      set_property -dict [list \
      CONFIG.AXIBAR2PCIEBAR_0             {0x40000000} \
      CONFIG.AXIBAR2PCIEBAR_1             {0x00000000} \
      CONFIG.AXIBAR2PCIEBAR_2             {0x00000000} \
      CONFIG.AXIBAR2PCIEBAR_3             {0x00000000} \
      CONFIG.AXIBAR2PCIEBAR_4             {0x00000000} \
      CONFIG.AXIBAR2PCIEBAR_5             {0x00000000} \
      CONFIG.AXIBAR_0                     {0x40000000} \
      CONFIG.AXIBAR_1                     {0xFFFFFFFF} \
      CONFIG.AXIBAR_2                     {0xFFFFFFFF} \
      CONFIG.AXIBAR_3                     {0xFFFFFFFF} \
      CONFIG.AXIBAR_4                     {0xFFFFFFFF} \
      CONFIG.AXIBAR_5                     {0xFFFFFFFF} \
      CONFIG.AXIBAR_AS_0                  {true} \
      CONFIG.AXIBAR_AS_1                  {false} \
      CONFIG.AXIBAR_AS_2                  {false} \
      CONFIG.AXIBAR_AS_3                  {false} \
      CONFIG.AXIBAR_AS_4                  {false} \
      CONFIG.AXIBAR_AS_5                  {false} \
      CONFIG.AXIBAR_HIGHADDR_0            {0x5FFFFFFF} \
      CONFIG.AXIBAR_HIGHADDR_1            {0x00000000} \
      CONFIG.AXIBAR_HIGHADDR_2            {0x00000000} \
      CONFIG.AXIBAR_HIGHADDR_3            {0x00000000} \
      CONFIG.AXIBAR_HIGHADDR_4            {0x00000000} \
      CONFIG.AXIBAR_HIGHADDR_5            {0x00000000} \
      CONFIG.AXIBAR_NUM                   {1} \
      CONFIG.BAR0_ENABLED                 {true} \
      CONFIG.BAR0_SCALE                   {Gigabytes} \
      CONFIG.BAR0_SIZE                    {4} \
      CONFIG.BAR0_TYPE                    {Memory} \
      CONFIG.BAR1_ENABLED                 {false} \
      CONFIG.BAR1_SCALE                   {N/A} \
      CONFIG.BAR1_SIZE                    {8} \
      CONFIG.BAR1_TYPE                    {N/A} \
      CONFIG.BAR2_ENABLED                 {false} \
      CONFIG.BAR2_SCALE                   {N/A} \
      CONFIG.BAR2_SIZE                    {8} \
      CONFIG.BAR2_TYPE                    {N/A} \
      CONFIG.BAR_64BIT                    {true} \
      CONFIG.BASEADDR                     {0x00000000} \
      CONFIG.BASE_CLASS_MENU              {Bridge_device} \
      CONFIG.CLASS_CODE                   {0x060400} \
      CONFIG.COMP_TIMEOUT                 {50ms} \
      CONFIG.Component_Name               {design_1_axi_pcie_1_0} \
      CONFIG.DEVICE_ID                    {0x7111} \
      CONFIG.ENABLE_CLASS_CODE            {true} \
      CONFIG.HIGHADDR                     {0x03FFFFFF} \
      CONFIG.INCLUDE_BAROFFSET_REG        {true} \
      CONFIG.INCLUDE_RC                   {Root_Port_of_PCI_Express_Root_Complex} \
      CONFIG.INTERRUPT_PIN                {false} \
      CONFIG.MAX_LINK_SPEED               {2.5_GT/s} \
      CONFIG.MSI_DECODE_ENABLED           {true} \
      CONFIG.M_AXI_ADDR_WIDTH             {32} \
      CONFIG.M_AXI_DATA_WIDTH             {64} \
      CONFIG.NO_OF_LANES                  {X1} \
      CONFIG.NUM_MSI_REQ                  {0} \
      CONFIG.PCIEBAR2AXIBAR_0_SEC         {1} \
      CONFIG.PCIEBAR2AXIBAR_0             {0x00000000} \
      CONFIG.PCIEBAR2AXIBAR_1             {0xFFFFFFFF} \
      CONFIG.PCIEBAR2AXIBAR_1_SEC         {1} \
      CONFIG.PCIEBAR2AXIBAR_2             {0xFFFFFFFF} \
      CONFIG.PCIEBAR2AXIBAR_2_SEC         {1} \
      CONFIG.PCIE_BLK_LOCN                {X1Y1} \
      CONFIG.PCIE_USE_MODE                {GES_and_Production} \
      CONFIG.REF_CLK_FREQ                 {100_MHz} \
      CONFIG.REV_ID                       {0x00} \
      CONFIG.SLOT_CLOCK_CONFIG            {true} \
      CONFIG.SUBSYSTEM_ID                 {0x0007} \
      CONFIG.SUBSYSTEM_VENDOR_ID          {0x10EE} \
      CONFIG.SUB_CLASS_INTERFACE_MENU     {Host_bridge} \
      CONFIG.S_AXI_ADDR_WIDTH             {32} \
      CONFIG.S_AXI_DATA_WIDTH             {64} \
      CONFIG.S_AXI_ID_WIDTH               {4} \
      CONFIG.S_AXI_SUPPORTS_NARROW_BURST  {false} \
      CONFIG.VENDOR_ID                    {0x10EE} \
      CONFIG.XLNX_REF_BOARD               {None} \
      CONFIG.axi_aclk_loopback            {false} \
      CONFIG.en_ext_ch_gt_drp             {false} \
      CONFIG.en_ext_clk                   {false} \
      CONFIG.en_ext_gt_common             {false} \
      CONFIG.en_ext_pipe_interface        {false} \
      CONFIG.en_transceiver_status_ports  {false} \
      CONFIG.no_slv_err                   {false} \
      CONFIG.rp_bar_hide                  {true} \
      CONFIG.shared_logic_in_core         {false} ] [get_ips vc707axi_to_pcie_x1]"""
  )
}
