// See LICENSE.SiFive for license details.
package sifive.fpgashells.clocks

import Chisel._
import chisel3.internal.sourceinfo.SourceInfo
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._

case class ClockGroupNode(groupName: String)(implicit valName: ValName)
  extends MixedCustomNode(ClockGroupImp, ClockImp)
{
  def resolveStar(iKnown: Int, oKnown: Int, iStars: Int, oStars: Int): (Int, Int) = {
    require (oStars == 0, s"${name} (a ClockGroupNode) cannot appear right of a :=*${lazyModule.line}")
    require (iKnown + iStars == 1, s"${name} (a ClockGroupNode) must appear exactly once on the left of a :=${lazyModule.line}")
    (1, 1)
  }
  def mapParamsD(n: Int, p: Seq[ClockGroupSourceParameters]): Seq[ClockSourceParameters] = {
    Seq.tabulate(n) { i => ClockSourceParameters(() => p.head.sdcName(i)) }
  }
  def mapParamsU(n: Int, p: Seq[ClockSinkParameters]): Seq[ClockGroupSinkParameters] = {
    Seq(ClockGroupSinkParameters(name = groupName, members = p))
  }
}

class ClockGroup(groupName: String)(implicit p: Parameters) extends LazyModule
{
  val node = ClockGroupNode(groupName)

  lazy val module = new LazyModuleImp(this) {
    val (in, _) = node.in(0)
    val (out, _) = node.out.unzip

    require (node.in.size == 1)
    require (in.member.size == out.size)

    (in.member zip out) foreach { case (i, o) => o := i }
  }
}

object ClockGroup
{
  def apply()(implicit p: Parameters, valName: ValName) =
    LazyModule(new ClockGroup(valName.name)).node
}
