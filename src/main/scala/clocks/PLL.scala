// See LICENSE for license details.
package sifive.fpgashells.clocks

import Chisel._
import chisel3.core.{Input, Output, attach}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._

case class PLLNode(val feedback: Boolean)(implicit valName: ValName)
  extends MixedNexusNode(ClockImp, ClockGroupImp)(
    dFn = { _ => ClockGroupSourceParameters() },
    uFn = { _ => ClockSinkParameters() })

trait PLL {
  def apply(feedback: Boolean = false)(implicit valName: ValName): PLLNode
}
case object PLLKey extends Field[PLL]

case class InClockParameters(
  freqMHz:  Double,
  jitter:   Double = 50,
  feedback: Boolean = false)

case class OutClockParameters(
  freqMHz:       Double,
  phaseDeg:      Double = 0,
  dutyCycle:     Double = 50, // in percent
  // used to create constraints:
  jitterPS:      Double = 50,
  freqErrorPPM:  Double = 10000,
  phaseErrorDeg: Double = 0)

case class PLLParameters( 
  name:  String,
  input: InClockParameters,
  req:   Seq[OutClockParameters])

trait PLLInstance {
  def getInput: Clock
  def getLocked: Bool
  def getClocks: Seq[Clock]
  def getClockNames: Seq[String]
}

class PLLFactory(maxOutputs: Int, gen: PLLParameters => PLLInstance)(implicit p: Parameters) extends LazyModule with LazyScope with PLL
{
  private var pllNodes: Seq[PLLNode] = Nil

  def apply(feedback: Boolean = false)(implicit valName: ValName): PLLNode = {
    val node = this { PLLNode(feedback) }
    pllNodes = node +: pllNodes
    node
  }

  lazy val module = new LazyModuleImp(this) {
    // Require all clock group names to be distinct
    val sdcGroups = Map() ++ pllNodes.flatMap { case node =>
      require (node.in.size == 1)
      val (in, edgeIn) = node.in(0)
      val (out, edgeOut) = node.out.unzip

      val params = PLLParameters(
        name = node.valName.name,
        input = InClockParameters(
          freqMHz  = edgeIn.clock.freqMHz,
          jitter   = edgeIn.source.jitterPS.getOrElse(50),
          feedback = node.feedback),
        req = edgeOut.flatMap(_.members).map { e =>
          OutClockParameters(
            freqMHz       = e.clock.freqMHz,
            phaseDeg      = e.sink.phaseDeg,
            dutyCycle     = e.clock.dutyCycle,
            jitterPS      = e.sink.jitterPS,
            freqErrorPPM  = e.sink.freqErrorPPM,
            phaseErrorDeg = e.sink.phaseErrorDeg)})

      val pll = gen(params)
      pll.getInput := in.clock
      (out.flatMap(_.member) zip pll.getClocks) foreach { case (o, i) =>
        o.clock := i
        o.reset := pll.getLocked || in.reset
      }

      val groupLabels = edgeOut.flatMap(e => Seq.fill(e.members.size) { e.sink.name })
      groupLabels zip pll.getClockNames
    }.groupBy(_._1).mapValues(_.map(_._2))

    // Ensure there are no clock groups with the same name
    require (sdcGroups.size == pllNodes.map(_.edges.out.size).sum)
    println(s"SDC groups: ${sdcGroups}")
  }
}
