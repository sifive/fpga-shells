// See LICENSE for license details.
package sifive.fpgashells.ip.microsemi.polarfirepcierootport

import Chisel._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.amba.apb._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.util.{ElaborationArtefacts}


import chisel3.core.{Input, Output, attach}

// Black Box for Microsemi PolarFire PCIe root port

trait PolarFirePCIeIOSerial extends Bundle {
  //serial external pins
  val PCIESS_LANE_TXD0_N    = Bool(OUTPUT)
  val PCIESS_LANE_TXD0_P    = Bool(OUTPUT)
  val PCIESS_LANE_TXD1_N    = Bool(OUTPUT)
  val PCIESS_LANE_TXD1_P    = Bool(OUTPUT)
  val PCIESS_LANE_TXD2_N    = Bool(OUTPUT)
  val PCIESS_LANE_TXD2_P    = Bool(OUTPUT)
  val PCIESS_LANE_TXD3_N    = Bool(OUTPUT)
  val PCIESS_LANE_TXD3_P    = Bool(OUTPUT)
  
  val PCIESS_LANE_RXD0_N    = Bool(INPUT)
  val PCIESS_LANE_RXD0_P    = Bool(INPUT)
  val PCIESS_LANE_RXD1_N    = Bool(INPUT)
  val PCIESS_LANE_RXD1_P    = Bool(INPUT)
  val PCIESS_LANE_RXD2_N    = Bool(INPUT)
  val PCIESS_LANE_RXD2_P    = Bool(INPUT)
  val PCIESS_LANE_RXD3_N    = Bool(INPUT)
  val PCIESS_LANE_RXD3_P    = Bool(INPUT)
}

trait PolarFirePCIeIOClocksReset extends Bundle {
  //clock, reset, control
  val APB_S_PCLK                    = Clock(INPUT)
  val APB_S_PRESET_N                = Bool(INPUT)
  
  val AXI_CLK                       = Clock(INPUT)
  val AXI_CLK_STABLE                = Bool(INPUT)
  
  val PCIE_1_TL_CLK_125MHz          = Clock(INPUT)
  val PCIE_1_TX_BIT_CLK             = Clock(INPUT)
  val PCIE_1_TX_PLL_REF_CLK         = Clock(INPUT)
  val PCIE_1_TX_PLL_LOCK            = Bool(INPUT)
  
  val PCIESS_LANE0_CDR_REF_CLK_0    = Clock(INPUT)
  val PCIESS_LANE1_CDR_REF_CLK_0    = Clock(INPUT)
  val PCIESS_LANE2_CDR_REF_CLK_0    = Clock(INPUT)
  val PCIESS_LANE3_CDR_REF_CLK_0    = Clock(INPUT)
}

trait PolarFirePCIeIODebug extends Bundle {
  
