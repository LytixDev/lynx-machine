package lynx.hw

import chisel3._
import chisel3.util._
import lynx.sw.Instruction

// TODO: Handle case where we write to data memory
class Writeback() extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(4.W))
    val reg1 = Input(UInt(2.W))
    val executionResult = Input(UInt(8.W))
    
    val regWriteAddr = Output(UInt(2.W))
    val regWriteData = Output(UInt(8.W))
    val regWriteEnable = Output(Bool())
  })

  // Determine if instruction writes to register file
  val writesToRegister = MuxLookup(io.opcode, false.B)(Seq(
    Instruction.Add.opcode.U -> true.B,
    Instruction.Sub.opcode.U -> true.B,
    Instruction.Ge.opcode.U -> true.B,
    Instruction.Le.opcode.U -> true.B,
    Instruction.Inc.opcode.U -> true.B,
    Instruction.Dec.opcode.U -> true.B,
    Instruction.Shift.opcode.U -> true.B,
    Instruction.Ali.opcode.U -> true.B,
    Instruction.Li.opcode.U -> true.B,
    Instruction.Mv.opcode.U -> true.B,
    Instruction.Load.opcode.U -> true.B
  ))

  // Determine destination register
  // inc, dec, mv, load write to reg1 (specified register)
  // others write to r0 (the implicit accumulator)
  val writeToSpecifiedReg = MuxLookup(io.opcode, false.B)(Seq(
    Instruction.Inc.opcode.U -> true.B,
    Instruction.Dec.opcode.U -> true.B,
    Instruction.Mv.opcode.U -> true.B,
    Instruction.Load.opcode.U -> true.B
  ))

  io.regWriteEnable := writesToRegister
  io.regWriteAddr := Mux(writeToSpecifiedReg, io.reg1, 0.U(2.W))  // r0 if not specified register
  io.regWriteData := io.executionResult
}