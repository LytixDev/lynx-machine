enum TokenType {
  Instruction,
  Operand,
  Error,
}

type Token =
  | { type: TokenType.Instruction; lexeme: string; value: Instruction }
  | { type: TokenType.Operand; lexeme: string; value: number | string }
  | { type: TokenType.Error; lexeme: string; message: string };

type Assembler = {
  input: string;
  pos: number;
  inputLen: number;
  instructionLen: number;
  instructionStream: Uint8Array;
  initialData: Uint8Array;

  hadError: boolean;
  errorMessages: string[];
  currentLine: number;
};

function nextToken(ass: Assembler): Token {
  // Ignore whitespace
  while (ass.pos < ass.inputLen && /[ \t]/.test(ass.input[ass.pos])) {
    ass.pos++;
  }

  if (ass.pos >= ass.inputLen) {
    return {
      type: TokenType.Error,
      lexeme: "",
      message: "End of input while parsing token",
    };
  }

  // Find the end of the token (next space or newline)
  let start = ass.pos;
  while (ass.pos < ass.inputLen && !/[ \t\n]/.test(ass.input[ass.pos])) {
    ass.pos++;
  }

  const lexeme = ass.input.slice(start, ass.pos);

  // Parse as directive
  if (lexeme.startsWith("#")) {
    const directiveName = lexeme.slice(1);
    const directive = Object.values(InstructionInfo).find(
      (i) => i.name === directiveName && i.is_directive
    );
    if (directive) {
      return { type: TokenType.Instruction, lexeme: lexeme, value: directive };
    } else {
      return {
        type: TokenType.Error,
        lexeme: lexeme,
        message: `Unknown assembler directive: ${lexeme}`,
      };
    }
  }

  // Parse as instruction
  const instr = Object.values(InstructionInfo).find(
    (i) => i.name === lexeme && !i.is_directive
  );
  if (instr) {
    return { type: TokenType.Instruction, lexeme: lexeme, value: instr };
  }

  // Parse as register (r0, r1, r2, or r3)
  if (/^r[0-3]$/.test(lexeme)) {
    return { type: TokenType.Operand, lexeme: lexeme, value: lexeme };
  }

  // Finally, parse as a potential number
  let value: number | null = null; // Maybe!
  if (/^0x[0-9a-fA-F]+$/.test(lexeme)) {
    // hex
    value = parseInt(lexeme, 16);
  } else if (/^0b[01]+$/.test(lexeme)) {
    // bin
    // get rid of 0b prefix
    value = parseInt(lexeme.slice(2), 2);
  } else if (/^-?[0-9]+$/.test(lexeme)) {
    // decimal
    value = parseInt(lexeme, 10);
  }

  if (value !== null && value >= -128 && value <= 255) {
    return { type: TokenType.Operand, lexeme: lexeme, value };
  }
  //if (value !== null && value >= -128 && value < 0) {
  //  return { type: TokenType.Operand, lexeme: lexeme, value };
  //}
  if (value === null) {
    return {
      type: TokenType.Error,
      lexeme: lexeme,
      message: `Unknown token: ${lexeme}`,
    };
  }
  return {
    type: TokenType.Error,
    lexeme: lexeme,
    message: `${value} too large. Value must be less than 255 and larger than -128.`,
  };
}

function getNextToken(ass: Assembler): Token | null {
  const token = nextToken(ass);
  if (token.type === TokenType.Error) {
    assReportError(ass, token.message);
    return null;
  }
  return token;
}

function getRegisterIndex(token: Token | undefined): number {
  if (!token) return 0;
  return token.type === TokenType.Operand && typeof token.value === "string"
    ? RegisterEncoding[token.value] ?? 0
    : 0;
}

