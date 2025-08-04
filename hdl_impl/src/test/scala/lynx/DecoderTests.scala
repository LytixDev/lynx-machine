package lynx

import chisel3.iotesters.PeekPokeTester

class DecoderTests(c: Decoder) extends PeekPokeTester(c) {
  {
    val opcode = Instruction.Add.opcode
    val fullInst = (opcode << 4) | 0x0
    poke(c.io.inst, fullInst)
    step(1)
    expect(c.io.out, 0)
  }

  {
    val opcode = Instruction.Halt.opcode
    val fullInst = (opcode << 4) | 0x0
    poke(c.io.inst, fullInst)
    step(1)
    expect(c.io.out, 1)
  }
}
