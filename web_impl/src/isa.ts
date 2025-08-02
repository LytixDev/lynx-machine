enum InstructionKind {
  Halt,

  Add,
  Sub,
  Inc,
  Dec,
  Ge,
  // 4 bit immdiate. If the most significant bit is 1 then right shift, else left shift.
  // Shift amount is the remainding 3 bits.
  Shift,
  Ali, // Add lower immediate
  Li, // Add immediate

  Mv, // dest, src
  Load, // dest, src
  Store, // dest, src
  Stpc, // store pc + 2

  Jmp, // 4bit jump
  Jiz, 
  Jaiz,

  // Directives
  Set,
  SkipTo,
}

enum OperandKind {
  None, // No operands
  Reg, // One register
  RegReg, // Two registers
  Imm, // One immediate
}

type Instruction = {
  kind: InstructionKind;
  name: string;
  operand_kind: OperandKind;
  encoding: number; // 4-bit sequence
  is_directive: boolean;
};

const InstructionInfo: Record<InstructionKind, Instruction> = {
  [InstructionKind.Halt]: {
    kind: InstructionKind.Halt,
    name: "halt",
    operand_kind: OperandKind.None,
    encoding: 0,
    is_directive: false,
  },

  [InstructionKind.Add]: {
    kind: InstructionKind.Add,
    name: "add",
    operand_kind: OperandKind.RegReg,
    encoding: 1,
    is_directive: false,
  },
  [InstructionKind.Sub]: {
    kind: InstructionKind.Sub,
    name: "sub",
    operand_kind: OperandKind.RegReg,
    encoding: 2,
    is_directive: false,
  },
  [InstructionKind.Inc]: {
    kind: InstructionKind.Inc,
    name: "inc",
    operand_kind: OperandKind.Reg,
    encoding: 3,
    is_directive: false,
  },
  [InstructionKind.Dec]: {
    kind: InstructionKind.Dec,
    name: "dec",
    operand_kind: OperandKind.Reg,
    encoding: 4,
    is_directive: false,
  },
  [InstructionKind.Ge]: {
    kind: InstructionKind.Ge,
    name: "ge",
    operand_kind: OperandKind.RegReg,
    encoding: 5,
    is_directive: false,
  },
  [InstructionKind.Shift]: {
    kind: InstructionKind.Shift,
    name: "shift",
    operand_kind: OperandKind.Imm,
    encoding: 6,
    is_directive: false,
  },
  [InstructionKind.Ali]: {
    kind: InstructionKind.Ali,
    name: "ali",
    operand_kind: OperandKind.Imm,
    encoding: 7,
    is_directive: false,
  },
  [InstructionKind.Li]: {
    kind: InstructionKind.Li,
    name: "li",
    operand_kind: OperandKind.Imm,
    encoding: 8,
    is_directive: false,
  },

  [InstructionKind.Mv]: {
    kind: InstructionKind.Mv,
    name: "mv",
    operand_kind: OperandKind.RegReg,
    encoding: 9,
    is_directive: false,
  },
  [InstructionKind.Load]: {
    kind: InstructionKind.Load,
    name: "load",
    operand_kind: OperandKind.RegReg,
    encoding: 10,
    is_directive: false,
  },
  [InstructionKind.Store]: {
    kind: InstructionKind.Store,
    name: "store",
    operand_kind: OperandKind.RegReg,
    encoding: 11,
    is_directive: false,
  },
  [InstructionKind.Stpc]: {
    kind: InstructionKind.Stpc,
    name: "stpc",
    operand_kind: OperandKind.Reg,
    encoding: 12,
    is_directive: false,
  },

  [InstructionKind.Jmp]: {
    kind: InstructionKind.Jmp,
    name: "jmp",
    operand_kind: OperandKind.Imm, // 4-bit signed immediate
    encoding: 13,
    is_directive: false,
  },
  [InstructionKind.Jiz]: {
    kind: InstructionKind.Jiz,
    name: "jiz",
    operand_kind: OperandKind.RegReg,
    encoding: 14,
    is_directive: false,
  },
  [InstructionKind.Jaiz]: {
    kind: InstructionKind.Jaiz,
    name: "jaiz",
    operand_kind: OperandKind.RegReg,
    encoding: 15,
    is_directive: false,
  },

  // Directives. Encoding is not used as these are not encoded as instructions.
  [InstructionKind.Set]: {
    kind: InstructionKind.Set,
    name: "set",
    operand_kind: OperandKind.RegReg,
    encoding: 0,
    is_directive: true,
  },
  [InstructionKind.SkipTo]: {
    kind: InstructionKind.SkipTo,
    name: "skipto",
    operand_kind: OperandKind.Imm, // 8-bit immediate
    encoding: 0, // encoding not used for directives
    is_directive: true,
  },
};

// Map register names to their 2-bit values
const RegisterEncoding: Record<string, number> = {
  r0: 0b00,
  r1: 0b01,
  r2: 0b10,
  r3: 0b11,
};

const regNames = ["r0", "r1", "r2", "r3"] as const;

