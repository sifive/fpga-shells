package sifive.fpgashells.ip.intel


import chisel3._
import shell.intel.MemIfBundle

class ddr4_64bit extends BlackBox {
  override val io = IO(new MemIfBundle {
    val local_address     = Input(UInt(26.W))
    val local_write_req   = Input(Bool())
    val local_read_req    = Input(Bool())
    val local_burstbegin = Input(Bool())
    val local_wdata       = Input(UInt(128.W))
    val local_be          = Input(UInt(16.W))
    val local_size        = Input(UInt(3.W))
    val local_ready       = Output(Bool())
    val local_rdata       = Output(UInt(128.W))
    val local_rdata_valid = Output(Bool())
    val local_refresh_ack = Output(Bool())
  })
}
