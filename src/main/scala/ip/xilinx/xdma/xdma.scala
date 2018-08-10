// See LICENSE for license details.
package sifive.fpgashells.ip.xilinx.xdma

import chisel3._
import chisel3.util._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.util.{ElaborationArtefacts}

trait HasXDMAPads {
  def lanes: Int

  val pci_exp_txp = Output(UInt(lanes.W))
  val pci_exp_txn = Output(UInt(lanes.W))
  val pci_exp_rxp = Input(UInt(lanes.W))
  val pci_exp_rxn = Input(UInt(lanes.W))
}

trait HasXDMAClocks {
  val sys_clk         = Input(Clock()) // 
  val sys_clk_gt      = Input(Clock()) // PCIe reference clock IBUFDS_GTE4.O
  val sys_rst_n       = Input(Bool())
  val axi_aclk        = Output(Clock())
  val axi_aresetn     = Output(Bool())
}

trait HasXDMAJunk {
  val cfg_ltssm_state = Output(UInt(6.W))
  val user_lnk_up     = Output(Bool())
  val axi_ctl_aresetn = Output(Bool()) // copy of axi_aresetn in RP mode
}

case class XDMABusParams(idBits: Int, addrBits: Int, dataBytes: Int)
{
  val dataBits = dataBytes*8
}

trait HasXDMABus {
  def mbus:  XDMABusParams
  def sbus:  XDMABusParams
  def slbus: XDMABusParams

  // I
  val interrupt_out = Output(Bool())
  val interrupt_out_msi_vec0to31  = Output(Bool())
  val interrupt_out_msi_vec32to63 = Output(Bool())

  // M.AW
  val m_axib_awready = Input(Bool())
  val m_axib_awvalid = Output(Bool())
  val m_axib_awid    = Output(UInt(mbus.idBits.W))
  val m_axib_awaddr  = Output(UInt(mbus.addrBits.W))
  val m_axib_awlen   = Output(UInt(8.W))
  val m_axib_awsize  = Output(UInt(3.W))
  val m_axib_awburst = Output(UInt(2.W))
  val m_axib_awprot  = Output(UInt(3.W))
  val m_axib_awcache = Output(UInt(4.W))
  val m_axib_awlock  = Output(Bool())

  // M.AR
  val m_axib_arready = Input(Bool())
  val m_axib_arvalid = Output(Bool())
  val m_axib_arid    = Output(UInt(mbus.idBits.W))
  val m_axib_araddr  = Output(UInt(mbus.addrBits.W))
  val m_axib_arlen   = Output(UInt(8.W))
  val m_axib_arsize  = Output(UInt(3.W))
  val m_axib_arburst = Output(UInt(2.W))
  val m_axib_arprot  = Output(UInt(3.W))
  val m_axib_arcache = Output(UInt(4.W))
  val m_axib_arlock  = Output(Bool())

  // M.W
  val m_axib_wready = Input(Bool())
  val m_axib_wvalid = Output(Bool())
  val m_axib_wdata  = Output(UInt(mbus.dataBits.W))
  val m_axib_wstrb  = Output(UInt(mbus.dataBytes.W))
  val m_axib_wlast  = Output(Bool())

  // M.B
  val m_axib_bready = Output(Bool())
  val m_axib_bvalid = Input(Bool())
  val m_axib_bid    = Input(UInt(mbus.idBits.W))
  val m_axib_bresp  = Input(UInt(2.W))

  // M.R
  val m_axib_rready = Output(Bool())
  val m_axib_rvalid = Input(Bool())
  val m_axib_rid    = Input(UInt(mbus.idBits.W))
  val m_axib_rdata  = Input(UInt(mbus.dataBits.W))
  val m_axib_rresp  = Input(UInt(2.W))
  val m_axib_rlast  = Input(Bool())

  // S.AW
  val s_axib_awready  = Output(Bool())
  val s_axib_awvalid  = Input(Bool())
  val s_axib_awid     = Input(UInt(sbus.idBits.W))
  val s_axib_awaddr   = Input(UInt(sbus.addrBits.W))
  val s_axib_awregion = Input(UInt(4.W))
  val s_axib_awlen    = Input(UInt(8.W))
  val s_axib_awsize   = Input(UInt(3.W))
  val s_axib_awburst  = Input(UInt(2.W))

