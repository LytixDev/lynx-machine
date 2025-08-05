package lynx

object Assembler {
  def assembleInstruction(instruction: Instruction, operands: Int*): Int = {
    instruction.kind match {
      case OperandKind.NoneKind =>
        instruction.opcode << 4
        
      case OperandKind.Reg =>
        val reg = operands(0)
        (instruction.opcode << 4) | reg
        
      case OperandKind.RegReg =>
        val reg1 = operands(0)
        val reg2 = operands(1)
        (instruction.opcode << 4) | (reg1 << 2) | reg2
        
      case OperandKind.Imm =>
        val imm = operands(0)
        (instruction.opcode << 4) | imm
    }
  }
  
  def halt(): Int = assembleInstruction(Instruction.Halt)
  
  def add(reg1: Int, reg2: Int): Int = assembleInstruction(Instruction.Add, reg1, reg2)
  def sub(reg1: Int, reg2: Int): Int = assembleInstruction(Instruction.Sub, reg1, reg2)
  def ge(reg1: Int, reg2: Int): Int = assembleInstruction(Instruction.Ge, reg1, reg2)
  def le(reg1: Int, reg2: Int): Int = assembleInstruction(Instruction.Le, reg1, reg2)
  def mv(reg1: Int, reg2: Int): Int = assembleInstruction(Instruction.Mv, reg1, reg2)
  def load(reg1: Int, reg2: Int): Int = assembleInstruction(Instruction.Load, reg1, reg2)
  def store(reg1: Int, reg2: Int): Int = assembleInstruction(Instruction.Store, reg1, reg2)
  def jiz(reg1: Int, reg2: Int): Int = assembleInstruction(Instruction.Jiz, reg1, reg2)
  def jaiz(reg1: Int, reg2: Int): Int = assembleInstruction(Instruction.Jaiz, reg1, reg2)
  
  def inc(reg: Int): Int = assembleInstruction(Instruction.Inc, reg)
  def dec(reg: Int): Int = assembleInstruction(Instruction.Dec, reg)
  
  def shift(imm: Int): Int = assembleInstruction(Instruction.Shift, imm)
  def ali(imm: Int): Int = assembleInstruction(Instruction.Ali, imm)
  def li(imm: Int): Int = assembleInstruction(Instruction.Li, imm)
  def jmp(imm: Int): Int = assembleInstruction(Instruction.Jmp, imm)
}
