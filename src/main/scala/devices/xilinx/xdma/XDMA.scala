// See LICENSE for license details.
package sifive.fpgashells.devices.xilinx.xdma

import chisel3._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.subsystem.{CrossesToOnlyOneClockDomain, CacheBlockBytes}
import sifive.fpgashells.ip.xilinx.xdma._

class XDMA(c: XDMAParams)(implicit p: Parameters, val crossing: ClockCrossingType = AsynchronousCrossing(8))
  extends LazyModule with CrossesToOnlyOneClockDomain
{
  val imp = LazyModule(new DiplomaticXDMA(c))

  val slave: TLInwardNode =
    (imp.slave
      := AXI4Buffer()
      := AXI4UserYanker()
      := AXI4Deinterleaver(p(CacheBlockBytes))
      := AXI4IdIndexer(idBits=c.sIDBits)
      := TLToAXI4(adapterName = Some("pcie-slave")))

  val control: TLInwardNode =
    (imp.control
      := AXI4Buffer()
      := AXI4UserYanker(capMaxFlight = Some(2))
      := TLToAXI4()
      := TLFragmenter(4, p(CacheBlockBytes), holdFirstDeny = true))

  val master: TLOutwardNode =
    (TLWidthWidget(c.busBytes)
      := AXI4ToTL()
      := AXI4UserYanker(capMaxFlight=Some(16))
      := AXI4Fragmenter()
      := AXI4IdIndexer(idBits=2)
      := imp.master)

  val intnode: IntOutwardNode = imp.intnode

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val pads = new XDMAPads(c.lanes)
      val clocks = new XDMAClocks
    })

    io.pads   <> imp.module.io.pads
    io.clocks <> imp.module.io.clocks
  }
}
