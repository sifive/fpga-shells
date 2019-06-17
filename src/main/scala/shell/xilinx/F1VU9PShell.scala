// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.experimental.{attach, Analog, IO, withClockAndReset}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.SyncResetSynchronizerShiftReg
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
//import sifive.blocks.devices.chiplink._
import sifive.fpgashells.devices.xilinx.xilinxf1vu9pddr._
import sifive.fpgashells.ip.xilinx.f1vu9pddr._

// input clk_main_a0 frequency of 250MHz set by using clock group A with recipe A0
// other clock frequencies can be found in aws-fpga/hdk/docs/clock_recipes.csv
class SysClockF1VU9POverlay(val shell: F1VU9PShellBasicOverlays, val name: String, params: ClockInputOverlayParams)
  extends SingleEndedClockInputXilinxOverlay(params)
{
  val node = shell { ClockSourceNode(freqMHz = 125)(ValName(name)) }
  shell { InModuleBody {
    val clk: Clock = io
  } }
}

class LEDF1VU9POverlay(val shell: F1VU9PShellBasicOverlays, val name: String, params: LEDOverlayParams)
  extends LEDXilinxOverlay(params)
{
  override val width = 16 // F1 has 16 virtual LEDs
}

class SwitchF1VU9POverlay(val shell: F1VU9PShellBasicOverlays, val name: String, params: SwitchOverlayParams)
  extends SwitchXilinxOverlay(params)
{
  override val width = 16 // 16 virtual DIP switch inputs
}

/*
class UARTF1VU9POverlay(val shell: F1VU9PShellBasicOverlays, val name: String, params: UARTOverlayParams)
  extends UARTOverlay(params, true)
{
  shell { InModuleBody {
    // instantiate UART transceiver and connect FIFO to one of the AXIs
    // probably want to use sifive-blocks/src/main/scala/devices/uart
    // or figure out what PlatformIO is and use that instead
  } }
}

// connects to Amazon's JTAG
class AmazonJTAGDebugF1VU9POverlay(val shell: F1VU9PShellBasicOverlays, val name: String, params: JTAGDebugOverlayParams)
  extends JTAGDebugOverlay(params)
{
  val debugBridge = LazyModule(new cl_debug_bridge)
  val ioNode = BundleBridgeSource(() => debugBridge.module.io.cloneType)
  val topIONode = shell { ioNode.makeSink() }
  InModuleBody { ioNode.bundle <> debugBridge.module.io }

  shell { InModuleBody {
    io <> topIONode.bundle.port
  } }
}
*/
// connects to our JTAG
class JTAGF1VU9POverlay(val shell: F1VU9PShellBasicOverlays, val name: String, params: JTAGDebugOverlayParams)
  extends JTAGDebugOverlay(params)
{
  
}

case object F1VU9PDDRSize extends Field[BigInt](0x400000000L * 1) // 16 GiB (in bytes)
class DisableDDRF1VU9POverlay(val shell: F1VU9PShellBasicOverlays, val name: String, params: DDROverlayParams)
  extends DDROverlay[XilinxF1VU9PDDRPads](params)
{
  val size = p(F1VU9PDDRSize)
	val ddrParams = XilinxF1VU9PDDRParams(addresses = Seq(AddressSet.misaligned(params.baseAddress, size),
                                                        AddressSet.misaligned(params.baseAddress + size, size),
                                                        AddressSet.misaligned(params.baseAddress + 3 * size, size)), // 3x because we want RAM D to be after C
                                        instantiate = Seq(false, false, false))
  val ddr = LazyModule(new XilinxF1VU9PDDR(ddrParams))
  val ioNode = BundleBridgeSource(() => ddr.module.io.cloneType)
  val topIONode = shell { ioNode.makeSink() }
  
  // implement abstract methods from Overlay and IOOverlay
  def designOutput = ddr.node
  def ioFactory = new XilinxF1VU9PDDRPads

  InModuleBody { ioNode.bundle <> ddr.module.io }

  shell { InModuleBody {
    require (shell.clk_main_a0.isDefined, "Use of DisableDDRF1VU9PPOverlay depends on SysClockF1VU9POverlay")
    val (sys, _) = shell.clk_main_a0.get.node.out(0)
    val port = topIONode.bundle.port
    io <> port
    port.clk := sys.clock.asUInt
    port.stat_clk := sys.clock.asUInt
    port.rst_n := !sys.reset
    port.stat_rst_n := sys.clock.asUInt // kinda weird but that's what amazon did for their example

    //port.cl_sh_ddr_awburst := Vec(1.U(2.W), 1.U(2.W), 1.U(2.W))
    //port.cl_sh_ddr_arburst := Vec(1.U(2.W), 1.U(2.W), 1.U(2.W))
    port.ddr_sh_stat_ack0 := 1.U(1.W)
    port.ddr_sh_stat_ack1 := 1.U(1.W)
    port.ddr_sh_stat_ack2 := 1.U(1.W)
  } }
}

