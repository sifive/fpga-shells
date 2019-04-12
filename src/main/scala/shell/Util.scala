package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental._

class AnalogToUInt(w: Int = 1) extends BlackBox(Map("WIDTH" -> IntParam(w))) {
  val io = IO(new Bundle {
    val a = Analog(w.W)
    val b = Output(UInt(w.W))
  })
}

object AnalogToUInt {
  def apply(a: Analog): UInt = {
    val a2b = Module(new AnalogToUInt(w = a.getWidth))
    attach(a, a2b.io.a)
    a2b.io.b
  }
}

class UIntToAnalog(w: Int = 1) extends BlackBox(Map("WIDTH" -> IntParam(w))) {
  val io = IO(new Bundle {
    val a = Analog(w.W)
    val b = Input(UInt(w.W))
    val b_en = Input(Bool())
  })
}

object UIntToAnalog {
  def apply(b: UInt, a: Analog, b_en: Bool): Unit = {
    val a2b = Module(new UIntToAnalog(w = a.getWidth))
    attach(a, a2b.io.a)
    a2b.io.b := b
    a2b.io.b_en := b_en
  }
}
