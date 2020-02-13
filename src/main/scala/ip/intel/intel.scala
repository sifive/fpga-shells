package sifive.fpgashells.ip.intel

import chisel3._
import chisel3.core.{Analog, BlackBox, IntParam, StringParam}
import freechips.rocketchip.jtag.Tristate

class IBUF extends BlackBox {
  val io = IO(new Bundle {
    val datain = Input(Bool())
    val dataout = Output(Bool())
  })

  override def desiredName: String = "ibuf"
}

object IBUF {
  def apply(o: Bool, i: Bool): IBUF = {
    val res = Module(new IBUF)
    res.io.datain := i
    o := res.io.dataout
    res
  }
}

class IOBUF extends BlackBox {
  val io = IO(new Bundle {
    val datain = Input(Bool())
    val dataout = Output(Bool())
    val oe = Input(Bool())

    val dataio = Analog(1.W)
  })

  override def desiredName: String = "iobuf"
}

object IOBUF {
  def apply(a: Analog, t: Tristate): IOBUF = {
    val res = Module(new IOBUF)
    res.io.datain := t.data
    res.io.oe := t.driven

    a <> res.io.dataio

    res
  }
}

class LATCH extends BlackBox {
  override val io = IO(new Bundle {
    val d = Input(Bool())
    val q = Output(Bool())

    val ena = Input(Clock())
  })
}

class FIFO (val width: Int, lglength: Int, showahead: Boolean) extends BlackBox(Map(
  "intended_device_family" -> StringParam("Stratix X"),
  "lpm_showahead" -> StringParam(if (showahead) "ON" else "OFF"),
  "lpm_type" -> StringParam("dcfifo"),
  "lpm_widthu" -> IntParam(lglength),
  "overflow_checking" -> StringParam("ON"),
  "rdsync_delaypipe" -> IntParam(5),
  "underflow_checking" -> StringParam("ON"),
  "use_eab" -> StringParam("ON"),
  "wrsync_delaypipe" -> IntParam(5),

  "lpm_width" -> IntParam(width),
  "lpm_numwords" -> IntParam(1 << lglength)
)) {
  override val io = IO(new Bundle {
    val data = Input(UInt(width.W))
    val rdclk = Input(Clock())
    val rdreq = Input(Bool())
    val wrclk = Input(Clock())
    val wrreq = Input(Bool())
    val q = Output(UInt(width.W))
    val rdempty = Output(Bool())
    val wrfull = Output(Bool())
  })

  override def desiredName: String = "dcfifo"
}

object FIFO {
  def apply[T <: Data](lglength: Int, output: T, outclk: Clock, input: T, inclk: Clock, showahead: Boolean): FIFO = {
    val res = Module(new FIFO(width = output.widthOption.get, lglength = lglength, showahead))
    require(input.getWidth == res.width)
    output := res.io.q.asTypeOf(output)
    res.io.rdclk := outclk
    res.io.data := input.asUInt()
    res.io.wrclk := inclk
    res
  }
}
