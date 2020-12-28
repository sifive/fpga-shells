package devices.intel

import Chisel._
import chisel3.core.withClock
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy.{AddressSet, DTB, LazyModule, LazyModuleImp, RegionType, SimpleDevice, TransferSizes}
import freechips.rocketchip.subsystem.{BaseSubsystem, ExtMem, MasterPortParams, MemoryPortParams}
import freechips.rocketchip.tilelink.{TLManagerNode, TLManagerParameters, TLManagerPortParameters, TLMessages}
import freechips.rocketchip.util._
import shell.intel.MemIfBundle
import sifive.fpgashells.ip.intel.{FIFO, ddr2_64bit}

class AltmemphyDDR2RAM(implicit p: Parameters) extends LazyModule {

  val MemoryPortParams(MasterPortParams(base, size, beatBytes, _, _, executable), 1) = p(ExtMem).get
  val node = TLManagerNode(Seq(TLManagerPortParameters(
    Seq(TLManagerParameters(
      address = AddressSet.misaligned(base, size),
      resources = new SimpleDevice("ram", Seq("sifive,altmemphy0")).reg("mem"),
      regionType = RegionType.UNCACHED,
      executable = executable,
      supportsGet = TransferSizes(1, 16),
      supportsPutFull = TransferSizes(1, 16),
      fifoId = Some(0)
    )),
    beatBytes = 16
  )))
  override lazy val module = new AltmemphyDDR2RAMImp(this)
}

class AltmemphyDDR2RAMImp(_outer: AltmemphyDDR2RAM)(implicit p: Parameters) extends LazyModuleImp(_outer) {
  val addrSize = log2Ceil(_outer.size / 16)

  val (in, edge) = _outer.node.in(0)
  val ddr2 = Module(new ddr2_64bit)
  require(ddr2.io.local_address.getWidth == addrSize)
  val tl_clock = clock
  val ddr_clock = ddr2.io.aux_full_rate_clk
  val mem_if = IO(new MemIfBundle)

  // tl_ prefixed hw are from CPU clock domain
  // ddr_ prefixed hw are from DDR2 controller clock domain

  class DdrRequest extends Bundle {
    val size = UInt(in.a.bits.size.widthOption.get.W)
    val source = UInt(in.a.bits.source.widthOption.get.W)
    val address = UInt(addrSize.W)
    val be = UInt(16.W)
    val wdata = UInt(128.W)
    val is_reading = Bool()
  }

  val tl_req = Wire(new DdrRequest)
  val ddr_req = Wire(new DdrRequest)
  val fifo_req = FIFO(2, ddr_req, ddr_clock, tl_req, clock, showahead = false)

  class DdrResponce extends Bundle {
    val is_reading = Bool()
    val size   = UInt(in.d.bits.size.widthOption.get.W)
    val source = UInt(in.d.bits.source.widthOption.get.W)
    val rdata = UInt(128.W)
  }

  val tl_resp = Wire(new DdrResponce)
  val ddr_resp = Wire(new DdrResponce)
  val fifo_resp = FIFO(2, tl_resp, clock, ddr_resp, ddr_clock, showahead = true)

  in.a.ready := !fifo_req.io.wrfull

  tl_req.size := in.a.bits.size
  tl_req.source := in.a.bits.source
  tl_req.address := edge.addr_hi(in.a.bits.address - _outer.base.U)(addrSize - 1, 0)
  tl_req.be := in.a.bits.mask
  tl_req.wdata := in.a.bits.data
  tl_req.is_reading := in.a.bits.opcode === TLMessages.Get

  fifo_req.io.wrreq := in.a.fire()

  in.d.valid := !fifo_resp.io.rdempty
  in.d.bits := Mux(
    tl_resp.is_reading,
    edge.AccessAck(toSource = tl_resp.source, lgSize = tl_resp.size, data = tl_resp.rdata),
    edge.AccessAck(toSource = tl_resp.source, lgSize = tl_resp.size)
  ) holdUnless !fifo_resp.io.rdempty
  fifo_resp.io.rdreq := in.d.fire()

  withClock(ddr_clock) {
    val rreq = RegInit(false.B)
    val wreq = RegInit(false.B)
    val rreq_pending = RegInit(false.B)

    ddr2.io.local_read_req := rreq
    ddr2.io.local_write_req := wreq

    ddr2.io.local_size := 1.U
    ddr2.io.local_burstbegin := true.B

    ddr2.io.local_address := ddr_req.address
    ddr2.io.local_be := ddr_req.be
    ddr2.io.local_wdata := ddr_req.wdata


    ddr_resp.is_reading := ddr_req.is_reading
    ddr_resp.size := ddr_req.size
    ddr_resp.source := ddr_req.source


    val will_read_request = !fifo_req.io.rdempty && !rreq && !wreq && !rreq_pending && ddr2.io.local_ready
    val will_respond = !fifo_resp.io.wrfull && ((rreq_pending && ddr2.io.local_rdata_valid) || (wreq && ddr2.io.local_ready))
    val request_is_read = RegNext(will_read_request)
    fifo_req.io.rdreq := will_read_request
    fifo_resp.io.wrreq := will_respond

    when (request_is_read) {
      rreq := ddr_req.is_reading
      rreq_pending := ddr_req.is_reading
      wreq := !ddr_req.is_reading
    }
    when (will_respond) { // the response will be sent next clock cycle
      rreq := false.B
      rreq_pending := false.B
      wreq := false.B
      ddr_resp.rdata := ddr2.io.local_rdata
    }
    when (rreq && ddr2.io.local_ready) {
      rreq := false.B
    }
  }

  in.b.valid := false.B
  in.c.ready := true.B
  in.e.ready := true.B

  mem_if.connectFrom(ddr2.io)
}

trait HasAltmemphyDDR2 { this: BaseSubsystem =>
  val dtb: DTB
  val mem_ctrl = LazyModule(new AltmemphyDDR2RAM)
  mem_ctrl.node := mbus.toDRAMController(Some("altmemphy-ddr2"))()
}

trait HasAltmemphyDDR2Imp extends LazyModuleImp {
  val outer: HasAltmemphyDDR2
  val mem_if = IO(new MemIfBundle)
  mem_if <> outer.mem_ctrl.module.mem_if
}
