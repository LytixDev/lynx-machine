package lynx.hw

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import lynx.sw.{LynxMachine, MachineCodeBuilder}

class StoreTest extends AnyFlatSpec with ChiselScalatestTester {
  
  "CPU" should "handle store operations correctly using hardware-software comparison" in {
    // Test program: store a value to memory 
    val program = Array[Byte](
      MachineCodeBuilder.li(10), // address
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(7), // data
      MachineCodeBuilder.store(0, 1),
      MachineCodeBuilder.halt()
    )

    test(new Cpu()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Initialize software VM for comparison
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
      
      // Switch to execution mode and give hardware head start
      dut.io.loadMode.poke(false.B)
      dut.clock.step(1)
      
      // Run both implementations cycle by cycle
      var cycle = 0
      var halted = false
      while (!halted && cycle < 10) {
        halted = comparator.stepBoth()
        
        if (!halted) {
          comparator.assertStateEqual(cycle)
        }
        
        cycle += 1
      }
      
      // If we reach here, both hardware and software produced identical results
      println(s"Store test completed successfully after $cycle cycles")
    }
  }
}