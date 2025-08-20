package lynx.hw

import org.scalatest.flatspec.AnyFlatSpec
import chiseltest.ChiselScalatestTester
import lynx.sw.MachineCodeBuilder

class SimpleAdd extends AnyFlatSpec with ChiselScalatestTester {
  "Simple add" should "just werk" in {
    test(new Cpu()) { dut =>
      val tester = new HardwareSoftwareTester(dut)
      tester.testProgram(Array[Byte](
        MachineCodeBuilder.li(5),
        MachineCodeBuilder.mv(0, 1),
        MachineCodeBuilder.li(3),
        MachineCodeBuilder.add(0, 1),
        MachineCodeBuilder.halt()
      ))
    }
  }
}