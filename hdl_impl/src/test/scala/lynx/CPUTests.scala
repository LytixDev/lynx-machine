package lynx

import chisel3.iotesters.PeekPokeTester

class CPUTests(c: CPU) extends PeekPokeTester(c) {
    step(1)
    expect(c.io.debugOut, Instruction.Li.opcode)
}
