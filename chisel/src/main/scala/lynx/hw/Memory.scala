package lynx.hw

import chisel3._

class Memory() extends Module {
  val io = IO(new Bundle {
    val address = Input(UInt(8.W))
    val dataIn = Input(UInt(8.W))
    val writeEnable = Input(Bool())

    val dataOut = Output(UInt(8.W))
  })

  val memory = SyncReadMem(256, UInt(8.W))

  io.dataOut := memory(io.address)

  when(io.writeEnable) {
    memory(io.address) := io.dataIn
  }
}
