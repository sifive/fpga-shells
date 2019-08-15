// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import scala.collection.immutable.ListMap
import chisel3._
import chisel3.core.ActualDirection
import chisel3.experimental.{attach, Analog, IO, withClockAndReset, DataMirror}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.SyncResetSynchronizerShiftReg
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.devices.xilinx.xilinxf1vu9pddr._
import sifive.fpgashells.ip.xilinx.f1vu9pddr.{F1VU9PDDRPads, F1VU9PDDRBase, F1VU9PDDRIO, F1VU9PAXISignals}
import sifive.fpgashells.devices.xilinx.xilinxf1vu9paxi4pcis._
import sifive.fpgashells.shell.AXI4PCISOverlayKey

// EmptyBundle and connectSink are used to produce custom-named IO at the shell level
// this prevents Chisel from inserting overlayname_ before each IO element, which is necessary for integration with AWS's F1
class EmptyBundle extends Bundle

// creates toplevel IO for each element of sink bundle and connects it to the sink
// optional blacklist to prevent creation of ports that shouldn't be punched through to toplevel IO
object connectSink {
  def apply[T <: Bundle](sink: T, blacklist: String => Boolean = _ => false) = {
    for ( (name, data) <- sink.elements) {
      if (!blacklist(name)) {
        DataMirror.directionOf(data) match {
          case ActualDirection.Input =>
            val port = IO(Input(chiselTypeOf(data))).suggestName(name)
            data := port
          case ActualDirection.Output =>
            val port = IO(Output(chiselTypeOf(data))).suggestName(name)
            port := data
          case ActualDirection.Unspecified =>
            val port = IO(Analog(data.getWidth.W)).suggestName(name)
            port <> data
        }
      }
    }
  }
}

//------------------
// Overlays
//------------------

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
  extends IOOverlay[EmptyBundle, VirtualUART]
{
  implicit val p = params.p

  def ioFactory = new EmptyBundle
  val designOutput 
  shell { InModuleBody {
    
  } }
}*/

// need to create our own JTAGOverlay for F1
class JTAGF1VU9POverlay(val shell: F1VU9PShellBasicOverlays, val name: String, params: JTAGDebugOverlayParams)
  extends IOOverlay[EmptyBundle, ModuleValue[FPGAJTAGIO]]
{
  implicit val p = params.p
  
  val jtagSource = BundleBridgeSource(() => new FPGAJTAGIO)
  val jtagSink = shell { jtagSource.makeSink() }
  
  def ioFactory = new EmptyBundle
  val designOutput = InModuleBody { jtagSource.bundle }

  shell { InModuleBody {
    val tck = IO(Input(Clock()))  suggestName "tck"
    val tms = IO(Input(Bool()))   suggestName "tms"
    val tdi = IO(Input(Bool()))   suggestName "tdi"
    val tdo = IO(Output(Bool()))  suggestName "tdo"

    val jt = jtagSink.bundle
    jt.jtag_TCK := tck
    jt.jtag_TMS := tms
    jt.jtag_TDI := tdi
    tdo := jt.jtag_TDO
  } }
}

class AXI4PCISF1VU9POverlay(val shell: F1VU9PShellBasicOverlays, val name: String, params: AXI4PCISOverlayParams)
  extends IOOverlay[EmptyBundle, TLOutwardNode]
{
  implicit val p = params.p
  val axi4pcisParams = AXI4PCISParams(name = "", mIDBits = 0, busBytes = 8)
  val axi4pcis = LazyModule(new XilinxF1VU9PAXI4PCIS(axi4pcisParams))
  val ioSource = BundleBridgeSource(() => new AXI4PCISPads)
  val ioSink = shell { ioSource.makeSink() }

  def designOutput = axi4pcis.node
  def ioFactory = new EmptyBundle

  InModuleBody { ioSource.bundle <> axi4pcis.module.io }

  shell { InModuleBody {
    connectSink(ioSink.bundle)
  } }
}


// each DDR chip is 16GiB
case object F1VU9PDDRSize extends Field[BigInt](0x40000000L * 16) // 16 GiB (in bytes) --- 0x40000000L is 1 Gi

