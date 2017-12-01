// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xilinxvcu118pciex4

import Chisel._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.coreplex.CacheBlockBytes
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.coreplex.{HasCrossing,AsynchronousCrossing}
import sifive.fpgashells.ip.xilinx.vcu118pcie_axi_bridge_x4.{VCU118PCIeAXIBridgeX4, VCU118PCIeAXIBridgeX4IOClocksReset, VCU118PCIeAXIBridgeX4IOSerial,EDGE,FMCP}
import sifive.fpgashells.ip.xilinx.ibufds_gte4.IBUFDS_GTE4

trait VC707AXIToPCIeRefClk extends Bundle{
  val REFCLK_rxp = Bool(INPUT)
  val REFCLK_rxn = Bool(INPUT)
}

class XilinxVCU118PCIeX4Pads extends Bundle 
  with VCU118PCIeAXIBridgeX4IOSerial
  with VC707AXIToPCIeRefClk

class XilinxVCU118PCIeX4IO extends Bundle
    with VC707AXIToPCIeRefClk
    with VCU118PCIeAXIBridgeX4IOSerial
    with VCU118PCIeAXIBridgeX4IOClocksReset 


class XilinxVCU118PCIeX4(implicit p: Parameters) extends LazyModule with HasCrossing {
  val crossing = AsynchronousCrossing(8)
  val vcu118pcie_axi_bridge_x4 = LazyModule(new VCU118PCIeAXIBridgeX4(FMCP))

  val slave: TLInwardNode =
    (vcu118pcie_axi_bridge_x4.slave
      := AXI4Buffer()
      := AXI4UserYanker()
      := AXI4Deinterleaver(p(CacheBlockBytes))
      := AXI4IdIndexer(idBits=4)
      := TLToAXI4(adapterName = Some("pcie-slave")))

  val control: TLInwardNode =
    (vcu118pcie_axi_bridge_x4.control
      := AXI4Buffer()
      := AXI4UserYanker(capMaxFlight = Some(2))
      := TLToAXI4()
      := TLFragmenter(4, p(CacheBlockBytes)))

  val master: TLOutwardNode =
    (TLWidthWidget(8)
      := AXI4ToTL()
      := AXI4UserYanker(capMaxFlight=Some(8))
      := AXI4Fragmenter()
      := vcu118pcie_axi_bridge_x4.master)

  val intnode: IntOutwardNode = vcu118pcie_axi_bridge_x4.intnode

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val port = new XilinxVCU118PCIeX4IO
    })

    io.port <> vcu118pcie_axi_bridge_x4.module.io.port

    //PCIe Reference Clock
    val ibufds_gte4 = Module(new IBUFDS_GTE4)
    vcu118pcie_axi_bridge_x4.module.io.sys_clk_gt := ibufds_gte4.io.O
    vcu118pcie_axi_bridge_x4.module.io.sys_clk := ibufds_gte4.io.ODIV2
    ibufds_gte4.io.CEB := UInt(0)
    ibufds_gte4.io.I := io.port.REFCLK_rxp
    ibufds_gte4.io.IB := io.port.REFCLK_rxn
  }
}
