// See LICENSE for license details.
package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog

import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.{BaseSubsystem, PeripheryBus, PeripheryBusKey}
import freechips.rocketchip.tilelink.TLBusWrapper
import freechips.rocketchip.interrupts.IntInwardNode
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.LogicalTreeNode

import sifive.enterprise.devices.hca._

case class HCAShellInput(index: Int = 0)
case class HCADesignInput(node: BundleBridgeSource[HCAPortIO])(implicit val p: Parameters)
case class HCAOverlayOutput()
case object HCAOverlayKey extends Field[Seq[DesignPlacer[HCADesignInput, HCAShellInput, HCAOverlayOutput]]](Nil)
trait HCAShellPlacer[Shell] extends ShellPlacer[HCADesignInput, HCAShellInput, HCAOverlayOutput]

class ShellHCAPortIO extends Bundle {
  val osc_test_pad = Analog(1.W)
}

abstract class HCAPlacedOverlay(
  val name: String, val di: HCADesignInput, val si: HCAShellInput)
    extends IOPlacedOverlay[ShellHCAPortIO, HCADesignInput, HCAShellInput, HCAOverlayOutput]
{
  implicit val p = di.p

  def ioFactory = new ShellHCAPortIO

  val tlhcaSink = sinkScope { di.node.makeSink }

  def overlayOutput = HCAOverlayOutput()
}
