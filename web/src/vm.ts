type LynxMachine = {
  instructions: Uint8Array;
  data: Uint8Array;
  initialDataCopy: Uint8Array;
  pc: number; // u8
  // In TS we can access these be doing an array access like so: lynxMachine["r0"]. Interesting.
  r0: number;
  r1: number;
  r2: number;
  r3: number;
  cyclesExecuted: number;
};

const lynxMachine: LynxMachine = {
  instructions: new Uint8Array(256),
  data: new Uint8Array(256),
  initialDataCopy: new Uint8Array(256),
  pc: 0,
  r0: 0,
  r1: 0,
  r2: 0,
  r3: 0,
  cyclesExecuted: 0,
};

// VM execution state
let vmRunning = false;
let vmTimeoutId: number | null = null;

function startVM(): void {
  if (vmRunning) return;

  vmRunning = true;
  updateVMButtons();

  const executeInstruction = () => {
    if (!vmRunning) return;

    const halted = executeNextInstruction(lynxMachine);
    render();

    if (halted) {
      stopVM();
      console.log("VM halted");
      return;
    }

    if (vmRunning) {
      //executeInstruction();
      vmTimeoutId = setTimeout(executeInstruction, 1000 / vmSpeed);
    }
  };

  executeInstruction();
}

function stopVM(): void {
  if (!vmRunning) return;

  vmRunning = false;

  if (vmTimeoutId !== null) {
    clearTimeout(vmTimeoutId);
    vmTimeoutId = null;
  }

  updateVMButtons();
  render();
}

function updateVMButtons(): void {
  const startBtn = document.getElementById("start-btn") as HTMLButtonElement;
  const stepBtn = document.getElementById("step-btn") as HTMLButtonElement;

  if (startBtn) {
    if (vmRunning) {
      startBtn.textContent = "Stop";
      startBtn.style.backgroundColor = "#333";
      startBtn.style.color = "#fff";
    } else {
      startBtn.textContent = "Start";
      startBtn.style.backgroundColor = "";
      startBtn.style.color = "";
    }
    startBtn.disabled = false;
  }
  if (stepBtn) stepBtn.disabled = vmRunning;
}

// TODO: Move to isa.ts?
function dissasembleInstruction(inst: number): string {
  const opcode = (inst & 0b11110000) >> 4;
  const imm = inst & 0b00001111;
  let signedImm = imm;
  const reg1Idx = (inst & 0b00001100) >> 2;
  const reg2Idx = inst & 0b00000011;
  const reg1 = regNames[reg1Idx];
  const reg2 = regNames[reg2Idx];

  const instruction: Instruction | undefined = (
    Object.values(InstructionInfo) as Instruction[]
  ).find((info) => info.encoding === opcode && !info.is_directive);

  if (!instruction) return "?";

  switch (instruction.operand_kind) {
    case OperandKind.Reg:
      return `${instruction.name} ${reg1}`;
    case OperandKind.RegReg:
      return `${instruction.name} ${reg1} ${reg2}`;
    case OperandKind.Imm:
      return `${instruction.name} ${imm}`;
    default:
      return instruction.name;
  }
}

function executeNextInstruction(lynxMachine: LynxMachine): boolean {
  const inst = lynxMachine.instructions[lynxMachine.pc];

  // Decode
  const opcode = (inst & 0b11110000) >> 4;
  // Least significant 4 bits either encode an immediate or one or two registers
  const imm = inst & 0b00001111;
  let signedImm = imm;
  if (imm & 0x8) signedImm = imm - 0x10;
  // As register
  const reg1Idx = (inst & 0b00001100) >> 2;
  const reg2Idx = inst & 0b00000011;
  const reg1 = regNames[reg1Idx];
  const reg2 = regNames[reg2Idx];

  // Jump instructions sets this to false
  let advancePC = true;

  switch (opcode) {
    // Arithmetic
    case InstructionInfo[InstructionKind.Add].encoding:
      lynxMachine.r0 = (lynxMachine[reg1] + lynxMachine[reg2]) & 0xff;
      break;
    case InstructionInfo[InstructionKind.Sub].encoding:
      lynxMachine.r0 = (lynxMachine[reg1] - lynxMachine[reg2]) & 0xff;
      break;
    case InstructionInfo[InstructionKind.Inc].encoding:
      lynxMachine[reg1] = (lynxMachine[reg1] + 1) & 0xff;
      break;
    case InstructionInfo[InstructionKind.Dec].encoding:
      lynxMachine[reg1] = (lynxMachine[reg1] - 1 + 256) & 0xff;
      break;
    case InstructionInfo[InstructionKind.Ge].encoding:
      lynxMachine.r0 = Number(lynxMachine[reg1] > lynxMachine[reg2]);
      break;
    case InstructionInfo[InstructionKind.Le].encoding:
      lynxMachine.r0 = Number(lynxMachine[reg1] < lynxMachine[reg2]);
      break;
    case InstructionInfo[InstructionKind.Shift].encoding:
      const signBit = (imm & 0b1000) >> 3;
      const shiftAmount = imm & 0b0111;
      if (signBit) {
        lynxMachine.r0 = (lynxMachine.r0 >> -signedImm) & 0xff;
      } else {
        lynxMachine.r0 = (lynxMachine.r0 << shiftAmount) & 0xff;
      }
      break;
    case InstructionInfo[InstructionKind.Ali].encoding:
      lynxMachine.r0 =
        (lynxMachine.r0 + ((lynxMachine.r0 + signedImm) & 0b1111)) & 0xff;
      break;
    case InstructionInfo[InstructionKind.Li].encoding:
      lynxMachine.r0 = imm;
      break;
    // Data movement
    case InstructionInfo[InstructionKind.Mv].encoding:
      lynxMachine[reg1] = lynxMachine[reg2];
      break;
    case InstructionInfo[InstructionKind.Load].encoding:
      lynxMachine[reg1] = lynxMachine.data[lynxMachine[reg2]];
      break;
    case InstructionInfo[InstructionKind.Store].encoding:
      lynxMachine.data[lynxMachine[reg2]] = lynxMachine[reg1];
      break;
    // Jumps
    case InstructionInfo[InstructionKind.Jmp].encoding:
      // Interpret imm as 4-bit signed (-8 to 7)
      lynxMachine.pc = (lynxMachine.pc + signedImm) & 0xff;
      advancePC = false;
      break;
    case InstructionInfo[InstructionKind.Jiz].encoding:
      if (lynxMachine[reg2] === 0) {
        lynxMachine.pc = (lynxMachine.pc + lynxMachine[reg1]) & 0xff;
        advancePC = false;
      }
      break;
    case InstructionInfo[InstructionKind.Jaiz].encoding:
      if (lynxMachine[reg2] === 0) {
        lynxMachine.pc = lynxMachine[reg1];
        advancePC = false;
      }
      break;

    case InstructionInfo[InstructionKind.Halt].encoding:
      // Perhaps we should set a flag or signal something
      return true;
    default:
      alert("Could not decode instruction :-(");
      break;
  }

  if (advancePC) {
    lynxMachine.pc = (lynxMachine.pc + 1) & 0xff;
  }

  lynxMachine.cyclesExecuted++;

  return false;
}
