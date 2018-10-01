// See LICENSE for license details.

package sifive.fpgashells.ip.xilinx
import Chisel._
import chisel3.{Input, Output}
import chisel3.experimental.{Analog, attach, StringParam, RawParam, IntParam, DoubleParam}

import sifive.blocks.devices.pinctrl.{BasePin}

object booleanToVerilogVectorParam extends (Boolean => RawParam) {
  def apply(b : Boolean) : RawParam =  if(b) RawParam("1") else RawParam("0")
}

object booleanToVerilogStringParam extends (Boolean => StringParam) {
  def apply(b : Boolean) : StringParam = if(b) StringParam("""TRUE""") else StringParam("""FALSE""")
}


/** IBUFDS -- SelectIO Differential Input */

class IBUFDS(
  CAPACITANCE : String = "DONT_CARE",
  DIFF_TERM : Boolean = false,
  DQS_BIAS : Boolean = false,
  IBUF_DELAY_VALUE : Int = 0,
  IBUF_LOW_PWR : Boolean = true,
  IFD_DELAY_VALUE : String = "AUTO",
  IOSTANDARD : String = "DEFAULT"
)
extends BlackBox(
  Map(
  "CAPACITANCE" -> StringParam(CAPACITANCE),
  "DIFF_TERM" -> booleanToVerilogStringParam(DIFF_TERM),
  "DQS_BIAS" -> booleanToVerilogStringParam(DQS_BIAS),
  "IBUF_DELAY_VALUE" -> IntParam(IBUF_DELAY_VALUE),
  "IBUF_LOW_PWR" -> booleanToVerilogStringParam(IBUF_LOW_PWR),
  "IFD_DELAY_VALUE" -> StringParam(IFD_DELAY_VALUE),
  "IOSTANDARD" -> StringParam(IOSTANDARD)
  )
) {
  val io = IO(new Bundle {
    val O  = Output(Clock())
    val I  = Input(Clock())
    val IB = Input(Clock())
  })
}

/** IBUFG -- Clock Input Buffer */

class IBUFG extends BlackBox {
  val io = IO(new Bundle {
    val O = Output(Clock())
    val I = Input(Clock())
  })
}

object IBUFG {
  def apply (pin: Clock): Clock = {
    val pad = Module (new IBUFG())
    pad.io.I := pin
    pad.io.O
  }
}

/** IBUF -- Input Buffer */

class IBUF extends BlackBox {
  val io = IO(new Bundle {
    val O = Output(Bool())
    val I = Input(Bool())
  })
}

object IBUF {
  def apply(pin: Bool): Bool = {
    val pad = Module (new IBUF)
    pad.io.I := pin
    pad.io.O
  }
}

/** IBUFDS_GTE2 -- Differential Signaling Input Buffer */

class IBUFDS_GTE2(
  CLKCM_CFG : Boolean = true,
  CLKRCV_TRST : Boolean = true,
  CLKSWING_CFG : Int = 3
)
extends BlackBox(
  Map(
  "CLKCM_CFG" -> booleanToVerilogStringParam(CLKCM_CFG),
  "CLKRCV_TRST" -> booleanToVerilogStringParam(CLKCM_CFG),
  "CLKSWING_CFG" -> IntParam(CLKSWING_CFG)
  )
) {
  val io = IO(new Bundle {
    val O         = Bool(OUTPUT)
    val ODIV2     = Bool(OUTPUT)
    val CEB       = Bool(INPUT)
    val I         = Bool(INPUT)
    val IB        = Bool(INPUT)
  })
}

class IBUFDS_GTE4(
    REFCLK_EN_TX_PATH:  Int = 0,
    REFCLK_HROW_CK_SEL: Int = 0,
    REFCLK_ICNTL_RX:    Int = 0)
  extends BlackBox(Map(
    "REFCLK_EN_TX_PATH"  -> IntParam(REFCLK_EN_TX_PATH),
    "REFCLK_HROW_CK_SEL" -> IntParam(REFCLK_HROW_CK_SEL),
    "REFCLK_ICNTL_RX"    -> IntParam(REFCLK_ICNTL_RX)))
{
  val io = IO(new Bundle {
    val O     = Clock(OUTPUT)
    val ODIV2 = Clock(OUTPUT)
    val CEB   = Bool(INPUT)
    val I     = Clock(INPUT)
    val IB    = Clock(INPUT)
  })
}

/** IDDR - 7 Series SelectIO DDR flop */