  // S.AR
  val s_axib_arready  = Output(Bool())
  val s_axib_arvalid  = Input(Bool())
  val s_axib_arid     = Input(UInt(sbus.idBits.W))
  val s_axib_araddr   = Input(UInt(sbus.addrBits.W))
  val s_axib_arregion = Input(UInt(4.W))
  val s_axib_arlen    = Input(UInt(8.W))
  val s_axib_arsize   = Input(UInt(3.W))
  val s_axib_arburst  = Input(UInt(2.W))

  // S.W
  val s_axib_wready = Output(Bool())
  val s_axib_wvalid = Input(Bool())
  val s_axib_wdata  = Input(UInt(sbus.dataBits.W))
  val s_axib_wstrb  = Input(UInt(sbus.dataBytes.W))
  val s_axib_wlast  = Input(Bool())

  // S.B
  val s_axib_bready = Input(Bool())
  val s_axib_bvalid = Output(Bool())
  val s_axib_bid    = Output(UInt(sbus.idBits.W))
  val s_axib_bresp  = Output(UInt(2.W))

  // S.R
  val s_axib_rready = Input(Bool())
  val s_axib_rvalid = Output(Bool())
  val s_axib_rid    = Output(UInt(sbus.idBits.W))
  val s_axib_rdata  = Output(UInt(sbus.dataBits.W))
  val s_axib_rresp  = Output(UInt(2.W))
  val s_axib_rlast  = Output(Bool())

  // SL.AW
  val s_axil_awready = Output(Bool())
  val s_axil_awvalid = Input(Bool())
  val s_axil_awaddr  = Input(UInt(slbus.addrBits.W))
  val s_axil_awprot  = Input(UInt(3.W))

  // SL.AR
  val s_axil_arready = Output(Bool())
  val s_axil_arvalid = Input(Bool())
  val s_axil_araddr  = Input(UInt(slbus.addrBits.W))
  val s_axil_arprot  = Input(UInt(3.W))

  // SL.W
  val s_axil_wready = Output(Bool())
  val s_axil_wvalid = Input(Bool())
  val s_axil_wdata  = Input(UInt(slbus.dataBits.W))
  val s_axil_wstrb  = Input(UInt(slbus.dataBytes.W))

  // SL.B
  val s_axil_bready = Input(Bool())
  val s_axil_bvalid = Output(Bool())
  val s_axil_bresp  = Output(UInt(2.W))

  // SL.R
  val s_axil_rready = Input(Bool())
  val s_axil_rvalid = Output(Bool())
  val s_axil_rdata  = Output(UInt(slbus.dataBits.W))
  val s_axil_rresp  = Output(UInt(2.W))
}

class XDMABlackBoxIO(
  val lanes: Int,
  val mbus:  XDMABusParams,
  val sbus:  XDMABusParams,
  val slbus: XDMABusParams) extends Bundle
  with HasXDMAPads
  with HasXDMAClocks
  with HasXDMAJunk
  with HasXDMABus

class XDMAPads(val lanes: Int) extends Bundle with HasXDMAPads
class XDMAClocks() extends Bundle with HasXDMAClocks

case class XDMAParams(
  name:     String,
  location: String,
  bars:     Seq[AddressSet],
  control:  BigInt,
  lanes:    Int    = 1,
  gen:      Int    = 3,
  addrBits: Int    = 64,
  mIDBits:  Int    = 4,
  sIDBits:  Int    = 4)
{
  require (!bars.isEmpty)
  require (control >> 32 << 32 == control)
  require (lanes >= 1 && lanes <= 16 && isPow2(lanes))
  require (gen >= 1  && gen <= 3)
  require (addrBits == 32 || addrBits == 64)
  require (mIDBits >= 1 && mIDBits <= 8)
  require (sIDBits >= 1 && sIDBits <= 8)
  bars.foreach { a => require (a.max >> addrBits == 0) }

  private val bandwidth = lanes * 250 << (gen-1) // MB/s
  private val busBytesAt250MHz = bandwidth / 250
  val busBytes = busBytesAt250MHz max 8
  private val minMHz = 250.0 * busBytesAt250MHz / busBytes
  val axiMHz = minMHz max 62.5
}

