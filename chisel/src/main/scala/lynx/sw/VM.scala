package lynx.sw

case class LynxMachine(
  instructions: Array[Byte],
  data: Array[Byte],
  var pc: Byte,
  var registers: Array[Byte],  // There are 4 registers
  var cyclesExecuted: Int
) {
  // Returns true on a halt
  def step(): Boolean = {
    val instr = instructions(pc & 0xFF)
    val opcode = (instr >> 4) & 0xF
    val operand = instr & 0xF
    val instruction = Instruction.byOpcode(opcode)
    val pcModified = executeInstruction(instruction, operand)
    if (!pcModified) {
      pc = ((pc + 1) & 0xFF).toByte
    }
    cyclesExecuted += 1
    return instruction == Instruction.Halt
  }
  
  private def executeInstruction(instruction: Instruction, operand: Int): Boolean = {
    instruction.kind match {
      case OperandKind.NoneKind =>
        executeNoneInstruction(instruction)
      case OperandKind.Reg =>
        val reg = operand & 0x3
        executeRegInstruction(instruction, reg)
      case OperandKind.RegReg =>
        val op1 = (operand >> 2) & 0x3
        val op2 = operand & 0x3
        executeRegRegInstruction(instruction, op1, op2)
      case OperandKind.Imm =>
        executeImmInstruction(instruction, operand)
    }
  }
  
  private def executeNoneInstruction(instruction: Instruction): Boolean = {
    instruction match {
      case Instruction.Halt =>
        false
    }
  }
  
  private def executeRegInstruction(instruction: Instruction, reg: Int): Boolean = {
    instruction match {
      case Instruction.Inc =>
        registers(reg) = ((registers(reg) + 1) & 0xFF).toByte
        false
      case Instruction.Dec =>
        registers(reg) = ((registers(reg) - 1) & 0xFF).toByte
        false
    }
  }
  
  private def executeRegRegInstruction(instruction: Instruction, op1: Int, op2: Int): Boolean = {
    instruction match {
      case Instruction.Add =>
        registers(0) = ((registers(op1) + registers(op2)) & 0xFF).toByte
        false
      case Instruction.Sub =>
        registers(0) = ((registers(op1) - registers(op2)) & 0xFF).toByte
        false
      case Instruction.Ge =>
        registers(0) = if (registers(op1) >= registers(op2)) 1 else 0
        false
      case Instruction.Le =>
        registers(0) = if (registers(op1) <= registers(op2)) 1 else 0
        false
      case Instruction.Mv =>
        registers(op1) = registers(op2)
        false
      case Instruction.Load =>
        val address = registers(op2)
        registers(op1) = data(address)
        false
      case Instruction.Store =>
        val address = registers(op2)
        data(address) = registers(op1)
        false
      case Instruction.Jiz =>
        if (registers(op2) == 0) {
          pc = ((pc + registers(op1)) & 0xFF).toByte
          true
        } else {
          false
        }
      case Instruction.Jaiz =>
        if (registers(op2) == 0) {
          pc = registers(op1)
          true
        } else {
          false
        }
    }
  }
  
  private def executeImmInstruction(instruction: Instruction, imm: Int): Boolean = {
    instruction match {
      case Instruction.Shift =>
        val shiftAmount = if (imm > 7) -(16 - imm) else imm
        if (shiftAmount >= 0) {
          registers(0) = ((registers(0) << shiftAmount) & 0xFF).toByte
        } else {
          registers(0) = (registers(0) >> (-shiftAmount)).toByte
        }
        false
      case Instruction.Ali =>
        registers(0) = (registers(0) & imm).toByte
        false
      case Instruction.Li =>
        registers(0) = imm.toByte
        false
      case Instruction.Jmp =>
        pc = imm.toByte
        true
    }
  }
}