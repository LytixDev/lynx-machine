package lynx.hw

import chisel3._

object AddMain extends App {
  println("Generating the adder hardware...")
  emitVerilog(new Add(), Array("--target-dir", "generated"))
}
