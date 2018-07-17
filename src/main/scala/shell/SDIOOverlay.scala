// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.spi._

case class SDIOOverlayParams(beatBytes: Int)(implicit val p: Parameters)
case object SDIOOverlayKey extends Field[Seq[DesignOverlay[SDIOOverlayParams, TLSPI]]](Nil)

// Tack on cts, rts signals available on some FPGAs. They are currently unused
// by our designs.
class FPGASDIOPortIO extends Bundle {
  val sdio_clk = Output(Bool())
  val sdio_cmd = Analog(1.W)
  val sdio_dat_0 = Analog(1.W)
  val sdio_dat_1 = Analog(1.W)
  val sdio_dat_2 = Analog(1.W)
  val sdio_dat_3 = Analog(1.W)
}

abstract class SDIOOverlay(
  val params: SDIOOverlayParams)
    extends IOOverlay[FPGASDIOPortIO, TLSPI]
{
  implicit val p = params.p

  def ioFactory = new FPGASDIOPortIO

  val spiParams = p(PeripherySPIKey)
  val use_name = Some(s"spi_0")

  val spiSource = BundleBridge(new TLSPI(params.beatBytes, spiParams(0)).suggestName(use_name))

  val spiSink = shell { spiSource.ioNode.sink }

  val designOutput = spiSource.child

}
