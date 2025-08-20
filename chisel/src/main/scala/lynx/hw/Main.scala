package lynx.hw

import chisel3._

object CPUMain extends App {
  emitVerilog(new Cpu(), Array("--target-dir", "generated"))
}