class IDDR(
  DDR_CLK_EDGE : String = "OPPOSITE_EDGE",
  INIT_Q1 : Boolean = false,
  INIT_Q2 : Boolean = false,
  IS_C_INVERTED : Boolean = false,
  IS_D_INVERTED : Boolean = false,
  SRTYPE : String = "SYNC"
)
extends BlackBox(
  Map(
    "DDR_CLK_EDGE" -> StringParam(DDR_CLK_EDGE),
    "INIT_Q1" -> booleanToVerilogVectorParam(INIT_Q1),
    "INIT_Q2" -> booleanToVerilogVectorParam(INIT_Q2),
    "IS_C_INVERTED" -> booleanToVerilogVectorParam(IS_C_INVERTED),
    "IS_D_INVERTED" -> booleanToVerilogVectorParam(IS_D_INVERTED),
    "SRTYPE" -> StringParam(SRTYPE)
  ) 
) {
  val io = IO(new Bundle {
    val Q1 = Output(Bool())
    val Q2 = Output(Bool())
    val C = Input(Bool())
    val CE = Input(Bool())
    val D = Input(Bool())
    val R = Input(Bool())
    val S = Input(Bool())
  })
} 

/** IDELAYCTRL - 7 Series SelectIO */

class IDELAYCTRL(
  sim_device : String = "7SERIES"
) 
extends BlackBox(
  Map(
    "SIM_DEVICE" -> StringParam(sim_device)
  )
) {
  val io = IO(new Bundle {
    val RDY = Output(Bool())
    val REFCLK = Input(Bool())
    val RST = Input(Bool())
  })
}


/** IDELAYE2 -- 7 Series SelectIO ILogic programmable delay. */

class IDELAYE2(
  CINVCTRL_SEL : Boolean = false,
  DELAY_SRC : String = "IDATAIN",
  HIGH_PERFORMANCE_MODE : Boolean = false,
  IDELAY_TYPE : String = "FIXED",
  IDELAY_VALUE : Int = 0,
  IS_C_INVERTED : Boolean = false,
  IS_DATAIN_INVERTED : Boolean = false,
  IS_IDATAIN_INVERTED : Boolean = false,
  PIPE_SEL : Boolean = false,
  REFCLK_FREQUENCY : Double = 200.0,
  SIGNAL_PATTERN : String  = "DATA",
  SIM_DELAY_D : Int = 0
) 
extends BlackBox(
  Map(
    "CINVCTRL_SEL" -> booleanToVerilogStringParam(CINVCTRL_SEL),
    "DELAY_SRC" -> StringParam(DELAY_SRC),
    "HIGH_PERFORMANCE_MODE" -> booleanToVerilogStringParam(HIGH_PERFORMANCE_MODE),
    "IDELAY_TYPE" -> StringParam(IDELAY_TYPE),
    "IS_C_INVERTED" -> booleanToVerilogVectorParam(IS_C_INVERTED),
    "IS_DATAIN_INVERTED" -> booleanToVerilogVectorParam(IS_DATAIN_INVERTED),
    "IS_IDATAIN_INVERTED" -> booleanToVerilogVectorParam(IS_IDATAIN_INVERTED),
    "PIPE_SEL" -> booleanToVerilogStringParam(PIPE_SEL),
    "REFCLK_FREQUENCY" ->  DoubleParam(REFCLK_FREQUENCY),
    "SIGNAL_PATTERN" -> StringParam(SIGNAL_PATTERN),
    "SIM_DELAY_D" -> IntParam(SIM_DELAY_D)
  )
) {
  val io = IO(new Bundle {
    val DATAOUT = Output(Bool())
    val CNTVALUEOUT = Output(UInt(5.W))
    val C = Input(Bool())
    val CE = Input(Bool())
    val CINVCTRL = Input(Bool())
    val DATAIN = Input(Bool())
    val IDATAIN = Input(Bool())
    val INC = Input(Bool())
    val LD = Input(Bool())
    val LDPIPEEN = Input(Bool())
    val REGRST = Input(Bool())
    val CNTVALUEIN = Input(UInt(5.W))
  })
}

/** IOBUF -- Bidirectional IO Buffer. */

//Cannot convert to BlackBox because of line 
//val IO = IO(Analog(1.W)) 
//is illegal

class IOBUF extends BlackBox {

  val io = new Bundle {
    val O = Output(Bool())
    val IO = Analog(1.W)
    val I = Input(Bool())
    val T = Input(Bool())
  }
}

object IOBUF {

    def apply (pin: Analog, ctrl: BasePin): Bool = {
      val pad = Module(new IOBUF())
      pad.io.I := ctrl.o.oval
      pad.io.T := ~ctrl.o.oe
      ctrl.i.ival := pad.io.O & ctrl.o.ie
      attach(pad.io.IO, pin)
      pad.io.O & ctrl.o.ie
  }