class XDMABlackBox(c: XDMAParams) extends BlackBox
{
  override def desiredName = c.name

  val mbus  = XDMABusParams(c.mIDBits, c.addrBits, c.busBytes)
  val sbus  = XDMABusParams(c.sIDBits, c.addrBits, c.busBytes)
  val slbus = XDMABusParams(0, 32, 4)

  val io = IO(new XDMABlackBoxIO(c.lanes, mbus, sbus, slbus))
  val pcieGTs = c.gen match {
    case 1 => "2.5_GT/s"
    case 2 => "5.0_GT/s"
    case 3 => "8.0_GT/s"
    case _ => "wrong"
  }

  // 62.5, 125, 250 (no trailing zeros)
  val formatter = new java.text.DecimalFormat("0.###")
  val axiMHzStr = formatter.format(c.axiMHz)

  val bars = c.bars.zipWithIndex.map { case (a, i) =>
    f"""  CONFIG.axibar_${i}			{0x${a.base}%X}				\\
       |  CONFIG.axibar_highaddr_${i}		{0x${a.max}%X}				\\
       |  CONFIG.axibar2pciebar_${i}		{0x${a.base}%X}				\\
       |""".stripMargin
  }

  ElaborationArtefacts.add(s"${desiredName}.vivado.tcl",
    s"""create_ip -vendor xilinx.com -library ip -version 4.1 -name xdma -module_name ${desiredName} -dir $$ipdir -force
       |set_property -dict [list 							\\
       |  CONFIG.functional_mode		{AXI_Bridge}				\\
       |  CONFIG.pcie_blk_locn			{${c.location}}				\\
       |  CONFIG.device_port_type		{Root_Port_of_PCI_Express_Root_Complex}	\\
       |  CONFIG.pf0_bar0_enabled		{false}					\\
       |  CONFIG.pf0_sub_class_interface_menu	{PCI_to_PCI_bridge}			\\
       |  CONFIG.ref_clk_freq			{100_MHz}				\\
       |  CONFIG.pl_link_cap_max_link_width	{X${c.lanes}}				\\
       |  CONFIG.pl_link_cap_max_link_speed	{${pcieGTs}}				\\
       |  CONFIG.msi_rx_pin_en			{true}					\\
       |  CONFIG.axisten_freq			{${axiMHzStr}}				\\
       |  CONFIG.axi_addr_width			{${c.addrBits}}				\\
       |  CONFIG.axi_data_width			{${c.busBytes*8}_bit}			\\
       |  CONFIG.axi_id_width			{${c.mIDBits}}				\\
       |  CONFIG.s_axi_id_width			{${c.sIDBits}}				\\
       |  CONFIG.axibar_num			{${c.bars.size}}			\\
       |${bars.mkString}] [get_ips ${desiredName}]
       |""".stripMargin)
}

