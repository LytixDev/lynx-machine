package lynx.hw

import chisel3._
import chisel3.util._
import lynx.sw.Instruction

class MemoryAccess() extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(4.W))
    val operand1 = Input(UInt(8.W))  // Store, then data value. Load, then destination reg.
    val operand2 = Input(UInt(8.W))  // Address for both load and store
    val executionResult = Input(UInt(8.W))  // Result from execution unit
    
    // Memory interface
    val memAddress = Output(UInt(8.W))
    val memWriteData = Output(UInt(8.W))
    val memWriteEnable = Output(Bool())
    val memReadData = Input(UInt(8.W))
    
    // Output to writeback
    val result = Output(UInt(8.W))
  })

  // Determine if this is a memory operation
  val isLoad = io.opcode === Instruction.Load.opcode.U
  val isStore = io.opcode === Instruction.Store.opcode.U
  val isMemoryOp = isLoad || isStore

  // Memory address comes from operand2 for both load and store
  io.memAddress := io.operand2

  // Store: write operand1 (data) to memory at address operand2
  io.memWriteEnable := isStore
  io.memWriteData := io.operand1

  // Output result: 
  // - For load: return data read from memory
  // - For others: pass through execution result
  io.result := Mux(isLoad, io.memReadData, io.executionResult)
}