// See LICENSE for license details.
package sifive.fpgashells

import chisel3._
import scala.language.implicitConversions

package object shell {
  implicit def boolToIOPin(x: Bool): IOPin = IOPin(x, 0)
  implicit def clockToIOPin(x: Clock): IOPin = IOPin(x, 0)
}
