package lynx.sw

import scala.io.Source

object MachineCodeBuilder {
  def build(instruction: Instruction, operands: Int*): Int = {
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

  def buildFromFile(fileName: String): Seq[Int] = {
    val source = Source.fromFile(fileName)
    try {
      val content = source.mkString.replaceAll("\\s+", "") // Remove all whitespace
      content.grouped(2).map(hexPair => Integer.parseInt(hexPair, 16)).toSeq
    } finally {
      source.close()
    }
  }

  def halt(): Int = build(Instruction.Halt)
  
  def add(reg1: Int, reg2: Int): Int = build(Instruction.Add, reg1, reg2)
  def sub(reg1: Int, reg2: Int): Int = build(Instruction.Sub, reg1, reg2)
  def ge(reg1: Int, reg2: Int): Int = build(Instruction.Ge, reg1, reg2)
  def le(reg1: Int, reg2: Int): Int = build(Instruction.Le, reg1, reg2)
  def mv(reg1: Int, reg2: Int): Int = build(Instruction.Mv, reg1, reg2)
  def load(reg1: Int, reg2: Int): Int = build(Instruction.Load, reg1, reg2)
  def store(reg1: Int, reg2: Int): Int = build(Instruction.Store, reg1, reg2)
  def jiz(reg1: Int, reg2: Int): Int = build(Instruction.Jiz, reg1, reg2)
  def jaiz(reg1: Int, reg2: Int): Int = build(Instruction.Jaiz, reg1, reg2)
  
  def inc(reg: Int): Int = build(Instruction.Inc, reg)
  def dec(reg: Int): Int = build(Instruction.Dec, reg)
  
  def shift(imm: Int): Int = build(Instruction.Shift, imm)
  def ali(imm: Int): Int = build(Instruction.Ali, imm)
  def li(imm: Int): Int = build(Instruction.Li, imm)
  def jmp(imm: Int): Int = build(Instruction.Jmp, imm)
}
