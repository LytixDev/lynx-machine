package lynx

import chisel3._

// NOTE: read only for now
class Memory() extends Module {
  val io = IO(new Bundle {
    val address = Input(UInt(8.W))
    val dataOut = Output(UInt(8.W))
  })

  val memory = SyncReadMem(256, UInt(8.W))

  io.dataOut := memory(io.address)
}
