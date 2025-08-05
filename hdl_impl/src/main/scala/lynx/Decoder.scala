package lynx

import chisel3._

class Decoder() extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(8.W))
    val opcode = Output(UInt(8.W))
  })

  io.opcode := io.inst(7, 4)
  //printf(p"inst = ${io.inst} = 0x${Hexadecimal(io.inst)} = ${Binary(io.inst)}\n")
}
