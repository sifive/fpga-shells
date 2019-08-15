// See LICENSE for license details.
package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.devices.xilinx.xilinxf1vu9paxi4pcis.AXI4PCISPads

case class AXI4PCISOverlayParams()(implicit val p: Parameters)
case object AXI4PCISOverlayKey extends Field[Seq[DesignOverlay[AXI4PCISOverlayParams,TLOutwardNode]]](Nil)

