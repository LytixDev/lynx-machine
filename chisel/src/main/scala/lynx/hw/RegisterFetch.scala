package lynx.hw

import chisel3._
import lynx.sw.Instruction

class RegisterFetch() extends Module {
  val io = IO(new Bundle {
    // Inputs from decoder
    val opcode = Input(UInt(4.W))
    val imm = Input(UInt(4.W))
    val reg1 = Input(UInt(2.W))
    val reg2 = Input(UInt(2.W))
    
    // Interface to register file
    val regReadData1 = Input(UInt(8.W))
    val regReadData2 = Input(UInt(8.W))
    val regReadAddr1 = Output(UInt(2.W))
    val regReadAddr2 = Output(UInt(2.W))

    // Operands for execution unit
    val operand1 = Output(UInt(8.W))
    val operand2 = Output(UInt(8.W))
  })

  // Default register file addresses
  io.regReadAddr1 := io.reg1
  io.regReadAddr2 := io.reg2

  // Operand fetching based on instruction type
  io.operand1 := MuxLookup(io.opcode, 0.U(8.W))(Seq(
    // Reg Reg instructions: operand1 = reg1, operand2 = reg2
    Instruction.Add.opcode.U -> io.regReadData1,
    Instruction.Sub.opcode.U -> io.regReadData1,
    Instruction.Ge.opcode.U -> io.regReadData1,
    Instruction.Le.opcode.U -> io.regReadData1,
    Instruction.Mv.opcode.U -> io.regReadData2,    // source is reg2
    Instruction.Load.opcode.U -> io.regReadData2,  // address from reg2
    Instruction.Store.opcode.U -> io.regReadData1,
    Instruction.Jiz.opcode.U -> io.regReadData1,
    Instruction.Jaiz.opcode.U -> io.regReadData1,
    
    Instruction.Inc.opcode.U -> io.regReadData1,
    Instruction.Dec.opcode.U -> io.regReadData1,
    
    // Sign extend
    Instruction.Shift.opcode.U -> Cat(Fill(4, io.imm(3)), io.imm),
    // These automatically zero-extend
    Instruction.Ali.opcode.U -> io.imm,
    Instruction.Li.opcode.U -> io.imm,
    Instruction.Jmp.opcode.U -> io.imm
  ))

  io.operand2 := MuxLookup(io.opcode, 0.U(8.W))(Seq(
    // Reg Reg instructions: operand2 = reg2
    Instruction.Add.opcode.U -> io.regReadData2,
    Instruction.Sub.opcode.U -> io.regReadData2,
    Instruction.Ge.opcode.U -> io.regReadData2,
    Instruction.Le.opcode.U -> io.regReadData2,
    Instruction.Mv.opcode.U -> io.regReadData1,   // destination reg1
    Instruction.Load.opcode.U -> io.regReadData1, // destination reg1
    Instruction.Store.opcode.U -> io.regReadData2,
    Instruction.Jiz.opcode.U -> io.regReadData2,
    Instruction.Jaiz.opcode.U -> io.regReadData2
  ))
}