    val debug_pclk    = Output(Clock())
    val debug_preset  = Output(Bool())
    val debug_penable = Output(Bool())
    val debug_psel    = Output(Bool())
    val debug_paddr2  = Output(Bool())
    val debug_paddr3  = Output(Bool())
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module
class polarfire_pcie_rp() extends BlackBox
{
  val io = new Bundle with PolarFirePCIeIOSerial
                      with PolarFirePCIeIOClocksReset {

    // APB control interface
    val APB_S_PADDR     = Bits(INPUT,26)
    val APB_S_PENABLE   = Bool(INPUT)
    val APB_S_PSEL      = Bool(INPUT)
    val APB_S_PWDATA    = Bits(INPUT,32)
    val APB_S_PWRITE    = Bool(INPUT)
    val APB_S_PRDATA    = Bits(OUTPUT,32)
    val APB_S_PREADY    = Bool(OUTPUT)    
    val APB_S_PSLVERR   = Bool(OUTPUT)

    //axi slave
    val PCIESS_AXI_1_S_ARREADY  = Bool(OUTPUT)
    val PCIESS_AXI_1_S_AWREADY  = Bool(OUTPUT)
    val PCIESS_AXI_1_S_BID      = Bits(OUTPUT,4)
    val PCIESS_AXI_1_S_BRESP    = Bits(OUTPUT,2)
    val PCIESS_AXI_1_S_BVALID   = Bool(OUTPUT)
    val PCIESS_AXI_1_S_RDATA    = Bits(OUTPUT,64)
    val PCIESS_AXI_1_S_RID      = Bits(OUTPUT,4)
    val PCIESS_AXI_1_S_RLAST    = Bool(OUTPUT)
    val PCIESS_AXI_1_S_RRESP    = Bits(OUTPUT,2)
    val PCIESS_AXI_1_S_RVALID   = Bool(OUTPUT)
    val PCIESS_AXI_1_S_WREADY   = Bool(OUTPUT)
    
    val PCIESS_AXI_1_S_ARADDR   = Bits(INPUT,32)
    val PCIESS_AXI_1_S_ARBURST  = Bits(INPUT,2)
    val PCIESS_AXI_1_S_ARID     = Bits(INPUT,4)
    val PCIESS_AXI_1_S_ARLEN    = Bits(INPUT,8)
    val PCIESS_AXI_1_S_ARSIZE   = Bits(INPUT,2)
    val PCIESS_AXI_1_S_ARVALID  = Bool(INPUT)
    val PCIESS_AXI_1_S_AWADDR   = Bits(INPUT,32)
    val PCIESS_AXI_1_S_AWBURST  = Bits(INPUT,2)
    val PCIESS_AXI_1_S_AWID     = Bits(INPUT,4)
    val PCIESS_AXI_1_S_AWLEN    = Bits(INPUT,8)
    val PCIESS_AXI_1_S_AWSIZE   = Bits(INPUT,2)
    val PCIESS_AXI_1_S_AWVALID  = Bool(INPUT)
    val PCIESS_AXI_1_S_BREADY   = Bool(INPUT)
    val PCIESS_AXI_1_S_RREADY   = Bool(INPUT)
    val PCIESS_AXI_1_S_WDATA    = Bits(INPUT,64)
    val PCIESS_AXI_1_S_WLAST    = Bool(INPUT)
    val PCIESS_AXI_1_S_WSTRB    = Bits(INPUT,8)
    val PCIESS_AXI_1_S_WVALID   = Bool(INPUT)
    
    //axi master
    val PCIESS_AXI_1_M_ARADDR   = Bits(OUTPUT,32)
    val PCIESS_AXI_1_M_ARBURST  = Bits(OUTPUT,2)
    val PCIESS_AXI_1_M_ARID     = Bits(OUTPUT,4)
    val PCIESS_AXI_1_M_ARLEN    = Bits(OUTPUT,8)
    val PCIESS_AXI_1_M_ARSIZE   = Bits(OUTPUT,2)
    val PCIESS_AXI_1_M_ARVALID  = Bool(OUTPUT)
    val PCIESS_AXI_1_M_AWADDR   = Bits(OUTPUT,32)
    val PCIESS_AXI_1_M_AWBURST  = Bits(OUTPUT,2)
    val PCIESS_AXI_1_M_AWID     = Bits(OUTPUT,4)
    val PCIESS_AXI_1_M_AWLEN    = Bits(OUTPUT,8)
    val PCIESS_AXI_1_M_AWSIZE   = Bits(OUTPUT,2)
    val PCIESS_AXI_1_M_AWVALID  = Bool(OUTPUT)
    val PCIESS_AXI_1_M_BREADY   = Bool(OUTPUT)
    val PCIESS_AXI_1_M_RREADY   = Bool(OUTPUT)
    val PCIESS_AXI_1_M_WDATA    = Bits(OUTPUT,64)
    val PCIESS_AXI_1_M_WLAST    = Bool(OUTPUT)
    val PCIESS_AXI_1_M_WSTRB    = Bits(OUTPUT,8)
    val PCIESS_AXI_1_M_WVALID   = Bool(OUTPUT)
    
    val PCIESS_AXI_1_M_ARREADY  = Bool(INPUT)
    val PCIESS_AXI_1_M_AWREADY  = Bool(INPUT)
    val PCIESS_AXI_1_M_BID      = Bits(INPUT,4)
    val PCIESS_AXI_1_M_BRESP    = Bits(INPUT,2)
    val PCIESS_AXI_1_M_BVALID   = Bool(INPUT)
    val PCIESS_AXI_1_M_RDATA    = Bits(INPUT,64)
    val PCIESS_AXI_1_M_RID      = Bits(INPUT,4)
    val PCIESS_AXI_1_M_RLAST    = Bool(INPUT)
    val PCIESS_AXI_1_M_RRESP    = Bits(INPUT,2)
    val PCIESS_AXI_1_M_RVALID   = Bool(INPUT)
    val PCIESS_AXI_1_M_WREADY   = Bool(INPUT)

    // Misc
    val DLL_OUT                 = Bool(OUTPUT)
    val PCIE_1_DLUP_EXIT        = Bool(OUTPUT)
    val PCIE_1_HOT_RST_EXIT     = Bool(OUTPUT)
    val PCIE_1_INTERRUPT_OUT    = Bool(OUTPUT)
    val PCIE_1_L2_EXIT          = Bool(OUTPUT)
    val PCIE_1_LTSSM            = Bits(OUTPUT,5)
    val PCIE_1_M_WDERR          = Bool(OUTPUT)
    val PCIE_1_S_RDERR          = Bool(OUTPUT)
    
    val PCIE_1_INTERRUPT        = Bits(INPUT,8)
    val PCIE_1_M_RDERR          = Bool(INPUT)
    val PCIE_1_S_WDERR          = Bool(INPUT)
 }
}
//scalastyle:off

class PolarFirePCIeX4(implicit p:Parameters) extends LazyModule
{
  val device = new SimpleDevice("pci", Seq("plda,axi-pcie-root-port-1.00")) {
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
                                                               ResourceMapping(address,
                                                                 BigInt(0x02000000) << 64, perms) }),
        "interrupt-controller" -> Seq(ResourceMap(labels = Seq(intc), value = Map(
          "interrupt-controller" -> Nil,
          "#address-cells"       -> ofInt(0),
          "#interrupt-cells"     -> ofInt(1)))))
      Description(name, mapping ++ extra)
    }
  }

  val slave = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = List(AddressSet(0x40000000L, 0x1fffffffL),AddressSet(0x2030000000L, 0x0fffffffL)),
      resources     = Seq(Resource(device, "ranges")),
      executable    = true,
      supportsWrite = TransferSizes(1, 128),
      supportsRead  = TransferSizes(1, 128))),
    beatBytes = 8)))

  val control = APBSlaveNode(Seq(APBSlavePortParameters(
    slaves = Seq(APBSlaveParameters(
      address       = List(AddressSet(0x2000000000L, 0x03ffffffL)),
      resources     = device.reg("control"),
      supportsWrite = TransferSizes(1, 4),
      supportsRead  = TransferSizes(1, 4))),
    beatBytes = 4)))


  val master = AXI4MasterNode(Seq(AXI4MasterPortParameters(
    masters = Seq(AXI4MasterParameters(
      name    = "PolarFire PCIe",
      id      = IdRange(0, 16),
      aligned = false)))))

  val intnode = IntSourceNode(IntSourcePortSimple(resources = device.int))

  lazy val module = new LazyModuleImp(this) {
    // Must have exactly the right number of idBits
    require (slave.edges.in(0).bundle.idBits == 4)
    require (master.edges.out(0).bundle.dataBits == 64)

    class PolarFirePCIeX4IOBundle extends Bundle with PolarFirePCIeIOSerial
                                                 with PolarFirePCIeIODebug
                                                 with PolarFirePCIeIOClocksReset;

    val io = IO(new Bundle {
      val port = new PolarFirePCIeX4IOBundle
      val REFCLK = Bool(INPUT)
    })

    val blackbox = Module(new polarfire_pcie_rp)

    val (s, _) = slave.in(0)
    val (c, _) = control.in(0)
    val (m, _) = master.out(0)
    val (i, _) = intnode.out(0)

    //to top level
    i(0)                        := blackbox.io.PCIE_1_INTERRUPT_OUT
    blackbox.io.APB_S_PCLK      := io.port.APB_S_PCLK
    blackbox.io.APB_S_PRESET_N  := io.port.APB_S_PRESET_N
    
    blackbox.io.AXI_CLK         := io.port.AXI_CLK
    blackbox.io.AXI_CLK_STABLE  := io.port.AXI_CLK_STABLE
    
    blackbox.io.PCIE_1_TL_CLK_125MHz        := io.port.PCIE_1_TL_CLK_125MHz
    blackbox.io.PCIE_1_TX_BIT_CLK           := io.port.PCIE_1_TX_BIT_CLK
    blackbox.io.PCIE_1_TX_PLL_REF_CLK       := io.port.PCIE_1_TX_PLL_REF_CLK
    blackbox.io.PCIE_1_TX_PLL_LOCK          := io.port.PCIE_1_TX_PLL_LOCK
    blackbox.io.PCIESS_LANE0_CDR_REF_CLK_0  := io.port.PCIESS_LANE0_CDR_REF_CLK_0
    blackbox.io.PCIESS_LANE1_CDR_REF_CLK_0  := io.port.PCIESS_LANE1_CDR_REF_CLK_0
    blackbox.io.PCIESS_LANE2_CDR_REF_CLK_0  := io.port.PCIESS_LANE2_CDR_REF_CLK_0
    blackbox.io.PCIESS_LANE3_CDR_REF_CLK_0  := io.port.PCIESS_LANE3_CDR_REF_CLK_0
    
    io.port.PCIESS_LANE_TXD0_N      := blackbox.io.PCIESS_LANE_TXD0_N
    io.port.PCIESS_LANE_TXD0_P      := blackbox.io.PCIESS_LANE_TXD0_P
    io.port.PCIESS_LANE_TXD1_N      := blackbox.io.PCIESS_LANE_TXD1_N
    io.port.PCIESS_LANE_TXD1_P      := blackbox.io.PCIESS_LANE_TXD1_P
    io.port.PCIESS_LANE_TXD2_N      := blackbox.io.PCIESS_LANE_TXD2_N
    io.port.PCIESS_LANE_TXD2_P      := blackbox.io.PCIESS_LANE_TXD2_P
    io.port.PCIESS_LANE_TXD3_N      := blackbox.io.PCIESS_LANE_TXD3_N
    io.port.PCIESS_LANE_TXD3_P      := blackbox.io.PCIESS_LANE_TXD3_P

    blackbox.io.PCIESS_LANE_RXD0_N  := io.port.PCIESS_LANE_RXD0_N
    blackbox.io.PCIESS_LANE_RXD0_P  := io.port.PCIESS_LANE_RXD0_P
    blackbox.io.PCIESS_LANE_RXD1_N  := io.port.PCIESS_LANE_RXD1_N
    blackbox.io.PCIESS_LANE_RXD1_P  := io.port.PCIESS_LANE_RXD1_P
    blackbox.io.PCIESS_LANE_RXD2_N  := io.port.PCIESS_LANE_RXD2_N
    blackbox.io.PCIESS_LANE_RXD2_P  := io.port.PCIESS_LANE_RXD2_P
    blackbox.io.PCIESS_LANE_RXD3_N  := io.port.PCIESS_LANE_RXD3_N
    blackbox.io.PCIESS_LANE_RXD3_P  := io.port.PCIESS_LANE_RXD3_P

    //s
    //slave interface
    blackbox.io.PCIESS_AXI_1_S_AWID     := s.aw.bits.id
    blackbox.io.PCIESS_AXI_1_S_AWADDR   := s.aw.bits.addr
    blackbox.io.PCIESS_AXI_1_S_AWLEN    := s.aw.bits.len
    blackbox.io.PCIESS_AXI_1_S_AWSIZE   := s.aw.bits.size
    blackbox.io.PCIESS_AXI_1_S_AWBURST  := s.aw.bits.burst
    blackbox.io.PCIESS_AXI_1_S_AWVALID	:= s.aw.valid
    s.aw.ready                   := blackbox.io.PCIESS_AXI_1_S_AWREADY
    
    blackbox.io.PCIESS_AXI_1_S_WDATA    := s.w.bits.data
    blackbox.io.PCIESS_AXI_1_S_WSTRB    := s.w.bits.strb
    blackbox.io.PCIESS_AXI_1_S_WLAST    := s.w.bits.last
    blackbox.io.PCIESS_AXI_1_S_WVALID   := s.w.valid
    s.w.ready                    := blackbox.io.PCIESS_AXI_1_S_WREADY
    
    s.b.bits.id                  := blackbox.io.PCIESS_AXI_1_S_BID
    s.b.bits.resp                := blackbox.io.PCIESS_AXI_1_S_BRESP
    s.b.valid                    		:= blackbox.io.PCIESS_AXI_1_S_BVALID
    blackbox.io.PCIESS_AXI_1_S_BREADY   := s.b.ready
    
    blackbox.io.PCIESS_AXI_1_S_ARID     := s.ar.bits.id
    blackbox.io.PCIESS_AXI_1_S_ARADDR   := s.ar.bits.addr
    blackbox.io.PCIESS_AXI_1_S_ARLEN    := s.ar.bits.len
    blackbox.io.PCIESS_AXI_1_S_ARSIZE   := s.ar.bits.size
    blackbox.io.PCIESS_AXI_1_S_ARBURST  := s.ar.bits.burst
    blackbox.io.PCIESS_AXI_1_S_ARVALID  := s.ar.valid
    s.ar.ready                          := blackbox.io.PCIESS_AXI_1_S_ARREADY
    
    s.r.bits.id                         := blackbox.io.PCIESS_AXI_1_S_RID
    s.r.bits.data                       := blackbox.io.PCIESS_AXI_1_S_RDATA
    s.r.bits.resp                       := blackbox.io.PCIESS_AXI_1_S_RRESP
    s.r.bits.last                       := blackbox.io.PCIESS_AXI_1_S_RLAST
    s.r.valid                           := blackbox.io.PCIESS_AXI_1_S_RVALID
    blackbox.io.PCIESS_AXI_1_S_RREADY   := s.r.ready

    blackbox.io.PCIE_1_INTERRUPT        := UInt(0)
    blackbox.io.PCIE_1_M_RDERR          := Bool(false)
    blackbox.io.PCIE_1_S_WDERR          := Bool(false)

    //ctl
    blackbox.io.APB_S_PADDR := c.paddr
    blackbox.io.APB_S_PENABLE := c.penable
    blackbox.io.APB_S_PSEL := c.psel
    blackbox.io.APB_S_PWDATA := c.pwdata
    blackbox.io.APB_S_PWRITE := c.pwrite
    c.prdata := blackbox.io.APB_S_PRDATA
    c.pready := blackbox.io.APB_S_PREADY
    c.pslverr := blackbox.io.APB_S_PSLVERR
    
    
    // <CJ> debug
    io.port.debug_pclk    := io.port.APB_S_PCLK
    io.port.debug_preset  := io.port.APB_S_PRESET_N
    io.port.debug_penable := c.penable
    io.port.debug_psel    := c.psel
    io.port.debug_paddr2  := c.paddr(2)
    io.port.debug_paddr3  := c.paddr(3)

    //m
    m.aw.bits.cache := UInt(0)
    m.aw.bits.prot  := AXI4Parameters.PROT_PRIVILEDGED
    m.aw.bits.qos   := UInt(0)

    m.aw.bits.id                        := blackbox.io.PCIESS_AXI_1_M_AWID
    m.aw.bits.addr                      := blackbox.io.PCIESS_AXI_1_M_AWADDR
    m.aw.bits.len                       := blackbox.io.PCIESS_AXI_1_M_AWLEN
    m.aw.bits.size                      := blackbox.io.PCIESS_AXI_1_M_AWSIZE
    m.aw.bits.burst                     := blackbox.io.PCIESS_AXI_1_M_AWBURST
    m.aw.bits.id                        := blackbox.io.PCIESS_AXI_1_M_AWID
    m.aw.valid                          := blackbox.io.PCIESS_AXI_1_M_AWVALID
    blackbox.io.PCIESS_AXI_1_M_AWREADY  := m.aw.ready

    m.w.bits.data                       := blackbox.io.PCIESS_AXI_1_M_WDATA
    m.w.bits.strb                       := blackbox.io.PCIESS_AXI_1_M_WSTRB
    m.w.bits.last                       := blackbox.io.PCIESS_AXI_1_M_WLAST
    m.w.valid                           := blackbox.io.PCIESS_AXI_1_M_WVALID
    blackbox.io.PCIESS_AXI_1_M_WREADY   := m.w.ready

    blackbox.io.PCIESS_AXI_1_M_BID      := m.b.bits.id
    blackbox.io.PCIESS_AXI_1_M_BRESP    := m.b.bits.resp
    blackbox.io.PCIESS_AXI_1_M_BVALID   := m.b.valid
    m.b.ready                           := blackbox.io.PCIESS_AXI_1_M_BREADY

    m.ar.bits.cache := UInt(0)
    m.ar.bits.prot  := AXI4Parameters.PROT_PRIVILEDGED
    m.ar.bits.qos   := UInt(0)

    m.ar.bits.id                        := blackbox.io.PCIESS_AXI_1_M_ARID
    m.ar.bits.addr                      := blackbox.io.PCIESS_AXI_1_M_ARADDR
    m.ar.bits.len                       := blackbox.io.PCIESS_AXI_1_M_ARLEN
    m.ar.bits.size                      := blackbox.io.PCIESS_AXI_1_M_ARSIZE
    m.ar.bits.burst                     := blackbox.io.PCIESS_AXI_1_M_ARBURST
    m.ar.valid                          := blackbox.io.PCIESS_AXI_1_M_ARVALID
    m.ar.bits.id                        := blackbox.io.PCIESS_AXI_1_M_ARID
    blackbox.io.PCIESS_AXI_1_M_ARREADY  := m.ar.ready

    blackbox.io.PCIESS_AXI_1_M_RID        := m.r.bits.id
    blackbox.io.PCIESS_AXI_1_M_RDATA      := m.r.bits.data
    blackbox.io.PCIESS_AXI_1_M_RRESP      := m.r.bits.resp
    blackbox.io.PCIESS_AXI_1_M_RLAST      := m.r.bits.last
    blackbox.io.PCIESS_AXI_1_M_RVALID     := m.r.valid
    m.r.ready                    := blackbox.io.PCIESS_AXI_1_M_RREADY
  }
  
  ElaborationArtefacts.add(
    "Libero.polarfire_pcie_root_port.tcl",
    """ 
new_file -type {SmartDesign} -name polarfire_pcie_rp
add_vlnv_instance -component {polarfire_pcie_rp} -library {} -vendor {Actel} -lib {SgCore} -name {PF_PCIE} -version {1.0.234} -instance_name {PF_PCIE_0} -promote_all 0 -file_name {} 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_IS_CONFIGURED:true"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_SIMULATION_LEVEL:RTL"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIESS_LANE0_IS_USED:true"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIESS_LANE1_IS_USED:false"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIESS_LANE2_IS_USED:false"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIESS_LANE3_IS_USED:false"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_GPSS1_LANE0_IS_USED:false"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_GPSS1_LANE1_IS_USED:false"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_GPSS1_LANE2_IS_USED:false"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_GPSS1_LANE3_IS_USED:false"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PROTOCOL_PRESET_USED:PCIe"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_TX_CLK_DIV_FACTOR:1"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_CONTROLLER_ENABLED:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_NUMBER_OF_LANES:x1"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_LANE_RATE:Gen1 (2.5 Gbps)"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_REF_CLK_FREQ:100"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_PORT_TYPE:End Point"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_CDR_REF_CLK_SOURCE:Dedicated"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_CDR_REF_CLK_NUMBER:1"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_CONTROLLER_ENABLED:Enabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_NUMBER_OF_LANES:x4"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_LANE_RATE:Gen2 (5.0 Gbps)"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_REF_CLK_FREQ:100"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_PORT_TYPE:Root Port"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_CDR_REF_CLK_SOURCE:Dedicated"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_CDR_REF_CLK_NUMBER:1"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_VENDOR_ID:0x11AA"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SUB_VENDOR_ID:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_REVISION_ID:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_DEVICE_ID:0x1556"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SUB_SYSTEM_ID:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_CLASS_CODE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_VENDOR_ID:0x11AA"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SUB_VENDOR_ID:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_REVISION_ID:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_DEVICE_ID:0x1556"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SUB_SYSTEM_ID:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_CLASS_CODE:0x060400"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_BAR_MODE:Custom"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_PHY_REF_CLK_SLOT:Slot"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_INTERRUPTS:INTx"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_DE_EMPHASIS:-3.5 dB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_EXPOSE_WAKE_SIG:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_TRANSMIT_SWING:Full Swing"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_BAR_MODE:Custom"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_PHY_REF_CLK_SLOT:Slot"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_INTERRUPTS:MSI4"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_DE_EMPHASIS:-3.5 dB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_EXPOSE_WAKE_SIG:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_TRANSMIT_SWING:Full Swing"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_NUM_FTS:63"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_L0_ACC_LATENCY:No limit"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_L0_EXIT_LATENCY:64 ns to less than 128 ns"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_L1_ENABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_L1_ACC_LATENCY:No limit"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_L1_EXIT_LATENCY:16 us to less than 32 us"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_NUM_FTS:63"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_L0_ACC_LATENCY:No limit"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_L0_EXIT_LATENCY:64 ns to less than 128 ns"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_L1_ENABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_L1_ACC_LATENCY:No limit"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_L1_EXIT_LATENCY:16 us to less than 32 us"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TYPE_BAR_0_TABLE:64-bit prefetchable memory"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SIZE_BAR_0_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TABLE_SIZE_BAR_0_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SOURCE_ADDRESS_BAR_0_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TRANS_ADDRESS_BAR_0_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TYPE_BAR_0_TABLE:64-bit prefetchable memory"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SIZE_BAR_0_TABLE:2 GB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TABLE_SIZE_BAR_0_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SOURCE_ADDRESS_BAR_0_TABLE:0x100000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TRANS_ADDRESS_BAR_0_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TYPE_BAR_1_TABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SIZE_BAR_1_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TABLE_SIZE_BAR_1_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SOURCE_ADDRESS_BAR_1_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TRANS_ADDRESS_BAR_1_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TYPE_BAR_1_TABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SIZE_BAR_1_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TABLE_SIZE_BAR_1_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SOURCE_ADDRESS_BAR_1_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TRANS_ADDRESS_BAR_1_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TYPE_BAR_2_TABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SIZE_BAR_2_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TABLE_SIZE_BAR_2_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SOURCE_ADDRESS_BAR_2_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TRANS_ADDRESS_BAR_2_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TYPE_BAR_2_TABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SIZE_BAR_2_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TABLE_SIZE_BAR_2_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SOURCE_ADDRESS_BAR_2_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TRANS_ADDRESS_BAR_2_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TYPE_BAR_3_TABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SIZE_BAR_3_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TABLE_SIZE_BAR_3_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SOURCE_ADDRESS_BAR_3_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TRANS_ADDRESS_BAR_3_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TYPE_BAR_3_TABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SIZE_BAR_3_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TABLE_SIZE_BAR_3_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SOURCE_ADDRESS_BAR_3_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TRANS_ADDRESS_BAR_3_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TYPE_BAR_4_TABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SIZE_BAR_4_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TABLE_SIZE_BAR_4_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SOURCE_ADDRESS_BAR_4_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TRANS_ADDRESS_BAR_4_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TYPE_BAR_4_TABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SIZE_BAR_4_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TABLE_SIZE_BAR_4_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SOURCE_ADDRESS_BAR_4_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TRANS_ADDRESS_BAR_4_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TYPE_BAR_5_TABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SIZE_BAR_5_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TABLE_SIZE_BAR_5_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_SOURCE_ADDRESS_BAR_5_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_MASTER_TRANS_ADDRESS_BAR_5_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TYPE_BAR_5_TABLE:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SIZE_BAR_5_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TABLE_SIZE_BAR_5_TABLE:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_SOURCE_ADDRESS_BAR_5_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_MASTER_TRANS_ADDRESS_BAR_5_TABLE:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_STATE_TABLE_0:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SIZE_TABLE_0:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SOURCE_ADDRESS_TABLE_0:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_TRANS_ADDRESS_TABLE_0:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_STATE_TABLE_0:Enabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SIZE_TABLE_0:256 MB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SOURCE_ADDRESS_TABLE_0:0x30000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_TRANS_ADDRESS_TABLE_0:0x30000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_STATE_TABLE_1:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SIZE_TABLE_1:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SOURCE_ADDRESS_TABLE_1:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_TRANS_ADDRESS_TABLE_1:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_STATE_TABLE_1:Enabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SIZE_TABLE_1:512 MB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SOURCE_ADDRESS_TABLE_1:0x40000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_TRANS_ADDRESS_TABLE_1:0x40000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_STATE_TABLE_2:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SIZE_TABLE_2:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SOURCE_ADDRESS_TABLE_2:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_TRANS_ADDRESS_TABLE_2:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_STATE_TABLE_2:Enabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SIZE_TABLE_2:2 GB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SOURCE_ADDRESS_TABLE_2:0x80000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_TRANS_ADDRESS_TABLE_2:0x2080000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_STATE_TABLE_3:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SIZE_TABLE_3:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SOURCE_ADDRESS_TABLE_3:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_TRANS_ADDRESS_TABLE_3:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_STATE_TABLE_3:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SIZE_TABLE_3:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SOURCE_ADDRESS_TABLE_3:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_TRANS_ADDRESS_TABLE_3:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_STATE_TABLE_4:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SIZE_TABLE_4:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SOURCE_ADDRESS_TABLE_4:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_TRANS_ADDRESS_TABLE_4:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_STATE_TABLE_4:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SIZE_TABLE_4:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SOURCE_ADDRESS_TABLE_4:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_TRANS_ADDRESS_TABLE_4:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_STATE_TABLE_5:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SIZE_TABLE_5:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SOURCE_ADDRESS_TABLE_5:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_TRANS_ADDRESS_TABLE_5:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_STATE_TABLE_5:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SIZE_TABLE_5:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SOURCE_ADDRESS_TABLE_5:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_TRANS_ADDRESS_TABLE_5:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_STATE_TABLE_6:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SIZE_TABLE_6:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SOURCE_ADDRESS_TABLE_6:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_TRANS_ADDRESS_TABLE_6:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_STATE_TABLE_6:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SIZE_TABLE_6:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SOURCE_ADDRESS_TABLE_6:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_TRANS_ADDRESS_TABLE_6:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_STATE_TABLE_7:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SIZE_TABLE_7:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_SOURCE_ADDRESS_TABLE_7:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_0_SLAVE_TRANS_ADDRESS_TABLE_7:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_STATE_TABLE_7:Disabled"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SIZE_TABLE_7:4 KB"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_SOURCE_ADDRESS_TABLE_7:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_PCIE_1_SLAVE_TRANS_ADDRESS_TABLE_7:0x0000"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_EXPOSE_LANE_DRI_PORTS:false"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"UI_EXPOSE_PCIE_APBLINK_PORTS:true"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"SD_EXPORT_HIDDEN_PORTS:false"} -validate_rules 0 
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"EXPOSE_ALL_DEBUG_PORTS:false"} -validate_rules 0
configure_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0} -params {"XT_ES_DEVICE:true"} -validate_rules 0
fix_vlnv_instance -component {polarfire_pcie_rp} -library {} -name {PF_PCIE_0}

promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:AXI_CLK} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:AXI_CLK_STABLE} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIESS_LANE0_CDR_REF_CLK_0} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIESS_LANE1_CDR_REF_CLK_0} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIESS_LANE2_CDR_REF_CLK_0} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIESS_LANE3_CDR_REF_CLK_0} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_1_TL_CLK_125MHZ} -pin_create_new {}

promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_1_INTERRUPT} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_1_S_WDERR} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_1_M_RDERR} -pin_create_new {}

promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:DLL_OUT} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_1_DLUP_EXIT} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_1_HOT_RST_EXIT} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_1_INTERRUPT_OUT} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_1_L2_EXIT} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_1_LTSSM} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_1_M_WDERR} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_1_S_RDERR} -pin_create_new {}

promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:APB_S_PCLK} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:APB_S_PRESET_N} -pin_create_new {}

promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:AXI_1_SLAVE} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:PCIE_APB_SLAVE} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:CLKS_FROM_TXPLL_TO_PCIE_1} -pin_create_new {}
promote_pin_to_top -component {polarfire_pcie_rp} -library {} -pin {PF_PCIE_0:AXI_1_MASTER} -pin_create_new {}

generate_design -component {polarfire_pcie_rp} -library {} -generator {} -recursive 1
"""
  )

}
