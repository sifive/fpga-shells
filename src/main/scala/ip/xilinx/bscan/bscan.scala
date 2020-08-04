package sifive.fpgashells.ip.xilinx.bscan2

import chisel3._
import chisel3.util._
import chisel3.experimental.{ExtModule, Analog, attach}

object JTAGTUNNEL {
  def apply (DUT_TCK: Bool, DUT_TMS: Bool, DUT_TDI: Bool, DUT_TDO:Bool, DUT_TDO_en: Bool): Unit = {
    val inst_jtag_tunnel = Module(new JTAGTUNNEL())
    DUT_TCK := inst_jtag_tunnel.jtag_tck.asBool()
    DUT_TMS := inst_jtag_tunnel.jtag_tms
    DUT_TDI := inst_jtag_tunnel.jtag_tdi
    inst_jtag_tunnel.jtag_tdo := DUT_TDO
    inst_jtag_tunnel.jtag_tdo_en := DUT_TDO_en
  }
}

class BUFGCE extends ExtModule {
  val O = IO(Output(Bool()))
  val CE = IO(Input(Bool()))
  val I = IO(Input(Bool()))
}

class BSCANE2 extends ExtModule(Map("JTAG_CHAIN" -> 4)) {
  val TDO = IO(Input(Bool()))
  val CAPTURE = IO(Output(Bool()))
  val DRCK = IO(Output(Bool()))
  val RESET = IO(Output(Bool()))
  val RUNTEST = IO(Output(Bool()))
  val SEL = IO(Output(Bool()))
  val SHIFT = IO(Output(Bool()))
  val TCK = IO(Output(Bool()))
  val TDI = IO(Output(Bool()))
  val TMS = IO(Output(Bool()))
  val UPDATE = IO(Output(Bool()))
}

class JTAGTUNNEL extends MultiIOModule {
  val jtag_tck: Clock = IO(Output(Clock()))
  val jtag_tms: Bool = IO(Output(Bool()))
  val jtag_tdi: Bool = IO(Output(Bool()))
  val jtag_tdo: Bool = IO(Input(Bool()))
  val jtag_tdo_en: Bool = IO(Input(Bool()))

  val bscane2: BSCANE2 = Module(new BSCANE2)
  jtag_tdi := bscane2.TDI
  bscane2.TDO := Mux(jtag_tdo_en, jtag_tdo, true.B)
  val bufgce = Module(new BUFGCE)
  bufgce.I := bscane2.TCK
  bufgce.CE := bscane2.SEL
  jtag_tck := bufgce.O.asClock

  val posClock: Clock = bscane2.TCK.asClock
  val negClock: Clock = (!bscane2.TCK).asClock

  val tdiRegisterWire = Wire(Bool())
  val shiftCounterWire = Wire(UInt(7.W))
  withReset(!bscane2.SHIFT) {
    withClock(posClock) {
      val shiftCounter = RegInit(0.U(7.W))
      val posCounter = RegInit(0.U(8.W))
      val tdiRegister = RegInit(false.B)
      posCounter := posCounter + 1.U
      when(posCounter >= 1.U && posCounter <= 7.U) {
        shiftCounter := Cat(bscane2.TDI, shiftCounter.head(6))
      }
      when(posCounter === 0.U) {
        tdiRegister := !bscane2.TDI
      }
      tdiRegisterWire := tdiRegister
      shiftCounterWire := shiftCounter
    }
    withClock(negClock) {
      val negCounter = RegInit(0.U(8.W))
      negCounter := negCounter + 1.U
      jtag_tms := MuxLookup(negCounter, false.B, Array(
        4.U -> tdiRegisterWire,
        5.U -> true.B,
        shiftCounterWire + 7.U -> true.B,
        shiftCounterWire + 8.U -> true.B)
      )
    }
  }
}
