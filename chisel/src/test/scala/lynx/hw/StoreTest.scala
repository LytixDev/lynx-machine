package lynx.hw

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import lynx.sw.MachineCodeBuilder

class StoreTest extends AnyFlatSpec with ChiselScalatestTester {
  
  "CPU" should "handle store operations correctly" in {
    val program = Array[Byte](
      MachineCodeBuilder.li(10),
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(7),
      MachineCodeBuilder.store(0, 1),
      MachineCodeBuilder.halt()
    )

    test(new Cpu()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val tester = new HardwareSoftwareTester(dut)
      val cycles = tester.testProgram(program)
      println(s"Store test completed successfully in $cycles cycles")
    }
  }

  "CPU" should "handle store and load together" in {
    // Test program: comprehensive store and load operations
    val program = Array[Byte](
      // Store 42 at memory[8]
      MachineCodeBuilder.li(8),
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(42),
      MachineCodeBuilder.store(0, 1),
      
      // Load it back
      MachineCodeBuilder.load(0, 1),     // r0 = mem[8] = 42
      
      // Store the loaded value at a new location
      MachineCodeBuilder.li(12),
      MachineCodeBuilder.mv(2, 0),
      MachineCodeBuilder.load(0, 1),
      MachineCodeBuilder.store(0, 2),
      
      MachineCodeBuilder.halt()
    )

    test(new Cpu()) { dut =>
      val tester = new HardwareSoftwareTester(dut)
      val cycles = tester.testProgram(program)
      println(s"Store and load test completed successfully in $cycles cycles")
    }
  }
}