// See LICENSE for license details.
package sifive.fpgashells.devices.microsemi.polarfireevalkitpciex4

import Chisel._
import freechips.rocketchip.amba.axi4._
//import freechips.rocketchip.coreplex.CacheBlockBytes
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.util._
import freechips.rocketchip.subsystem.{CrossesToOnlyOneClockDomain, CacheBlockBytes}

import sifive.fpgashells.ip.microsemi.polarfirepcierootport._

trait PolarFireEvalKitPCIeRefClk extends Bundle{
//TODO: bring reference clock connection in here
//  val REFCLK_rxp = Bool(INPUT)
//  val REFCLK_rxn = Bool(INPUT)
}

class PolarFireEvalKitPCIeX4Pads extends Bundle 
  with PolarFirePCIeIOSerial
  with PolarFireEvalKitPCIeRefClk

class PolarFireEvalKitPCIeX4IO extends Bundle
    with PolarFireEvalKitPCIeRefClk
    with PolarFirePCIeIOSerial
    with PolarFirePCIeIODebug
    with PolarFirePCIeIOClocksReset {
  val axi_ctl_aresetn = Bool(INPUT)
}

class PolarFireEvalKitPCIeX4(implicit p: Parameters) extends LazyModule with CrossesToOnlyOneClockDomain {
  val crossing = SynchronousCrossing()
  val axi_to_pcie = LazyModule(new PolarFirePCIeX4)

  val slave: TLInwardNode =
    (axi_to_pcie.slave
      := AXI4Buffer()
      := AXI4UserYanker()
      := AXI4Deinterleaver(p(CacheBlockBytes))
      := AXI4IdIndexer(idBits=4)
      := TLToAXI4(adapterName = Some("pcie-slave")))

  val control: TLInwardNode =
    (axi_to_pcie.control
      := TLToAPB(false)
      := TLBuffer()
      := TLFragmenter(4, p(CacheBlockBytes)))

  val master: TLOutwardNode =
    (TLWidthWidget(8)
      := AXI4ToTL()
      := AXI4UserYanker(capMaxFlight=Some(8))
      := AXI4Fragmenter()
      := AXI4IdIndexer(idBits=2)
      := AXI4Buffer()
      := axi_to_pcie.master)

  val TLScope = LazyModule(new SimpleLazyModule with LazyScope)
  val intnode: IntOutwardNode = IntSyncCrossingSink() := TLScope {
    IntSyncCrossingSource(alreadyRegistered = true) := axi_to_pcie.intnode
  }

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val port = new PolarFireEvalKitPCIeX4IO
    })

    io.port <> axi_to_pcie.module.io.port
    TLScope.module.clock := io.port.PCIE_1_TL_CLK_125MHz
    TLScope.module.reset := ResetCatchAndSync(io.port.PCIE_1_TL_CLK_125MHz, reset)
  }
}
