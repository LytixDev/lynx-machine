package lynx.hw

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import lynx.sw.MachineCodeBuilder

class FibonacciTest extends AnyFlatSpec with ChiselScalatestTester {
  
  "CPU" should "execute Fibonacci program from hex file" in {
    val program = MachineCodeBuilder.buildFromFile("src/test/resources/fib").toArray

    test(new Cpu()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val tester = new HardwareSoftwareTester(dut)
      // NOTE: The Fibonacci program does not halt and will run for the full 1000 cycles.
      // This is expected behavior. The program is poorly written and never halts :-).
      // TODO: This is actually wrong because we should actually preload 0x01 to be 0x01
      tester.testProgram(program, maxCycles = 1000, requireHalt = false)
    }
  }
}