function encodeInstruction(ass: Assembler, first: Token, ops: Token[]): void {
  // TODO: This is purely to make the typechekcer happy
  if (first.type !== TokenType.Instruction) return;

  const kind = first.value.operand_kind;
  const opcode = first.value.encoding << 4;
  let encoded = opcode;

  // Default to 0 if operand is missing
  const reg1 = getRegisterIndex(ops[0]);
  const reg2 = getRegisterIndex(ops[1]);
  const imm =
    ops[0] &&
    ops[0].type === TokenType.Operand &&
    typeof ops[0].value === "number"
      ? ops[0].value
      : 0;

  // TODO: Convert to a 4-bit signed two-complements representation?
  //      JS uses floats under the hood so this may be a bit weird
  //console.log(imm);

  switch (kind) {
    case OperandKind.None:
      // No operands
      encoded = opcode;
      break;
    case OperandKind.Reg:
      encoded = opcode | (reg1 << 2);
      break;
    case OperandKind.RegReg:
      encoded = opcode | (reg1 << 2) | reg2;
      break;
    case OperandKind.Imm:
      encoded = opcode | (imm & 0x0f);
      break;
    default:
      encoded = opcode;
  }

  ass.instructionStream[ass.instructionLen as number] = encoded;
  ass.instructionLen = (ass.instructionLen as number) + 1;
}

function handleDirective(ass: Assembler, first: Token, ops: Token[]): void {
  // TODO: This is purely to make the typechekcer happy
  if (first.type !== TokenType.Instruction) return;

  switch (first.value.kind) {
    case InstructionKind.SkipTo: {
      // @ts-expect-error
      const imm = ops[0].value;
      if (imm < 0 || imm > 255) {
        assReportError(
          ass,
          `#skipto operand must be between 0 and 255 (got ${imm})`
        );
        return;
      }
      ass.instructionLen = imm;
      break;
    }
    case InstructionKind.Set: {
      // @ts-expect-error
      ass.initialData[ops[0].value] = ops[1].value;
      break;
    }
  }
}

function assReportError(ass: Assembler, message: string): void {
  ass.hadError = true;
  ass.errorMessages.push(`Line ${ass.currentLine}: ${message}`);
}

function parseAndAssembleLine(ass: Assembler): void {
  const mnemonic = getNextToken(ass);
  if (!mnemonic || mnemonic.type !== TokenType.Instruction) {
    assReportError(
      ass,
      `Expected an instruction or directive, got '${
        mnemonic ? mnemonic.lexeme : ""
      }'`
    );
    return;
  }

  let numOperands = 0;
  switch (mnemonic.value.operand_kind) {
    case OperandKind.Reg:
    case OperandKind.Imm:
      numOperands = 1;
      break;
    case OperandKind.RegReg:
      numOperands = 2;
      break;
  }

  const operands: Token[] = [];
  for (let i = 0; i < numOperands; i++) {
    const operand = getNextToken(ass);
    if (!operand) return;
    operands.push(operand);
  }

  if (mnemonic.value.is_directive) {
    handleDirective(ass, mnemonic, operands);
  } else {
    encodeInstruction(ass, mnemonic, operands);
  }
}

function assemble(input: string): {
  instructions: Uint8Array;
  data: Uint8Array;
  hadError: boolean;
  errorMessages: string[];
} {
  const ass: Assembler = {
    input: input,
    pos: 0,
    inputLen: input.length,
    instructionLen: 0,
    instructionStream: new Uint8Array(256),
    initialData: new Uint8Array(256),

    hadError: false,
    errorMessages: [],
    currentLine: 0,
  };

  // This is stupid but this is web dev so lets embrace being stupid
  const lines = input.split("\n");
  for (const line of lines) {
    ass.currentLine++;
    // Ignore empty lines and comments. Again, this can be done much faster but so be it.
    const trimmed = line.trim();
    if (trimmed === "" || trimmed.startsWith("//")) continue;
    ass.input = line;
    ass.pos = 0;
    ass.inputLen = line.length;
    parseAndAssembleLine(ass);
  }

  return {
    instructions: ass.instructionStream.slice(0, ass.instructionLen as number),
    data: ass.initialData,
    hadError: ass.hadError,
    errorMessages: ass.errorMessages,
  };
}
