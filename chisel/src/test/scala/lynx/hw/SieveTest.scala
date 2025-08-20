package lynx.hw

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import lynx.sw.MachineCodeBuilder

class SieveTest extends AnyFlatSpec with ChiselScalatestTester {
  
  "CPU" should "execute Sieve of Eratosthenes program from hex file" in {
    val program = MachineCodeBuilder.buildFromFile("src/test/resources/sieve_of_eratosthenes").toArray

    test(new Cpu()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val tester = new HardwareSoftwareTester(dut)
      tester.testProgram(program, maxCycles = 3000, requireHalt = false)
    }
  }
}