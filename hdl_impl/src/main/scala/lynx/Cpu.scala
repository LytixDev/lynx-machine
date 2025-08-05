package lynx

import chisel3._

class CPU() extends Module {
  val io = IO(new Bundle {
    val in0 = Input(UInt(8.W))
    val debugOut = Output(UInt(1.W))
  })

  val PC = Wire(UInt(8.W))
  PC := 0.U


  val instructions = Module(new Memory)
  instructions.io.address := PC

  val decoder = Module(new Decoder)
  decoder.io.inst := instructions.io.dataOut

  io.debugOut := decoder.io.out
}
