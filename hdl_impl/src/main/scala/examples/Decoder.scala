package examples

import chisel3._

class Decoder() extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(8.W))
    val out = Output(UInt(1.W))
  })

  io.out := 0.U

  val opcode = io.inst(7, 4)
  printf(p"io.inst = ${io.inst}, opcode = $opcode\n")
  when (opcode === Instruction.Halt.opcode.U) {
    io.out := 1.U
  }
}
