package lynx.hw

import chisel3._
import chisel3.util._
import lynx.sw.Instruction

class ExecutionUnit() extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(4.W))
    val operand1 = Input(UInt(8.W))
    val operand2 = Input(UInt(8.W))
    val result = Output(UInt(8.W))
  })

  io.result := MuxLookup(io.opcode, 0.U(8.W))(Seq(
    Instruction.Add.opcode.U -> (io.operand1 + io.operand2),
    Instruction.Sub.opcode.U -> (io.operand1 - io.operand2),
    Instruction.Inc.opcode.U -> (io.operand1 + 1.U),
    Instruction.Dec.opcode.U -> (io.operand1 - 1.U),
    // Comparison operations (1 if true, 0 if false)
    Instruction.Ge.opcode.U -> Mux(io.operand1 >= io.operand2, 1.U, 0.U),
    Instruction.Le.opcode.U -> Mux(io.operand1 <= io.operand2, 1.U, 0.U),
    // Bitwise operations
    Instruction.Shift.opcode.U -> (io.operand1 << io.operand2),
    Instruction.Ali.opcode.U -> (io.operand1 & io.operand2),
    Instruction.Li.opcode.U -> io.operand1,
    Instruction.Mv.opcode.U -> io.operand1,
    
    Instruction.Load.opcode.U -> io.operand1,  // Will be overridden by memory read
    Instruction.Store.opcode.U -> io.operand1, // Data to store
    Instruction.Jmp.opcode.U -> io.operand1,   // Jump address
    Instruction.Jiz.opcode.U -> io.operand1,   // Jump offset
    Instruction.Jaiz.opcode.U -> io.operand1,  // Jump address
    Instruction.Halt.opcode.U -> 0.U
  ))
}