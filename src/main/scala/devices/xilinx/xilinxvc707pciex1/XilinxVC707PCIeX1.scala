// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvc707pciex1

import Chisel._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.subsystem.{CrossesToOnlyOneClockDomain, CacheBlockBytes}
import sifive.fpgashells.ip.xilinx.vc707axi_to_pcie_x1.{VC707AXIToPCIeX1, VC707AXIToPCIeX1IOClocksReset, VC707AXIToPCIeX1IOSerial}
import sifive.fpgashells.ip.xilinx.ibufds_gte2.IBUFDS_GTE2

trait VC707AXIToPCIeRefClk extends Bundle{
  val REFCLK_rxp = Bool(INPUT)
  val REFCLK_rxn = Bool(INPUT)
}

class XilinxVC707PCIeX1Pads extends Bundle 
  with VC707AXIToPCIeX1IOSerial
  with VC707AXIToPCIeRefClk

class XilinxVC707PCIeX1IO extends Bundle
    with VC707AXIToPCIeRefClk
    with VC707AXIToPCIeX1IOSerial
    with VC707AXIToPCIeX1IOClocksReset {
  val axi_ctl_aresetn = Bool(INPUT)
}

class XilinxVC707PCIeX1(implicit p: Parameters, val crossing: ClockCrossingType = AsynchronousCrossing(8))
  extends LazyModule with CrossesToOnlyOneClockDomain
{
  val axi_to_pcie_x1 = LazyModule(new VC707AXIToPCIeX1)

  val slave: TLInwardNode =
    (axi_to_pcie_x1.slave
      := AXI4Buffer()
      := AXI4UserYanker()
      := AXI4Deinterleaver(p(CacheBlockBytes))
      := AXI4IdIndexer(idBits=4)
      := TLToAXI4(adapterName = Some("pcie-slave")))

  val control: TLInwardNode =
    (axi_to_pcie_x1.control
      := AXI4Buffer()
      := AXI4UserYanker(capMaxFlight = Some(2))
      := TLToAXI4()
      := TLFragmenter(4, p(CacheBlockBytes), holdFirstDeny = true))

  val master: TLOutwardNode =
    (TLWidthWidget(8)
      := AXI4ToTL()
      := AXI4UserYanker(capMaxFlight=Some(8))
      := AXI4Fragmenter()
      := axi_to_pcie_x1.master)

  val intnode: IntOutwardNode = axi_to_pcie_x1.intnode

  lazy val module = new LazyRawModuleImp(this) {
    val io = IO(new Bundle {
      val port = new XilinxVC707PCIeX1IO
    })

    childClock := io.port.axi_aclk_out
    childReset := ~io.port.axi_aresetn

    io.port <> axi_to_pcie_x1.module.io.port

    //PCIe Reference Clock
    val ibufds_gte2 = Module(new IBUFDS_GTE2)
    axi_to_pcie_x1.module.io.REFCLK := ibufds_gte2.io.O
    ibufds_gte2.io.CEB := UInt(0)
    ibufds_gte2.io.I := io.port.REFCLK_rxp
    ibufds_gte2.io.IB := io.port.REFCLK_rxn
  }
}
