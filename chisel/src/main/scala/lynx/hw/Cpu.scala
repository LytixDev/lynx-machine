package lynx.hw

import chisel3._

class Cpu(val program: Seq[Int]) extends Module {
  val io = IO(new Bundle {
    val in0 = Input(UInt(8.W))
    val debugOut = Output(UInt(8.W))
  })

  val PC = WireInit(0.U(8.W))

  val instructions = Module(new Memory)
  instructions.io.address := PC

  val decoder = Module(new Decoder)
  decoder.io.inst := instructions.io.dataOut

  io.debugOut := decoder.io.opcode
}
