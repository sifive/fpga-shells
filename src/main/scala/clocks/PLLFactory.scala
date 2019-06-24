// See LICENSE for license details.
package sifive.fpgashells.clocks

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._
import scala.collection.immutable.ListMap

case class PLLNode(val feedback: Boolean, names: (Int, Int) => String)(implicit valName: ValName)
  extends MixedCustomNode(ClockImp, ClockGroupImp)
{
  def resolveStar(iKnown: Int, oKnown: Int, iStars: Int, oStars: Int): (Int, Int) = {
    require (oStars == 0, s"${name} (a PLLNode) cannot appear right of a :=*${lazyModule.line}")
    require (iKnown + iStars == 1, s"${name} (a PLLNode) must appear exactly once on the left of a :=${lazyModule.line}")
    (1, 1)
  }
  def mapParamsD(n: Int, p: Seq[ClockSourceParameters]): Seq[ClockGroupSourceParameters] = {
    Seq.tabulate(n) { i => ClockGroupSourceParameters(j => names(i,j)) }
  }
  def mapParamsU(n: Int, p: Seq[ClockGroupSinkParameters]): Seq[ClockSinkParameters] = {
    Seq(ClockSinkParameters())
  }
}

case class PLLInClockParameters(
  freqMHz:  Double,
  jitter:   Double = 50,
  feedback: Boolean = false)

case class PLLOutClockParameters(
  freqMHz:       Double,
  phaseDeg:      Double = 0,
  dutyCycle:     Double = 50, // in percent
  // used to create constraints:
  jitterPS:      Double = 300,
  freqErrorPPM:  Double = 10000,
  phaseErrorDeg: Double = 5)

case class PLLParameters( 
  name:  String,
  input: PLLInClockParameters,
  req:   Seq[PLLOutClockParameters])

trait PLLInstance {
  def getInput: Clock
  def getReset: Option[Bool]
  def getLocked: Bool
  def getClocks: Seq[Clock]
  def getClockNames: Seq[String]
}

case object PLLFactoryKey extends Field[PLLFactory]
class PLLFactory(scope: IOShell, maxOutputs: Int, gen: PLLParameters => PLLInstance)
{
  private var pllNodes: Seq[PLLNode] = Nil

  def apply(feedback: Boolean = false)(implicit valName: ValName, p: Parameters): PLLNode = {
    val node = scope { PLLNode(feedback, names _) }
    pllNodes = node +: pllNodes
    node
  }

  val plls: ModuleValue[Seq[(PLLInstance, PLLNode)]] = scope { InModuleBody {
    val plls = pllNodes.flatMap { case node =>
      require (node.in.size == 1)
      val (in, edgeIn) = node.in(0)
      val (out, edgeOut) = node.out.unzip

      val params = PLLParameters(
        name = node.valName.name,
        input = PLLInClockParameters(
          freqMHz  = edgeIn.clock.freqMHz,
          jitter   = edgeIn.source.jitterPS.getOrElse(50),
          feedback = node.feedback),
        req = edgeOut.flatMap(_.members).map { e =>
          PLLOutClockParameters(
            freqMHz       = e.clock.freqMHz,
            phaseDeg      = e.sink.phaseDeg,
            dutyCycle     = e.clock.dutyCycle,
            jitterPS      = e.sink.jitterPS,
            freqErrorPPM  = e.sink.freqErrorPPM,
            phaseErrorDeg = e.sink.phaseErrorDeg)})

      val pll = gen(params)
      pll.getInput := in.clock
      pll.getReset.foreach { _ := in.reset }
      (out.flatMap(_.member) zip pll.getClocks) foreach { case (o, i) =>
        o.clock := i
        o.reset := !pll.getLocked || in.reset
      }
      Some((pll, node))
    }

    // Require all clock group names to be distinct
    val sdcGroups = Map() ++ plls.flatMap { case tuple =>
    val (pll, node) = tuple
    val (out, edgeOut) = node.out.unzip
      val groupLabels = edgeOut.flatMap(e => Seq.fill(e.members.size) { e.sink.name })
      groupLabels zip pll.getClockNames
    }.groupBy(_._1).mapValues(_.map(_._2))

    // Ensure there are no clock groups with the same name
    require (sdcGroups.size == pllNodes.map(_.edges.out.size).sum)
    sdcGroups.foreach { case (_, clockNames) => scope.sdc.addGroup(clockNames) }

    plls
  } }
  private def names(group: Int, clock: Int) = "WTF"
}
