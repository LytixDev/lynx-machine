package lynx.sw

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

  case class Inst(override val opcode: Int, override val kind: OperandKind) extends Instruction(opcode, kind)

  val Halt  = Inst(0x0, NoneKind)
  val Add   = Inst(0x1, RegReg)
  val Sub   = Inst(0x2, RegReg)
  val Ge    = Inst(0x3, RegReg)
  val Le    = Inst(0x4, RegReg)
  val Inc   = Inst(0x5, Reg)
  val Dec   = Inst(0x6, Reg)
  val Shift = Inst(0x7, Imm)
  val Ali   = Inst(0x8, Imm)
  val Li    = Inst(0x9, Imm)
  val Mv    = Inst(0xA, RegReg)
  val Load  = Inst(0xB, RegReg)
  val Store = Inst(0xC, RegReg)
  val Jmp   = Inst(0xD, Imm)
  val Jiz   = Inst(0xE, RegReg)
  val Jaiz  = Inst(0xF, RegReg)

  val all: Seq[Instruction] = Seq(
    Halt, Add, Sub, Ge, Le, Inc, Dec,
    Shift, Ali, Li, Mv, Load, Store, Jmp, Jiz, Jaiz
  )
  
  val byOpcode: Array[Instruction] = Array(
    Halt, Add, Sub, Ge, Le, Inc, Dec,
    Shift, Ali, Li, Mv, Load, Store, Jmp, Jiz, Jaiz
  )
  
  def opcodeToName(opcode: Int): String = opcode match {
    case 0x0 => "halt"
    case 0x1 => "add" 
    case 0x2 => "sub"
    case 0x3 => "ge"
    case 0x4 => "le"
    case 0x5 => "inc"
    case 0x6 => "dec"
    case 0x7 => "shift"
    case 0x8 => "ali"
    case 0x9 => "li"
    case 0xA => "mv"
    case 0xB => "load"
    case 0xC => "store"
    case 0xD => "jmp"
    case 0xE => "jiz"
    case 0xF => "jaiz"
    case _ => s"unknown($opcode)"
  }
}
