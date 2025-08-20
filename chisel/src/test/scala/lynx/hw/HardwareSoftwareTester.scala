package lynx.hw

import chisel3._
import chiseltest._
import chiseltest.ChiselScalatestTester
import lynx.sw.{LynxMachine, Instruction}

/**
 * Runs a program on both hardware and software implementations  and verifies they produce identical results at each
 * cycle. Assumes the software implementation is always correct.
 */
class HardwareSoftwareTester(dut: Cpu) {
  def testProgram(
    program: Array[Byte], 
    maxCycles: Int = 10_000,
  ): Int = {
    // Initialize software implementation
    val vm = LynxMachine(
      instructions = program,
      data = Array.fill[Byte](256)(0),
      pc = 0,
      registers = Array.fill[Byte](4)(0),
      cyclesExecuted = 0
    )
    
    val comparator = new HardwareSoftwareComparator(dut, vm)
    
    // Load the program into the hardware implementations instruction memory.
    loadProgramToHardware(program)
    
    // Switch to execution mode
    dut.io.loadMode.poke(false.B)
    
    // Run both implementations cycle by cycle
    var cycle = 0
    var halted = false

    var hwHalted = false
    var swHalted = false
    while (!halted && cycle < maxCycles) {
      cycle += 1
      // Check for halt before stepping
      hwHalted = dut.io.debug.decoderOpcodeOut.peek().litValue == Instruction.Halt.opcode
      swHalted = vm.instructions(vm.pc & 0xFF) match {
        case instr => ((instr >> 4) & 0xF) == Instruction.Halt.opcode
      }
      
      if (hwHalted || swHalted) {
        halted = true
      } else {
        comparator.stepBoth()
        comparator.assertStateEqual(cycle)
      }
    }
    
    //if (cycle >= maxCycles) {
    //  throw new RuntimeException(s"Test timed out after $maxCycles cycles without halting")
    //}
    
    // Verify both implementations halted, and not just one of them.
    assert(hwHalted, "Hardware should have halted")
    assert(swHalted, "Software should have halted")
    
    cycle
  }
  
  def readRegister(regIndex: Int): Int = {
    require(regIndex >= 0 && regIndex <= 3, s"Register index must be 0-3, got $regIndex")
    dut.io.debug.regReadAddr.poke(regIndex.U)
    dut.io.debug.regReadData.peek().litValue.toInt
  }
  
  def assertRegister(regIndex: Int, expectedValue: Int): Unit = {
    val actualValue = readRegister(regIndex)
    assert(actualValue == expectedValue, 
      s"Register r$regIndex expected $expectedValue but got $actualValue")
  }
  
  private def loadProgramToHardware(program: Array[Byte]): Unit = {
    dut.io.loadMode.poke(true.B)
    for ((instruction, address) <- program.zipWithIndex) {
      dut.io.loadAddress.poke(address.U)
      dut.io.loadData.poke((instruction & 0xFF).U)
      dut.clock.step(1)
    }
  }
}

object HardwareSoftwareTester {
  /**
   * Convenience method to create and run a test in one call.
   * Must be called from within a test class that extends ChiselScalatestTester.
   */
  def testProgram(
    program: Array[Byte],
    maxCycles: Int = 10_000,
    enableVcd: Boolean = false
  )(implicit tester: ChiselScalatestTester): Int = {
    val annotations = if (enableVcd) Seq(WriteVcdAnnotation) else Seq.empty
    var result = 0
    tester.test(new Cpu()).withAnnotations(annotations) { dut =>
      val hwSwTester = new HardwareSoftwareTester(dut)
      result = hwSwTester.testProgram(program, maxCycles)
    }
    result
  }
}