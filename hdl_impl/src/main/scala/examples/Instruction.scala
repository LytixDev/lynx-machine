package examples

sealed trait OperandKind
object OperandKind {
  case object NoneKind extends OperandKind
  case object Reg extends OperandKind
  case object RegReg extends OperandKind
  case object Imm extends OperandKind
}

sealed abstract class Instruction(val opcode: Int, val kind: OperandKind)
object Instruction {
  import OperandKind._

  case object Halt  extends Instruction(0x0, NoneKind)
  case object Add   extends Instruction(0x1, RegReg)
  case object Sub   extends Instruction(0x2, RegReg)
  case object Ge    extends Instruction(0x3, RegReg)
  case object Le    extends Instruction(0x4, RegReg)
  case object Inc   extends Instruction(0x5, Reg)
  case object Dec   extends Instruction(0x6, Reg)
  case object Shift extends Instruction(0x7, Imm)
  case object Ali   extends Instruction(0x8, Imm)
  case object Li    extends Instruction(0x9, Imm)
  case object Mv    extends Instruction(0xA, RegReg)
  case object Load  extends Instruction(0xB, RegReg)
  case object Store extends Instruction(0xC, RegReg)
  case object Jmp   extends Instruction(0xD, Imm)
  case object Jiz   extends Instruction(0xE, RegReg)
  case object Jaiz  extends Instruction(0xF, RegReg)

  val all: Seq[Instruction] = Seq(
    Halt, Add, Sub, Ge, Le, Inc, Dec,
    Shift, Ali, Li, Mv, Load, Store, Jmp, Jiz, Jaiz
  )

  //def fromOpcode(op: Int): Option[Instruction] =
  //  all.find(_.opcode == op)
}
