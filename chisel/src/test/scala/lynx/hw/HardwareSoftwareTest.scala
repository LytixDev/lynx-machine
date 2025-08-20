package lynx.hw

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import lynx.sw.{LynxMachine, MachineCodeBuilder}

class HardwareSoftwareComparator(dut: Cpu, vm: LynxMachine) {
  
  def assertStateEqual(cycle: Int): Unit = {
    val hwPC = dut.io.debug.nextPCOut.peek().litValue.toInt
    val swPC = vm.pc & 0xFF
    assert(hwPC == swPC, s"Cycle $cycle: PC mismatch - HW: $hwPC, SW: $swPC")

    // Compare all registers
    val wasInLoadMode = dut.io.loadMode.peek().litToBoolean
    dut.io.loadMode.poke(true.B)
    for (regIdx <- 0 until 4) {
      dut.io.debug.regReadAddr.poke(regIdx.U)
      val hwReg = dut.io.debug.regReadData.peek().litValue.toInt
      val swReg = vm.registers(regIdx) & 0xFF
      assert(hwReg == swReg, s"Cycle $cycle: Register r$regIdx mismatch - HW: $hwReg, SW: $swReg")
    }
    dut.io.loadMode.poke(wasInLoadMode.B)
    
    // Compare data memory ??
  }
  
  def stepBoth(): Boolean = {
    val halted = vm.step()
    dut.clock.step(1)
    halted
  }
}

class HardwareSoftwareTest extends AnyFlatSpec with ChiselScalatestTester {
  // This is more of a manual test. We have better testing infra in HardwareSoftwareTester
  "Hardware and Software" should "produce identical results for arithmetic program" in {
    val program = Array[Byte](
      MachineCodeBuilder.li(5),
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(3),
      MachineCodeBuilder.mv(2, 0),
      MachineCodeBuilder.add(1, 2),
      MachineCodeBuilder.halt()
    )

    test(new Cpu()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Initialize software VM
      val vm = LynxMachine(
        instructions = program,
        data = Array.fill[Byte](256)(0),
        pc = 0,
        registers = Array.fill[Byte](4)(0),
        cyclesExecuted = 0
      )
      
      val comparator = new HardwareSoftwareComparator(dut, vm)
      
      // Load program into hardware
      dut.io.loadMode.poke(true.B)
      for ((instruction, address) <- program.zipWithIndex) {
        dut.io.loadAddress.poke(address.U)
        dut.io.loadData.poke((instruction & 0xFF).U)
        dut.clock.step(1)
      }
      
      // Switch to execution mode 
      dut.io.loadMode.poke(false.B)

      var cycle = 0
      var halted = false
      while (!halted && cycle < 20) {
        halted = comparator.stepBoth()
        if (!halted) {
          comparator.assertStateEqual(cycle)
        }
        
        cycle += 1
      }
      
      // Final state comparison
      comparator.assertStateEqual(cycle)
      
      // Verify both reached halt state
      assert(halted, s"Software VM should have halted")
      assert(dut.io.debug.decoderOpcodeOut.peek().litValue == 0, s"Hardware should be at halt instruction (opcode 0)")
    }
  }
}