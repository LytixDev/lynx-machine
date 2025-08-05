// See LICENSE.txt for license details.
package lynx

import chisel3.iotesters.{Driver, TesterOptionsManager}
import utils.TutorialRunner

object Launcher {

  //val program = Seq(Instruction.Add.opcode)
  val program = Seq(
    Assembler.li(5),
    Assembler.mv(1, 0),
    Assembler.li(3),
    Assembler.mv(2, 0),
    Assembler.add(1, 2),
    Assembler.halt(),
  )

  val examples = Map(
      "Decoder" -> { (manager: TesterOptionsManager) =>
        Driver.execute(() => new Decoder(), manager) {
          (c) => new DecoderTests(c)
        }
      },
      "CPU" -> { (manager: TesterOptionsManager) =>
        Driver.execute(() => new CPU(program), manager) {
          (c) => new CPUTests(c)
        }
      },
  )
  def main(args: Array[String]): Unit = {
    TutorialRunner("lynx", examples, args)
  }
}