  // Creates an output IOBUF
  def apply (pin: Analog, in: Bool): Unit = {
    val pad = Module(new IOBUF())
    pad.io.I := in
    pad.io.T := false.B
    attach(pad.io.IO, pin)
  }

  // Creates an input IOBUF
  def apply (pin: Analog): Bool = {
    val pad = Module(new IOBUF())
    pad.io.I := false.B
    pad.io.T := true.B
    attach(pad.io.IO, pin)
    pad.io.O
  }

}

/** ODDR - 7 Series SelectIO DDR flop */

class ODDR(
  DDR_CLK_EDGE : String = "OPPOSITE_EDGE",
  INIT : Boolean = false,
  IS_C_INVERTED : Boolean = false,
  IS_D1_INVERTED : Boolean = false,
  IS_D2_INVERTED : Boolean = false,
  SRTYPE : String = "SYNC"
)
extends BlackBox(
  Map(
    "DDR_CLK_EDGE" -> StringParam(DDR_CLK_EDGE),
    "INIT" -> booleanToVerilogVectorParam(INIT),
    "IS_C_INVERTED" -> booleanToVerilogVectorParam(IS_C_INVERTED),
    "IS_D1_INVERTED" -> booleanToVerilogVectorParam(IS_D1_INVERTED),
    "IS_D2_INVERTED" -> booleanToVerilogVectorParam(IS_D2_INVERTED),
    "SRTYPE" -> StringParam(SRTYPE)
  ) 
) {
  val io = IO(new Bundle {
    val Q = Output(Bool())
    val C = Input(Clock())
    val CE = Input(Bool())
    val D1 = Input(Bool())
    val D2 = Input(Bool())
    val R = Input(Bool())
    val S = Input(Bool())
  })
} 

/** ODELAYE2 -- 7 Series SelectIO OLogic programmable delay. */

class ODELAYE2(
  CINVCTRL_SEL : Boolean = false,
  DELAY_SRC : String = "ODATAIN",
  HIGH_PERFORMANCE_MODE : Boolean = false,
  IS_C_INVERTED : Boolean = false,
  IS_ODATAIN_INVERTED : Boolean = false,
  ODELAY_TYPE : String = "FIXED",
  ODELAY_VALUE : Int = 0,
  PIPE_SEL : Boolean = false,
  REFCLK_FREQUENCY : Double = 200.0,
  SIGNAL_PATTERN : String  = "DATA",
  SIM_DELAY_D : Int = 0
) 
extends BlackBox(
  Map(
    "CINVCTRL_SEL" -> booleanToVerilogStringParam(CINVCTRL_SEL),
    "DELAY_SRC" -> StringParam(DELAY_SRC),
    "HIGH_PERFORMANCE_MODE" -> booleanToVerilogStringParam(HIGH_PERFORMANCE_MODE),
    "IS_C_INVERTED" -> booleanToVerilogVectorParam(IS_C_INVERTED),
    "IS_ODATAIN_INVERTED" -> booleanToVerilogVectorParam(IS_ODATAIN_INVERTED),
    "ODELAY_TYPE" -> StringParam(ODELAY_TYPE),
    "PIPE_SEL" -> booleanToVerilogStringParam(PIPE_SEL),
    "REFCLK_FREQUENCY" ->  DoubleParam(REFCLK_FREQUENCY),
    "SIGNAL_PATTERN" -> StringParam(SIGNAL_PATTERN),
    "SIM_DELAY_D" -> IntParam(SIM_DELAY_D)
  )
) {
  val io = IO(new Bundle { 
    val DATAOUT = Output(Bool())
    val CNTVALUEOUT = Output(UInt(5.W))
    val C = Input(Bool())
    val CE = Input(Bool())
    val CINVCTRL = Input(Bool())
    val CLKIN = Input(Bool())
    val INC = Input(Bool())
    val LD = Input(Bool())
    val LDPIPEEN = Input(Bool())
    val ODATAIN = Input(Bool())
    val REGRST = Input(Bool())
    val CNTVALUEIN = Input(UInt(5.W))
  })
}

/** PULLUP : can be applied to Input to add a Pullup. */

class PULLUP extends BlackBox {
  val io = IO(new Bundle { 
    val O = Analog(1.W)
  })
}

object PULLUP {
    def apply (pin: Analog): Unit = {
    val pullup = Module(new PULLUP())
    attach(pullup.io.O, pin)
    }
}

/** KEEPER : can be applied to I/O to hold its last value since driven. */

class KEEPER extends BlackBox {
  val io = IO(new Bundle { 
    val O = Analog(1.W)
  })
}

object KEEPER {
    def apply (pin: Analog): Unit = {
    val pullup = Module(new KEEPER())
    attach(pullup.io.O, pin)
    }
}