// don't need to extend UltraScaleShell or XilinxShell since we don't need a PLL nor do we have any xdc/sdc constraints
abstract class F1VU9PShellBasicOverlays()(implicit p: Parameters) extends UltraScaleShell{

  val clk_main_a0       = Overlay(ClockInputOverlayKey) (new SysClockF1VU9POverlay    (_, _, _))
  val cl_sh_status_vled = Overlay(LEDOverlayKey)        (new LEDF1VU9POverlay         (_, _, _))
  val sh_cl_status_vdip = Overlay(SwitchOverlayKey)     (new SwitchF1VU9POverlay      (_, _, _))
  //val uart            = Overlay(UARTOverlayKey)       (new UARTF1VU9POverlay        (_, _, _))
  val ddr_overlay       = Overlay(DDROverlayKey)        (new DisableDDRF1VU9POverlay  (_, _, _))
  // select which JTAG to use by choosing which subclass of BasicOverlays is implemented
  val jtag              = Overlay(JTAGDebugOverlayKey)  (new JTAGF1VU9POverlay   (_, _, _))
}

class F1VU9PShell()(implicit p: Parameters) extends F1VU9PShellBasicOverlays
{
  val pllReset = InModuleBody { Wire(Bool()) }
  val topDesign = LazyModule(p(DesignKey)(designParameters))
  
  // Place the sys_clock at the Shell if the user didn't ask for it
  designParameters(ClockInputOverlayKey).foreach { unused =>
    val source = unused(ClockInputOverlayParams())
    val sink = ClockSinkNode(Seq(ClockSinkParameters()))
    sink := source
  }
  


