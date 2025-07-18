# The Lynx Abstract Machine Specification

>This document is work in progress

The Lynx Abstract machine is a specification for a "true" 8-bit machine. By "true" 8-bit we informally mean that all components from the registers, data bus, address bus, ALU and to the instruction set are exactly 8 bits wide. It therefore follows that the Lynx Abstract Machine is only able to perform 8-bit arithmetic while being able to address just 256 bytes of memory. Lynx is a primitive machine able to achieve simple computations. The motivation behind the project is to explore what a machine with the previously mentioned constraints would look like and experiment with the possible computational problems it is able to express.

The following is a somewhat informal yet mostly exhaustive specification of the Lynx Abstract Machine. 

## Registers

The Lynx Abstract Machine has four general purpose registers. They are referred to as r0, r1, r2, and r3. All registers are 8-bits wide. The r0 register is also called the accumulator register and serves a special purpose in some instructions. Specifically, certain instructions will imcplicitly store the result of the operation in the r0 register. For instance, the add instruction adds takes any two registers as input and stores the result in r0.

The Lynx Abstract Machine also comes with a program counter (PC). The PC is a special register which points to the next instruction to be executed in the instruction stream. The PC is incremented after executing an instruction unless the instruction itself modifies the PC as is the case for jump instructions. The PC register can not be used as an operand to instructions.

## Memory model

The Lynx Abstract Machine follows the Harvard architecture meaning there are two seperately addressable memories. We call these the instruction memory and the data memory. Both memories are 256 bytes long. The instruction memory is read-only during program execution. The instruction memory is only addressable through the PC. The data memory can be both be read from and written to during program execution. Both memories are byte-addressable.

## Instruction Set

### Encoding

A single instruction takes up 8-bits or one byte of the instruction memory. Consequently, the size of a program for the Lynx machine is limited to 256 instructions.

The instruction set for the Lynx Abstract machine comprises of 16 unique instructions. Therefore, 4 of the 8 available bits fore each instruction is taken up by the 4-bit opcode. The opcode is stored in the 4 most significant bits, i.e, the opcode `0b1111` is stored like `0b11110000` where `0b0000` is the operand.

There are two kinds of operands: registers and immediates. An instruction either expects one register, two registers or an immediate as its operand.

Since there are four registers, two bits are used to specify a single registers. The table below describes the encoding for each general purpose register.

| Register | Encoding |
|----------|----------|
| r0       | 00       |
| r1       | 01       |
| r2       | 10       |
| r3       | 11       |

When registers are used as operands we refer to them as op1 and op2. op1 is stored in the most significant two bits of the four bit operand sequence while op2 is stored as the two least significant bits.

When an immediate is used as the operand it takes up the entire four bit space of the operand. The interpretation of the immediate is instruction dependant. It is either intepreted as a four bit signed integer stored in two complements or a four bit unsigned integer.


### Table

| Mnemonic | Opcode | Operand Kind     | Semantics                          |
|----------|--------|------------------|------------------------------------|
| Halt     | 0000   | None             | Halts execution                    |
| Add      | 0000   | Reg Reg          | r0 = op1 + op2                     |
| Sub      | 0000   | Reg Reg          | r0 = op1 - op2                     |
| Inc      | 0000   | Reg              | op1 = op1 + 1                      |
| Dec      | 0000   | Reg              | op1 = op1 - 1                      |
| Shift    | 0000   | Imm              | r0 = r0 << imm with some nuance    |
| Ali      | 0000   | Imm              | r0 = r0 & imm                      |
| li       | 0000   | Imm              | r0 = imm                           |
| Mv       | 0000   | Reg Reg          | op1 = op2                          |
| Load     | 0000   | Reg Reg          | op1 = mem\[op2\]                   |
| Store    | 0000   | Reg Reg          | mem\[op1\] = op2                   |
| Jmp      | 0000   | Imm              | pc = imm                           |
| Jiz      | 0000   | Reg Reg          | if op1 = 0 then pc = pc + op2      |
