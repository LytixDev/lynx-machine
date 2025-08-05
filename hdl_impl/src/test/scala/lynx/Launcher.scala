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
      "CPU" -> { (manager: TesterOptionsManager) =>
        Driver.execute(() => new CPU(), manager) {
          (c) => new CPUTests(c)
        }
      },
  )
  def main(args: Array[String]): Unit = {
    TutorialRunner("lynx", examples, args)
  }
}

