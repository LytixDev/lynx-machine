package lynx.hw

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import lynx.sw.{Instruction, MachineCodeBuilder}

class CpuTest extends AnyFlatSpec with ChiselScalatestTester {
  "CPU" should "load a program" in {
    val program = Seq(
      MachineCodeBuilder.li(5),
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(3),
      MachineCodeBuilder.mv(2, 0),
      MachineCodeBuilder.add(1, 2),
      MachineCodeBuilder.halt()
      // Expect 8 in r0
    )

    val expectedCycles = 5  // One cycle faster with combinational memory

    test(new Cpu()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Load the program
      dut.io.loadMode.poke(true.B)
      for ((instruction, address) <- program.zipWithIndex) {
        dut.io.loadAddress.poke(address.U)
        // TODO: Unfortunately the instruction coming out is a signed integer. We need to cast it down to a byte.
        //       Assuming the signed int has been sign extended then this is correct.
        dut.io.loadData.poke((instruction & 0xFF).U)
        dut.clock.step(1)
      }

      dut.io.loadMode.poke(false.B)
      // NOTE: We have now processed one cycle
      dut.clock.step(1)
      var cycles = 1

      // Run until halt
      while(dut.io.debug.decoderOpcodeOut.peek().litValue != Instruction.Halt.opcode && cycles < expectedCycles + 10) {
        val pc = dut.io.debug.nextPCOut.peek().litValue.toInt
        val expectedInstruction = program(pc)
        val expectedOpcode = (expectedInstruction >> 4) & 0xF
        val actualOpcode = dut.io.debug.decoderOpcodeOut.peek().litValue
        val expectedName = Instruction.opcodeToName(expectedOpcode.toInt)
        val actualName = Instruction.opcodeToName(actualOpcode.toInt)
        assert(actualOpcode == expectedOpcode, s"At PC=$pc: expected opcode $expectedName ($expectedOpcode), got $actualName ($actualOpcode)")
        
        dut.clock.step(1)
        cycles += 1
      }

      assert(cycles == expectedCycles, s"Expected $expectedCycles cycles, but got $cycles")
      // Check final result: r0 should contain 8 (5 + 3)
    }
  }
}