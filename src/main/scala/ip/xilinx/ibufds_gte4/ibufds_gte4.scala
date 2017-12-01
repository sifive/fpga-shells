// See LICENSE for license details.
package sifive.fpgashells.ip.xilinx.ibufds_gte4

import Chisel._

//IP : xilinx unisim IBUFDS_GTE4
//Virtex Ultrscale + Gigabit Transceiver Differential Signaling Input Buffer
//use default paramterization for 
// REFCLK_EN_TX_PATH ( 1'b0 => reserved )
// REFCLK_HROW_CK_SEL (2'b00 => ODIV2 = 0 )

class IBUFDS_GTE4 extends BlackBox {
  val io = new Bundle {
    val O         = Bool(OUTPUT)
    val ODIV2     = Bool(OUTPUT)
    val CEB       = Bool(INPUT)
    val I         = Bool(INPUT)
    val IB        = Bool(INPUT)
  }
}
