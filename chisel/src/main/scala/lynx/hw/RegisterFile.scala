package lynx.hw

import chisel3._

// This register file supports reading up to two registers and writing to one at the same time.
// Reading to a register which is written to in the same cycle will return the old value.
class RegisterFile() extends Module {
  val io = IO(new Bundle {
    val readAddr1 = Input(UInt(2.W))
    val readAddr2 = Input(UInt(2.W))
    val readData1 = Output(UInt(8.W))
    val readData2 = Output(UInt(8.W))
    
    val writeAddr = Input(UInt(2.W))
    val writeData = Input(UInt(8.W))
    val writeEnable = Input(Bool())
  })

  // The four 8-bit registers: r0, r1, r2, r3
  val registers = RegInit(VecInit(Seq.fill(4)(0.U(8.W))))

  io.readData1 := registers(io.readAddr1)
  io.readData2 := registers(io.readAddr2)

  when(io.writeEnable) {
    registers(io.writeAddr) := io.writeData
  }

  //printf("Registers: r0=%d, r1=%d, r2=%d, r3=%d\n",
  //  registers(0), registers(1), registers(2), registers(3))
}