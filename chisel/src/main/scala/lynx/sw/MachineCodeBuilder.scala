package lynx.sw

import scala.io.Source

object MachineCodeBuilder {
  private def validateRegister(reg: Int): Unit = {
    require(reg >= 0 && reg <= 3, s"Register must be between 0 and 3, got: $reg")
  }

  def build(instruction: Instruction, operands: Int*): Byte = {
    val result = instruction.kind match {
      case OperandKind.NoneKind =>
        instruction.opcode << 4
      case OperandKind.Reg =>
        val reg = operands(0)
        validateRegister(reg)
        (instruction.opcode << 4) | reg
      case OperandKind.RegReg =>
        val reg1 = operands(0)
        val reg2 = operands(1)
        validateRegister(reg1)
        validateRegister(reg2)
        (instruction.opcode << 4) | (reg1 << 2) | reg2
      case OperandKind.Imm =>
        val imm = operands(0)
        // TODO: validate Imm
        (instruction.opcode << 4) | imm
    }
    result.toByte
  }

  def buildFromFile(fileName: String): Seq[Byte] = {
    val source = Source.fromFile(fileName)
    try {
      val content = source.mkString.replaceAll("\\s+", "") // Remove all whitespace
      content.grouped(2).map(hexPair => Integer.parseInt(hexPair, 16).toByte).toSeq
    } finally {
      source.close()
    }
  }

  def halt(): Byte = build(Instruction.Halt)

  def add(reg1: Int, reg2: Int): Byte = build(Instruction.Add, reg1, reg2)
  def sub(reg1: Int, reg2: Int): Byte = build(Instruction.Sub, reg1, reg2)
  def ge(reg1: Int, reg2: Int): Byte = build(Instruction.Ge, reg1, reg2)
  def le(reg1: Int, reg2: Int): Byte = build(Instruction.Le, reg1, reg2)
  def mv(reg1: Int, reg2: Int): Byte = build(Instruction.Mv, reg1, reg2)
  def load(reg1: Int, reg2: Int): Byte = build(Instruction.Load, reg1, reg2)
  def store(reg1: Int, reg2: Int): Byte = build(Instruction.Store, reg1, reg2)
  def jiz(reg1: Int, reg2: Int): Byte = build(Instruction.Jiz, reg1, reg2)
  def jaiz(reg1: Int, reg2: Int): Byte = build(Instruction.Jaiz, reg1, reg2)

  def inc(reg: Int): Byte = build(Instruction.Inc, reg)
  def dec(reg: Int): Byte = build(Instruction.Dec, reg)

  def shift(imm: Int): Byte = build(Instruction.Shift, imm)
  def ali(imm: Int): Byte = build(Instruction.Ali, imm)
  def li(imm: Int): Byte = build(Instruction.Li, imm)
  def jmp(imm: Int): Byte = build(Instruction.Jmp, imm)
}