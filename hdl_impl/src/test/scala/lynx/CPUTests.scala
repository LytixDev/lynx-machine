package lynx

import chisel3.iotesters.PeekPokeTester

class CPUTests(c: CPU) extends PeekPokeTester(c) {
    expect(c.io.debugOut, 1)
}
