package lynx.hw

import chisel3._

class Decoder() extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(8.W))

    val opcode = Output(UInt(4.W))
    val imm = Output(UInt(4.W))
    val reg1 = Output(UInt(2.W))
    val reg2 = Output(UInt(2.W))
  })
  // Literally just bit extraction.
  io.opcode := io.inst(7, 4)
  io.imm := io.inst(3, 0)
  io.reg1 := io.inst(3, 2)
  io.reg2 := io.inst(1, 0)
}
