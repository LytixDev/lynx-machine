package lynx.hw

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import lynx.sw.MachineCodeBuilder

class LoadTest extends AnyFlatSpec with ChiselScalatestTester {
  
  "CPU" should "handle load operations correctly" in {
    val program = Array[Byte](
      MachineCodeBuilder.li(10),
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(42),
      MachineCodeBuilder.store(0, 1),
      MachineCodeBuilder.load(0, 1),
      MachineCodeBuilder.halt()
    )

    test(new Cpu()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val tester = new HardwareSoftwareTester(dut)
      val cycles = tester.testProgram(program)
      println(s"Load test completed successfully in $cycles cycles")
    }
  }
  
}