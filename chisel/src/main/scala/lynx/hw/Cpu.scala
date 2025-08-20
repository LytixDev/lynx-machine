package lynx.hw

import chisel3._
import lynx.sw.Instruction

class Cpu() extends Module {
  val io = IO(new Bundle {
    // Ports for loading instructions before executing.
    val loadMode = Input(Bool())
    val loadAddress = Input(UInt(8.W))
    val loadData = Input(UInt(8.W))

    // These IO outputs are only for testing purposes
    val decoderOpcodeOut = Output(UInt(4.W))
    val pcOut = Output(UInt(8.W))
  })

  val PC = RegInit(0.U(8.W))
  val instructionMemory = Module(new Memory)
  val dataMemory = Module(new Memory)

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


  dataMemory.io.writeEnable := (decoder.io.opcode === Instruction.Store.opcode.U) && !io.loadMode
  dataMemory.io.address := 0.U  // Will be connected to execution unit
  dataMemory.io.dataIn := 0.U   // Will be connected to execution unit

  // TODO: branching
  when(!io.loadMode) {
    PC := PC + 1.U
  }

  // Capture PC before increment for testing
  val currentPC = RegNext(PC, 0.U(8.W))

  // Outputs for testing
  io.decoderOpcodeOut := decoder.io.opcode
  io.pcOut := currentPC
}
