// See LICENSE.txt for license details.
package lynx

import chisel3.iotesters.{Driver, TesterOptionsManager}
import utils.TutorialRunner

object Launcher {
  val examples = Map(
      "Decoder" -> { (manager: TesterOptionsManager) =>
        Driver.execute(() => new Decoder(), manager) {
          (c) => new DecoderTests(c)
        }
      },
  )
  def main(args: Array[String]): Unit = {
    TutorialRunner("lynx", examples, args)
  }
}

