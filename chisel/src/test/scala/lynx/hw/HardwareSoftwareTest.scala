package lynx.hw

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import lynx.sw.{LynxMachine, MachineCodeBuilder}

class HardwareSoftwareComparator(dut: Cpu, vm: LynxMachine) {
  
  def assertStateEqual(cycle: Int): Unit = {
    // Compare PC
    val hwPC = dut.io.debug.currentPCOut.peek().litValue.toInt
    val swPC = vm.pc & 0xFF
    assert(hwPC == swPC, s"Cycle $cycle: PC mismatch - HW: $hwPC, SW: $swPC")
    
    // Compare all registers - temporarily enable load mode for debug access
    val wasInLoadMode = dut.io.loadMode.peek().litToBoolean
    dut.io.loadMode.poke(true.B)
    for (regIdx <- 0 until 4) {
      dut.io.debug.regReadAddr.poke(regIdx.U)
      val hwReg = dut.io.debug.regReadData.peek().litValue.toInt
      val swReg = vm.registers(regIdx) & 0xFF
      assert(hwReg == swReg, s"Cycle $cycle: Register r$regIdx mismatch - HW: $hwReg, SW: $swReg")
    }
    dut.io.loadMode.poke(wasInLoadMode.B)
    
    // Compare data memory (sample a few locations for now)
    // val memoryAddressesToCheck = Seq(0, 10, 20, 50, 100, 255)
    // for (addr <- memoryAddressesToCheck) {
    //   dut.io.debug.dataMemReadAddr.poke(addr.U)
    //   dut.clock.step(1) // Need a clock cycle for memory read
    //   val hwMem = dut.io.debug.dataMemReadData.peek().litValue.toInt
    //   val swMem = vm.data(addr) & 0xFF
    //   assert(hwMem == swMem, s"Cycle $cycle: Memory[$addr] mismatch - HW: $hwMem, SW: $swMem")
    // }
  }
  
  def stepBoth(): Boolean = {
    // TIMING SYNCHRONIZATION:
    // Hardware is kept 1 cycle ahead so its registered outputs show
    // the same logical state as the software's immediate updates.
    // 
    // Both execute the same instruction, but hardware shows the result
    // in its registers while software shows it immediately.
    
    val halted = vm.step()  // Software executes and updates immediately
    dut.clock.step(1)       // Hardware executes next instruction
    
    halted
  }
}

class HardwareSoftwareTest extends AnyFlatSpec with ChiselScalatestTester {
  
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
      
      // INITIAL STATE SYNCHRONIZATION:
      // Hardware registers update on clock edges (next cycle)
      // Software updates immediately 
      // Solution: Give hardware a 1-cycle head start so both show the same logical state
      dut.clock.step(1)  // Hardware processes first instruction, registers will update
      
      // Now both are synchronized at cycle 0 state
      comparator.assertStateEqual(0)
      
      var cycle = 1
      var halted = false
      while (!halted && cycle < 20) {
        halted = comparator.stepBoth()
        
        // After stepBoth(), both implementations are synchronized at the same cycle
        if (!halted) {
          comparator.assertStateEqual(cycle)
        }
        
        cycle += 1
      }
      
      // Final state comparison
      comparator.assertStateEqual(cycle - 1)
      
      // Verify both reached halt state
      assert(halted, s"Software VM should have halted")
      assert(dut.io.debug.decoderOpcodeOut.peek().litValue == 0, s"Hardware should be at halt instruction (opcode 0)")
      
      println(s"Test completed successfully after $cycle cycles")
    }
  }
}