// parameterizes DDROverlay with EmptyBundle to allow for manual creation of toplevel IO so we have control over naming
class DDRF1VU9POverlay(val shell: F1VU9PShellBasicOverlays, val name: String, params: DDROverlayParams)
  extends DDROverlay[EmptyBundle](params)
{
  val size = p(F1VU9PDDRSize)
	// create ddr module (wrapper for .v IP)
  val ddrParams = XilinxF1VU9PDDRParams(addresses = Seq(AddressSet.misaligned(params.baseAddress, size),
                                                      AddressSet.misaligned(params.baseAddress + size, size),
                                                      AddressSet.misaligned(params.baseAddress + 3*size, size)),
                                        instantiate = Seq(true, true, true))
  val ddr = LazyModule(new XilinxF1VU9PDDR(ddrParams))

  val ddrDirectionedSource = BundleBridgeSource(() => new F1VU9PDDRBase)
  val ddrAnalogSource = BundleBridgeSource(() => new Bundle with F1VU9PDDRIO)

  // use our own makeSink implementation here to get proper portnames
  val ddrDirectionedSink = shell { ddrDirectionedSource.makeSink() }
  val ddrAnalogSink = shell { ddrAnalogSource.makeSink() }
  
  // implement abstract methods from Overlay and IOOverlay
  def designOutput = ddr.node 
  def ioFactory = new EmptyBundle
  
  // connect up ddr (wrapper) to bundlebridgesource
  InModuleBody { 
    ddrDirectionedSource.bundle <> ddr.module.io.directioned
    ddrAnalogSource.bundle <> ddr.module.io.analog
  }
  
  // connect up toplevel IO to bundlebridgesink
  shell { InModuleBody {
    require (shell.clk_main_a0.isDefined, "Use of DisableDDRF1VU9PPOverlay depends on SysClockF1VU9POverlay")
    val (sys, _) = shell.clk_main_a0.get.node.out(0)
    val directioned = ddrDirectionedSink.bundle
    val analog = ddrAnalogSink.bundle
   
    // create toplevel IO and connect it up to sinks
    connectSink(analog)
    connectSink(directioned, name => name match {
      case "clk" => true
      case "stat_clk" => true
      case "rst_n" => true
      case "stat_rst_n" => true
      case _ => false
    }) // match function creates a "set" of blacklisted names; we don't want to create IOs for these

    directioned.clk := sys.clock.asUInt
    directioned.stat_clk := sys.clock.asUInt
    directioned.rst_n := !sys.reset
    directioned.stat_rst_n := sys.clock.asUInt // kinda weird but that's what amazon did for their example

  } }
}

abstract class F1VU9PShellBasicOverlays()(implicit p: Parameters) extends UltraScaleShell{

  val clk_main_a0       = Overlay(ClockInputOverlayKey) (new SysClockF1VU9POverlay  (_, _, _))
  val cl_sh_status_vled = Overlay(LEDOverlayKey)        (new LEDF1VU9POverlay       (_, _, _))
  val sh_cl_status_vdip = Overlay(SwitchOverlayKey)     (new SwitchF1VU9POverlay    (_, _, _))
//val uart              = Overlay(UARTOverlayKey)       (new UARTF1VU9POverlay      (_, _, _))
  val ddr               = Overlay(DDROverlayKey)        (new DDRF1VU9POverlay       (_, _, _))
  val jtag              = Overlay(JTAGDebugOverlayKey)  (new JTAGF1VU9POverlay      (_, _, _))
  val axi4pcis          = Overlay(AXI4PCISOverlayKey)   (new AXI4PCISF1VU9POverlay  (_, _, _))
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
    
    val rst_main_n = IO(Input(Bool()))
    val cl_sh_id0  = IO(Output(UInt(32.W)))
    val cl_sh_id1  = IO(Output(UInt(32.W)))
    
    // connect pllReset
    // unclear if it's necessary to synchronize rst_main_n with clk_main_a0
    // example synchronizes it but says it's already synchronous with clk_main_a0
    pllReset := !rst_main_n

    // default PCIe IDs used by cl_hello_world
    cl_sh_id0 := "h_f000_1d0f".U
    cl_sh_id1 := "h_1d51_fedd".U
    
    def blacklist(name: String): Boolean = {
      // any module that uses IOs will need to add a blacklist
      if ( (new F1VU9PDDRPads).elements.contains(name) ) {
        true
      } else if ( (new AXI4PCISPads).elements.contains(name) ) {
        true
      } else {
        name match {
          // comment these lines out if vLEDs and/or vDIPs aren't used
          case "cl_sh_status_vled" => true
          case "sh_cl_status_vdip" => true
          // comment these lines out if JTAG is not being used
          case "tck" => true 
          case "tms" => true
          case "tdi" => true
          case "tdo" => true
          // these are required
          case "clk_main_a0" => true
          case "rst_main_n" => true
          case "cl_sh_id0" => true
          case "cl_sh_id1" => true
          case _ => false
        }
      }
    }
    
    // loop over elements in CLPorts, creating IO for non blacklisted elements
    for ( (name, direction, htype, width) <- CLPorts.elements) {
      val hw: Data = htype match {
        case "B" => Bool()
        case "U" => UInt(width.W)
        case "A" => Analog(width.W)
        case _ => throw new Exception("unknown hwtype")
      }
      if (!blacklist(name)) {
        direction match {
          case "I" =>
            val port = IO(Input(hw)).suggestName(name)
          case "O" =>
            val port = IO(Output(hw)).suggestName(name)
            // tie down DDR-C non-zero outputs
            if (name == "cl_sh_ddr_awburst" || name == "cl_sh_ddr_arburst") {
              port := 1.U(2.W)
            }
          case "U" =>
            val port = IO(hw).suggestName(name)
        }
      }
    }
  }
}

