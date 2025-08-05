package lynx

import chisel3._

// TODO: We want some kind of memory module where we can preload it for testing
class Memory() extends Module {
  val io = IO(new Bundle {
    val address = Input(UInt(8.W))
    val dataOut = Output(UInt(8.W))
  })

  val memory = SyncReadMem(256, UInt(8.W))

  io.dataOut := memory(io.address)
}

class ProgramROM(val program: Seq[Int]) extends Module {
  val io = IO(new Bundle {
    val address = Input(UInt(8.W))
    val dataOut = Output(UInt(8.W))
  })
  
  val rom = VecInit(program.map(_.U(8.W)) ++ Seq.fill(256 - program.length)(0.U(8.W)))
  io.dataOut := rom(io.address)
}
