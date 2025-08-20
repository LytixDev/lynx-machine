package lynx.hw

import chisel3._
import lynx.sw.Instruction

class Cpu() extends Module {
  val io = IO(new Bundle {
    // Ports for loading instructions before executing.
    val loadMode = Input(Bool())
    val loadAddress = Input(UInt(8.W))
    val loadData = Input(UInt(8.W))

    // Debug ports for testing - allow reading register file and data memory
    val debug = new Bundle {
      val regReadAddr = Input(UInt(2.W))
      val regReadData = Output(UInt(8.W))
      val dataMemReadAddr = Input(UInt(8.W))
      val dataMemReadData = Output(UInt(8.W))
      val decoderOpcodeOut = Output(UInt(4.W))
      val currentPCOut = Output(UInt(8.W))
      val nextPCOut = Output(UInt(8.W))
      val executionUnitResult = Output(UInt(8.W))
    }
  })

  val PC = RegInit(0.U(8.W))
  val instructionMemory = Module(new Memory)
  val dataMemory = Module(new Memory)
  val registerFile = Module(new RegisterFile)
  val registerFetch = Module(new RegisterFetch)
  val executionUnit = Module(new ExecutionUnit)
  val writeback = Module(new Writeback)

  // Memory interface: either CPU execution or instruction loading
  when(io.loadMode) {
    instructionMemory.io.address := io.loadAddress
    instructionMemory.io.dataIn := io.loadData
    instructionMemory.io.writeEnable := true.B
  }.otherwise {
    instructionMemory.io.address := PC
    instructionMemory.io.dataIn := 0.U
    instructionMemory.io.writeEnable := false.B
  }

  val decoder = Module(new Decoder)
  decoder.io.inst := instructionMemory.io.dataOut

  // Wires from decoder to register fetch
  // TODO: is the opcode needed?
  registerFetch.io.opcode := decoder.io.opcode
  registerFetch.io.imm := decoder.io.imm
  registerFetch.io.reg1 := decoder.io.reg1
  registerFetch.io.reg2 := decoder.io.reg2

  // Wires from register fetch to the register file  
  registerFile.io.readAddr1 := Mux(io.loadMode, io.debug.regReadAddr, registerFetch.io.regReadAddr1)
  registerFile.io.readAddr2 := registerFetch.io.regReadAddr2
  registerFetch.io.regReadData1 := registerFile.io.readData1
  registerFetch.io.regReadData2 := registerFile.io.readData2

  // Wires to the execution unit
  executionUnit.io.opcode := decoder.io.opcode
  executionUnit.io.operand1 := registerFetch.io.operand1
  executionUnit.io.operand2 := registerFetch.io.operand2

  // Wire execution unit to writeback
  writeback.io.opcode := decoder.io.opcode
  writeback.io.reg1 := decoder.io.reg1
  writeback.io.executionResult := executionUnit.io.result

  // Wire writeback to register file
  registerFile.io.writeAddr := writeback.io.regWriteAddr
  registerFile.io.writeData := writeback.io.regWriteData
  registerFile.io.writeEnable := writeback.io.regWriteEnable && !io.loadMode

  // Simple store operation: directly connect register operands to memory
  dataMemory.io.writeEnable := (decoder.io.opcode === Instruction.Store.opcode.U) && !io.loadMode
  dataMemory.io.address := Mux(io.loadMode, io.debug.dataMemReadAddr, 
    Mux(decoder.io.opcode === Instruction.Store.opcode.U, 
      registerFetch.io.operand2,  // store address from second register
      0.U))
  dataMemory.io.dataIn := registerFetch.io.operand1  // store data from first register

  // TODO: branching
  when(!io.loadMode) {
    PC := PC + 1.U
  }

  // Capture PC before increment for testing
  val currentPC = RegNext(PC, 0.U(8.W))

  // Debug port connections for testing
  io.debug.regReadData := registerFile.io.readData1
  io.debug.dataMemReadData := dataMemory.io.dataOut
  io.debug.decoderOpcodeOut := decoder.io.opcode
  io.debug.currentPCOut := currentPC
  io.debug.nextPCOut := PC
  io.debug.executionUnitResult := executionUnit.io.result
}
