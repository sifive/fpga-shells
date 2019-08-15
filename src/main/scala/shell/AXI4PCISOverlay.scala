// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.config._
import freechips.rocketchip.tilelink._

case class AXI4PCISOverlayParams()(implicit val p: Parameters)
case object AXI4PCISOverlayKey extends Field[Seq[DesignOverlay[AXI4PCISOverlayParams,TLOutwardNode]]](Nil)

