package lynx.hw

import chisel3._

// NOTE: unfinished
class Decoder() extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(8.W))
    val opcode = Output(UInt(8.W))
  })

  io.opcode := io.inst(7, 4)
}
