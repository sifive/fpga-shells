package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.experimental.{attach, IO, withClockAndReset}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.SyncResetSynchronizerShiftReg
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.devices.xilinx.xilinxvc709mig._

case object VC709DDR3Size extends Field[BigInt](0x100000000L) // 4GB
case object DualVC709DDR3Size extends Field[BigInt](0x200000000L) // 8GB
abstract class DDR3XilinxPlacedOverlay(shell: VC709ShellBasicOverlays, name: String, designInput: DDRDesignInput, shellInput: DDRShellInput)
  extends DDRPlacedOverlay[XilinxVC709MIGPads](name, designInput, shellInput)
{
  val size = p(VC709DDR3Size)

  val migParams = XilinxVC709MIGParams(address = AddressSet.misaligned(di.baseAddress, size))
  val mig       = LazyModule(new XilinxVC709MIG(migParams))
  val ioNode    = BundleBridgeSource(() => mig.module.io.cloneType)
  val topIONode = shell { ioNode.makeSink() }
  val ddrUI     = shell { ClockSourceNode(freqMHz = 200) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := designInput.wrangler := ddrUI

  def overlayOutput = DDROverlayOutput(ddr = mig.node)
  def ioFactory = new XilinxVC709MIGPads(size)

  InModuleBody { ioNode.bundle <> mig.module.io }
}