class DiplomaticXDMA(c: XDMAParams)(implicit p:Parameters) extends LazyModule
{
  val device = new SimpleDevice("pci", Seq("xlnx,xdma-host-3.00")) {
    override def describe(resources: ResourceBindings): Description = {
      val Description(name, mapping) = super.describe(resources)
      val intc = s"${c.name}_intc"
      def ofInt(x: Int) = Seq(ResourceInt(BigInt(x)))
      def ofMap(x: Int) = Seq(0, 0, 0, x).flatMap(ofInt) ++ Seq(ResourceReference(intc)) ++ ofInt(x)
      val extra = Map(
        "#address-cells"     -> ofInt(3),
        "#size-cells"        -> ofInt(2),
        "#interrupt-cells"   -> ofInt(1),
        "device_type"        -> Seq(ResourceString("pci")),
        "interrupt-names"    -> Seq("misc", "msi0", "msi1").map(ResourceString.apply _),
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
      address       = c.bars,
      resources     = Seq(Resource(device, "ranges")),
      executable    = true,
      supportsWrite = TransferSizes(1, 128),
      supportsRead  = TransferSizes(1, 128))),
    beatBytes = c.busBytes)))

  val control = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = List(AddressSet(c.control, 0x3ffffffL)), // when truncated to 32-bits, is 0
      resources     = device.reg("control"),
      supportsWrite = TransferSizes(1, 4),
      supportsRead  = TransferSizes(1, 4),
      interleavedId = Some(0))), // AXI4-Lite never interleaves responses
    beatBytes = 4)))

  val master = AXI4MasterNode(Seq(AXI4MasterPortParameters(
    masters = Seq(AXI4MasterParameters(
      name    = c.name,
      id      = IdRange(0, 1 << c.mIDBits),
      aligned = false)))))

  val intnode = IntSourceNode(IntSourcePortSimple(num = 3, resources = device.int))

  lazy val module = new LazyRawModuleImp(this) {
    // The master on the control port must be AXI-lite
    require (control.edges.in(0).master.endId == 1)
    // Must have the right number of slave idBits
    require (slave.edges.in(0).bundle.idBits <= c.sIDBits)
    // Must have the right bus width
    require (master.edges.out(0).slave.beatBytes == c.busBytes)

    val io = IO(new Bundle {
      val pads   = new XDMAPads(c.lanes)
      val clocks = new XDMAClocks
    })

    val blackbox = Module(new XDMABlackBox(c))

    val (s, _) = slave.in(0)
    val (t, _) = control.in(0)
    val (m, _) = master.out(0)
    val (i, _) = intnode.out(0)

    // Pads
    io.pads.pci_exp_txp := blackbox.io.pci_exp_txp
    io.pads.pci_exp_txn := blackbox.io.pci_exp_txn
    blackbox.io.pci_exp_rxp := io.pads.pci_exp_rxp
    blackbox.io.pci_exp_rxn := io.pads.pci_exp_rxn

    // Clocks
    blackbox.io.sys_clk    := io.clocks.sys_clk
    blackbox.io.sys_clk_gt := io.clocks.sys_clk_gt
    blackbox.io.sys_rst_n  := io.clocks.sys_rst_n
    io.clocks.axi_aclk     := blackbox.io.axi_aclk
    io.clocks.axi_aresetn  := blackbox.io.axi_aresetn

    // I
    i(0) := blackbox.io.interrupt_out
    i(1) := blackbox.io.interrupt_out_msi_vec0to31
    i(2) := blackbox.io.interrupt_out_msi_vec32to63

    // M.AW
    blackbox.io.m_axib_awready := m.aw.ready
    m.aw.valid := blackbox.io.m_axib_awvalid
    m.aw.bits.id    := blackbox.io.m_axib_awid
    m.aw.bits.addr  := blackbox.io.m_axib_awaddr
    m.aw.bits.len   := blackbox.io.m_axib_awlen
    m.aw.bits.size  := blackbox.io.m_axib_awsize
    m.aw.bits.burst := blackbox.io.m_axib_awburst
    m.aw.bits.prot  := blackbox.io.m_axib_awprot
    m.aw.bits.cache := blackbox.io.m_axib_awcache
    m.aw.bits.lock  := blackbox.io.m_axib_awlock
    m.aw.bits.qos   := 0.U

    // M.AR
    blackbox.io.m_axib_arready := m.ar.ready
    m.ar.valid := blackbox.io.m_axib_arvalid
    m.ar.bits.id    := blackbox.io.m_axib_arid
    m.ar.bits.addr  := blackbox.io.m_axib_araddr
    m.ar.bits.len   := blackbox.io.m_axib_arlen
    m.ar.bits.size  := blackbox.io.m_axib_arsize
    m.ar.bits.burst := blackbox.io.m_axib_arburst
    m.ar.bits.prot  := blackbox.io.m_axib_arprot
    m.ar.bits.cache := blackbox.io.m_axib_arcache
    m.ar.bits.lock  := blackbox.io.m_axib_arlock
    m.ar.bits.qos   := 0.U

    // M.W
    blackbox.io.m_axib_wready := m.w.ready
    m.w.valid := blackbox.io.m_axib_wvalid
    m.w.bits.data := blackbox.io.m_axib_wdata
    m.w.bits.strb := blackbox.io.m_axib_wstrb
    m.w.bits.last := blackbox.io.m_axib_wlast

    // M.B
    m.b.ready := blackbox.io.m_axib_bready
    blackbox.io.m_axib_bvalid := m.b.valid
    blackbox.io.m_axib_bid   := m.b.bits.id
    blackbox.io.m_axib_bresp := m.b.bits.resp

    // M.R
    m.r.ready := blackbox.io.m_axib_rready
    blackbox.io.m_axib_rvalid := m.r.valid
    blackbox.io.m_axib_rid   := m.r.bits.id
    blackbox.io.m_axib_rdata := m.r.bits.data
    blackbox.io.m_axib_rresp := m.r.bits.resp
    blackbox.io.m_axib_rlast := m.r.bits.last

    // S.AW
    s.aw.ready := blackbox.io.s_axib_awready
    blackbox.io.s_axib_awvalid := s.aw.valid
    blackbox.io.s_axib_awid     := s.aw.bits.id
    blackbox.io.s_axib_awaddr   := s.aw.bits.addr
    blackbox.io.s_axib_awregion := 0.U
    blackbox.io.s_axib_awlen    := s.aw.bits.len
    blackbox.io.s_axib_awsize   := s.aw.bits.size
    blackbox.io.s_axib_awburst  := s.aw.bits.burst

    // S.AR
    s.ar.ready := blackbox.io.s_axib_arready
    blackbox.io.s_axib_arvalid := s.ar.valid
    blackbox.io.s_axib_arid     := s.ar.bits.id
    blackbox.io.s_axib_araddr   := s.ar.bits.addr
    blackbox.io.s_axib_arregion := 0.U
    blackbox.io.s_axib_arlen    := s.ar.bits.len
    blackbox.io.s_axib_arsize   := s.ar.bits.size
    blackbox.io.s_axib_arburst  := s.ar.bits.burst

    // S.W
    s.w.ready := blackbox.io.s_axib_wready
    blackbox.io.s_axib_wvalid := s.w.valid
    blackbox.io.s_axib_wdata := s.w.bits.data
    blackbox.io.s_axib_wstrb := s.w.bits.strb
    blackbox.io.s_axib_wlast := s.w.bits.last

    // S.B
    blackbox.io.s_axib_bready := s.b.ready
    s.b.valid := blackbox.io.s_axib_bvalid
    s.b.bits.id   := blackbox.io.s_axib_bid
    s.b.bits.resp := blackbox.io.s_axib_bresp

    // S.R
    blackbox.io.s_axib_rready := s.r.ready
    s.r.valid := blackbox.io.s_axib_rvalid
    s.r.bits.id   := blackbox.io.s_axib_rid
    s.r.bits.data := blackbox.io.s_axib_rdata
    s.r.bits.resp := blackbox.io.s_axib_rresp
    s.r.bits.last := blackbox.io.s_axib_rlast

    // SL.AW
    t.aw.ready := blackbox.io.s_axil_awready
    blackbox.io.s_axil_awvalid := t.aw.valid
    blackbox.io.s_axil_awaddr := t.aw.bits.addr
    blackbox.io.s_axil_awprot := t.aw.bits.prot

    // SL.AR
    t.ar.ready := blackbox.io.s_axil_arready
    blackbox.io.s_axil_arvalid := t.ar.valid
    blackbox.io.s_axil_araddr := t.ar.bits.addr
    blackbox.io.s_axil_arprot := t.ar.bits.prot

    // SL.W
    t.w.ready := blackbox.io.s_axil_wready
    blackbox.io.s_axil_wvalid := t.w.valid
    blackbox.io.s_axil_wdata := t.w.bits.data
    blackbox.io.s_axil_wstrb := t.w.bits.strb

    // SL.B
    blackbox.io.s_axil_bready := t.b.ready
    t.b.valid := blackbox.io.s_axil_bvalid
    t.b.bits.id   := 0.U
    t.b.bits.resp := blackbox.io.s_axil_bresp

    // SL.R
    blackbox.io.s_axil_rready := t.r.ready
    t.r.valid := blackbox.io.s_axil_rvalid
    t.r.bits.id   := 0.U
    t.r.bits.data := blackbox.io.s_axil_rdata
    t.r.bits.resp := blackbox.io.s_axil_rresp
    t.r.bits.last := true.B
  }
}
