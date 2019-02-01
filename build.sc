import mill._
import mill.scalalib._
import ammonite.ops._

import $file.^.`scala-wake`.common, common._

trait FPGAShellsBase extends ScalaModule with WakeModule with CommonOptions {
  def millSourcePath = os.pwd
}