  override lazy val module = new LazyRawModuleImp(this) {
    // all connections specified by F1 Shell
    // ports that are used by our shell are commented out
  //val clk_main_a0            = IO(Input(Bool()))
    val clk_extra_a1           = IO(Input(Bool()))
    val clk_extra_a2           = IO(Input(Bool()))
    val clk_extra_a3           = IO(Input(Bool()))
    val clk_extra_b0           = IO(Input(Bool()))
    val clk_extra_b1           = IO(Input(Bool()))
    val clk_extra_c0           = IO(Input(Bool()))
    val clk_extra_c1           = IO(Input(Bool()))
    val kernel_rst_n           = IO(Input(Bool()))
    val rst_main_n             = IO(Input(Bool()))
    val sh_cl_flr_assert       = IO(Input(Bool()))
    val cl_sh_flr_done         = IO(Output(Bool()))
    val cl_sh_status0          = IO(Output(UInt(32.W)))
    val cl_sh_status1          = IO(Output(UInt(32.W)))
    val cl_sh_id0              = IO(Output(UInt(32.W)))
    val cl_sh_id1              = IO(Output(UInt(32.W)))
    val sh_cl_ctl0             = IO(Input(UInt(32.W)))
    val sh_cl_ctl1             = IO(Input(UInt(32.W)))
  //val sh_cl_status_vdip      = IO(Input(UInt(16.W)))
  //val cl_sh_status_vled      = IO(Output(UInt(16.W)))
    val sh_cl_pwr_state        = IO(Input(UInt(2.W)))
    val cl_sh_dma_wr_full      = IO(Output(Bool()))
    val cl_sh_dma_rd_full      = IO(Output(Bool()))
    val cl_sh_pcim_awid        = IO(Output(UInt(16.W)))
    val cl_sh_pcim_awaddr      = IO(Output(UInt(64.W)))
    val cl_sh_pcim_awlen       = IO(Output(UInt(8.W)))
    val cl_sh_pcim_awsize      = IO(Output(UInt(3.W)))
    val cl_sh_pcim_awuser      = IO(Output(UInt(19.W)))
    val cl_sh_pcim_awvalid     = IO(Output(Bool()))
    val sh_cl_pcim_awready     = IO(Input(Bool()))
    val cl_sh_pcim_wdata       = IO(Output(UInt(512.W)))
    val cl_sh_pcim_wstrb       = IO(Output(UInt(64.W)))
    val cl_sh_pcim_wlast       = IO(Output(Bool()))
    val cl_sh_pcim_wvalid      = IO(Output(Bool()))
    val sh_cl_pcim_wready      = IO(Input(Bool()))
    val sh_cl_pcim_bid         = IO(Input(UInt(16.W)))
    val sh_cl_pcim_bresp       = IO(Input(UInt(2.W)))
    val sh_cl_pcim_bvalid      = IO(Input(Bool()))
    val cl_sh_pcim_bready      = IO(Output(Bool()))
    val cl_sh_pcim_arid        = IO(Output(UInt(16.W)))
    val cl_sh_pcim_araddr      = IO(Output(UInt(64.W)))
    val cl_sh_pcim_arlen       = IO(Output(UInt(8.W)))
    val cl_sh_pcim_arsize      = IO(Output(UInt(3.W)))
    val cl_sh_pcim_aruser      = IO(Output(UInt(19.W)))
    val cl_sh_pcim_arvalid     = IO(Output(Bool()))
    val sh_cl_pcim_arready     = IO(Input(Bool()))
    val sh_cl_pcim_rid         = IO(Input(UInt(16.W)))
    val sh_cl_pcim_rdata       = IO(Input(UInt(512.W)))
    val sh_cl_pcim_rresp       = IO(Input(UInt(2.W)))
    val sh_cl_pcim_rlast       = IO(Input(Bool()))
    val sh_cl_pcim_rvalid      = IO(Input(Bool()))
    val cl_sh_pcim_rready      = IO(Output(Bool()))
    val cfg_max_payload        = IO(Input(UInt(2.W)))
    val cfg_max_read_req       = IO(Input(UInt(3.W)))
    /*val CLK_300M_DIMM0_DP      = IO(Input(Bool()))
    val CLK_300M_DIMM0_DN      = IO(Input(Bool()))
    val M_A_ACT_N              = IO(Output(Bool()))
    val M_A_MA                 = IO(Output(UInt(17.W)))
    val M_A_BA                 = IO(Output(UInt(2.W)))
    val M_A_BG                 = IO(Output(UInt(2.W)))
    val M_A_CKE                = IO(Output(Bool()))
    val M_A_ODT                = IO(Output(Bool()))
    val M_A_CS_N               = IO(Output(Bool()))
    val M_A_CLK_DN             = IO(Output(Bool()))
    val M_A_CLK_DP             = IO(Output(Bool()))
    val M_A_PAR                = IO(Output(Bool()))
    val M_A_DQ                 = IO(Analog(64.W))
    val M_A_ECC                = IO(Analog(8.W))
    val M_A_DQS_DP             = IO(Analog(18.W))
    val M_A_DQS_DN             = IO(Analog(18.W))
    val cl_RST_DIMM_A_N        = IO(Output(Bool()))
    val CLK_300M_DIMM1_DP      = IO(Input(Bool()))
    val CLK_300M_DIMM1_DN      = IO(Input(Bool()))
    val M_B_ACT_N              = IO(Output(Bool()))
    val M_B_MA                 = IO(Output(UInt(17.W)))
    val M_B_BA                 = IO(Output(UInt(2.W)))
    val M_B_BG                 = IO(Output(UInt(2.W)))
    val M_B_CKE                = IO(Output(Bool()))
    val M_B_ODT                = IO(Output(Bool()))
    val M_B_CS_N               = IO(Output(Bool()))
    val M_B_CLK_DN             = IO(Output(Bool()))
    val M_B_CLK_DP             = IO(Output(Bool()))
    val M_B_PAR                = IO(Output(Bool()))
    val M_B_DQ                 = IO(Analog(64.W))
    val M_B_ECC                = IO(Analog(8.W))
    val M_B_DQS_DP             = IO(Analog(18.W))
    val M_B_DQS_DN             = IO(Analog(18.W))
    val cl_RST_DIMM_B_N        = IO(Output(Bool()))
    val CLK_300M_DIMM3_DP      = IO(Input(Bool()))
    val CLK_300M_DIMM3_DN      = IO(Input(Bool()))
    val M_D_ACT_N              = IO(Output(Bool()))
    val M_D_MA                 = IO(Output(UInt(17.W)))
    val M_D_BA                 = IO(Output(UInt(2.W)))
    val M_D_BG                 = IO(Output(UInt(2.W)))
    val M_D_CKE                = IO(Output(Bool()))
    val M_D_ODT                = IO(Output(Bool()))
    val M_D_CS_N               = IO(Output(Bool()))
    val M_D_CLK_DN             = IO(Output(Bool()))
    val M_D_CLK_DP             = IO(Output(Bool()))
    val M_D_PAR                = IO(Output(Bool()))
    val M_D_DQ                 = IO(Analog(64.W))
    val M_D_ECC                = IO(Analog(8.W))
    val M_D_DQS_DP             = IO(Analog(18.W))
    val M_D_DQS_DN             = IO(Analog(18.W))
    val cl_RST_DIMM_D_N        = IO(Output(Bool()))
    val sh_ddr_stat_addr0      = IO(Input(UInt(8.W)))
    val sh_ddr_stat_wr0        = IO(Input(Bool()))
    val sh_ddr_stat_rd0        = IO(Input(Bool()))
    val sh_ddr_stat_wdata0     = IO(Input(UInt(32.W)))
    val ddr_sh_stat_ack0       = IO(Output(Bool()))
    val ddr_sh_stat_rdata0     = IO(Output(UInt(32.W)))
    val ddr_sh_stat_int0       = IO(Output(UInt(8.W)))
    val sh_ddr_stat_addr1      = IO(Input(UInt(8.W)))
    val sh_ddr_stat_wr1        = IO(Input(Bool()))
    val sh_ddr_stat_rd1        = IO(Input(Bool()))
    val sh_ddr_stat_wdata1     = IO(Input(UInt(32.W)))
    val ddr_sh_stat_ack1       = IO(Output(Bool()))
    val ddr_sh_stat_rdata1     = IO(Output(UInt(32.W)))
    val ddr_sh_stat_int1       = IO(Output(UInt(8.W)))
    val sh_ddr_stat_addr2      = IO(Input(UInt(8.W)))
    val sh_ddr_stat_wr2        = IO(Input(Bool()))
    val sh_ddr_stat_rd2        = IO(Input(Bool()))
    val sh_ddr_stat_wdata2     = IO(Input(UInt(32.W)))
    val ddr_sh_stat_ack2       = IO(Output(Bool()))
    val ddr_sh_stat_rdata2     = IO(Output(UInt(32.W)))
    val ddr_sh_stat_int2       = IO(Output(UInt(8.W)))
    val cl_sh_ddr_awid         = IO(Output(UInt(16.W)))
    val cl_sh_ddr_awaddr       = IO(Output(UInt(64.W)))
    val cl_sh_ddr_awlen        = IO(Output(UInt(8.W)))
    val cl_sh_ddr_awsize       = IO(Output(UInt(3.W)))
    val cl_sh_ddr_awburst      = IO(Output(UInt(2.W)))
    val cl_sh_ddr_awvalid      = IO(Output(Bool()))
    val sh_cl_ddr_awready      = IO(Input(Bool()))
    val cl_sh_ddr_wid          = IO(Output(UInt(16.W)))
    val cl_sh_ddr_wdata        = IO(Output(UInt(512.W)))
    val cl_sh_ddr_wstrb        = IO(Output(UInt(64.W)))
    val cl_sh_ddr_wlast        = IO(Output(Bool()))
    val cl_sh_ddr_wvalid       = IO(Output(Bool()))
    val sh_cl_ddr_wready       = IO(Input(Bool()))
    val sh_cl_ddr_bid          = IO(Input(UInt(16.W)))
    val sh_cl_ddr_bresp        = IO(Input(UInt(2.W)))
    val sh_cl_ddr_bvalid       = IO(Input(Bool()))
    val cl_sh_ddr_bready       = IO(Output(Bool()))
    val cl_sh_ddr_arid         = IO(Output(UInt(16.W)))
    val cl_sh_ddr_araddr       = IO(Output(UInt(64.W)))
    val cl_sh_ddr_arlen        = IO(Output(UInt(8.W)))
    val cl_sh_ddr_arsize       = IO(Output(UInt(3.W)))
    val cl_sh_ddr_arburst      = IO(Output(UInt(2.W)))
    val cl_sh_ddr_arvalid      = IO(Output(Bool()))
    val sh_cl_ddr_arready      = IO(Input(Bool()))
    val sh_cl_ddr_rid          = IO(Input(UInt(16.W)))
    val sh_cl_ddr_rdata        = IO(Input(UInt(512.W)))
    val sh_cl_ddr_rresp        = IO(Input(UInt(2.W)))
    val sh_cl_ddr_rlast        = IO(Input(Bool()))
    val sh_cl_ddr_rvalid       = IO(Input(Bool()))
    val cl_sh_ddr_rready       = IO(Output(Bool()))
    val sh_cl_ddr_is_ready     = IO(Input(Bool()))*/
    val cl_sh_apppf_irq_req    = IO(Output(UInt(16.W)))
    val sh_cl_apppf_irq_ack    = IO(Input(UInt(16.W)))
    val sh_cl_dma_pcis_awid    = IO(Input(UInt(6.W)))
    val sh_cl_dma_pcis_awaddr  = IO(Input(UInt(64.W)))
    val sh_cl_dma_pcis_awlen   = IO(Input(UInt(8.W)))
    val sh_cl_dma_pcis_awsize  = IO(Input(UInt(3.W)))
    val sh_cl_dma_pcis_awvalid = IO(Input(Bool()))
    val cl_sh_dma_pcis_awready = IO(Output(Bool()))
    val sh_cl_dma_pcis_wdata   = IO(Input(UInt(512.W)))
    val sh_cl_dma_pcis_wstrb   = IO(Input(UInt(64.W)))
    val sh_cl_dma_pcis_wlast   = IO(Input(Bool()))
    val sh_cl_dma_pcis_wvalid  = IO(Input(Bool()))
    val cl_sh_dma_pcis_wready  = IO(Output(Bool()))
    val cl_sh_dma_pcis_bid     = IO(Output(UInt(6.W)))
    val cl_sh_dma_pcis_bresp   = IO(Output(UInt(2.W)))
    val cl_sh_dma_pcis_bvalid  = IO(Output(Bool()))
    val sh_cl_dma_pcis_bready  = IO(Input(Bool()))
    val sh_cl_dma_pcis_arid    = IO(Input(UInt(6.W)))
    val sh_cl_dma_pcis_araddr  = IO(Input(UInt(64.W)))
    val sh_cl_dma_pcis_arlen   = IO(Input(UInt(8.W)))
    val sh_cl_dma_pcis_arsize  = IO(Input(UInt(3.W)))
    val sh_cl_dma_pcis_arvalid = IO(Input(Bool()))
    val cl_sh_dma_pcis_arready = IO(Output(Bool()))
    val cl_sh_dma_pcis_rid     = IO(Output(UInt(6.W)))
    val cl_sh_dma_pcis_rdata   = IO(Output(UInt(512.W)))
    val cl_sh_dma_pcis_rresp   = IO(Output(UInt(2.W)))
    val cl_sh_dma_pcis_rlast   = IO(Output(Bool()))
    val cl_sh_dma_pcis_rvalid  = IO(Output(Bool()))
    val sh_cl_dma_pcis_rready  = IO(Input(Bool()))
    val sda_cl_awvalid         = IO(Input(Bool()))
    val sda_cl_awaddr          = IO(Input(UInt(32.W)))
    val cl_sda_awready         = IO(Output(Bool()))
    val sda_cl_wvalid          = IO(Input(Bool()))
    val sda_cl_wdata           = IO(Input(UInt(32.W)))
    val sda_cl_wstrb           = IO(Input(UInt(4.W)))
    val cl_sda_wready          = IO(Output(Bool()))
    val cl_sda_bvalid          = IO(Output(Bool()))
    val cl_sda_bresp           = IO(Output(UInt(2.W)))
    val sda_cl_bready          = IO(Input(Bool()))
    val sda_cl_arvalid         = IO(Input(Bool()))
    val sda_cl_araddr          = IO(Input(UInt(32.W)))
    val cl_sda_arready         = IO(Output(Bool()))
    val cl_sda_rvalid          = IO(Output(Bool()))
    val cl_sda_rdata           = IO(Output(UInt(32.W)))
    val cl_sda_rresp           = IO(Output(UInt(2.W)))
    val sda_cl_rready          = IO(Input(Bool()))
    val sh_ocl_awvalid         = IO(Input(Bool()))
    val sh_ocl_awaddr          = IO(Input(UInt(32.W)))
    val ocl_sh_awready         = IO(Output(Bool()))
    val sh_ocl_wvalid          = IO(Input(Bool()))
    val sh_ocl_wdata           = IO(Input(UInt(32.W)))
    val sh_ocl_wstrb           = IO(Input(UInt(4.W)))
    val ocl_sh_wready          = IO(Output(Bool()))
    val ocl_sh_bvalid          = IO(Output(Bool()))
    val ocl_sh_bresp           = IO(Output(UInt(2.W)))
    val sh_ocl_bready          = IO(Input(Bool()))
    val sh_ocl_arvalid         = IO(Input(Bool()))
    val sh_ocl_araddr          = IO(Input(UInt(32.W)))
    val ocl_sh_arready         = IO(Output(Bool()))
    val ocl_sh_rvalid          = IO(Output(Bool()))
    val ocl_sh_rdata           = IO(Output(UInt(32.W)))
    val ocl_sh_rresp           = IO(Output(UInt(2.W)))
    val sh_ocl_rready          = IO(Input(Bool()))
    val sh_bar1_awvalid        = IO(Input(Bool()))
    val sh_bar1_awaddr         = IO(Input(UInt(32.W)))
    val bar1_sh_awready        = IO(Output(Bool()))
    val sh_bar1_wvalid         = IO(Input(Bool()))
    val sh_bar1_wdata          = IO(Input(UInt(32.W)))
    val sh_bar1_wstrb          = IO(Input(UInt(4.W)))
    val bar1_sh_wready         = IO(Output(Bool()))
    val bar1_sh_bvalid         = IO(Output(Bool()))
    val bar1_sh_bresp          = IO(Output(UInt(2.W)))
    val sh_bar1_bready         = IO(Input(Bool()))
    val sh_bar1_arvalid        = IO(Input(Bool()))
    val sh_bar1_araddr         = IO(Input(UInt(32.W)))
    val bar1_sh_arready        = IO(Output(Bool()))
    val bar1_sh_rvalid         = IO(Output(Bool()))
    val bar1_sh_rdata          = IO(Output(UInt(32.W)))
    val bar1_sh_rresp          = IO(Output(UInt(2.W)))
    val sh_bar1_rready         = IO(Input(Bool()))
    val drck                   = IO(Input(Bool()))
    val shift                  = IO(Input(Bool()))
    val tdi                    = IO(Input(Bool()))
    val update                 = IO(Input(Bool()))
    val sel                    = IO(Input(Bool()))
    val tdo                    = IO(Output(Bool()))
    val tms                    = IO(Input(Bool()))
    val tck                    = IO(Input(Bool()))
    val runtest                = IO(Input(Bool()))
    val reset                  = IO(Input(Bool()))
    val capture                = IO(Input(Bool()))
    val bscanid_en             = IO(Input(Bool()))
    val sh_cl_glcount0         = IO(Input(UInt(64.W)))
    val sh_cl_glcount1         = IO(Input(UInt(64.W)))
    
    // connect pllReset
    // unclear if it's necessary to synchronize rst_main_n with clk_main_a0
    // example synchronizes it but says it's already synchronous with clk_main_a0
    pllReset := !rst_main_n
    
    // drive non-zero constant outputs (e.g. PCIe ID#)
    // default PCIe IDs used by cl_hello_world
    cl_sh_id0 := "h_f000_1d0f".U
    cl_sh_id1 := "h_1d51_fedd".U
    
  }
}
