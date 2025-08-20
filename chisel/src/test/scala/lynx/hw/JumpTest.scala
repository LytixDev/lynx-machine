package lynx.hw

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import lynx.sw.MachineCodeBuilder

class JumpTest extends AnyFlatSpec with ChiselScalatestTester {
  
  "CPU" should "handle unconditional jump (jmp) correctly" in {
    val program = Array[Byte](
      MachineCodeBuilder.li(5),
      MachineCodeBuilder.jmp(4),
      MachineCodeBuilder.li(9),
      MachineCodeBuilder.li(8),
      MachineCodeBuilder.li(12), // jmp target
      MachineCodeBuilder.halt()
    )

    test(new Cpu()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val tester = new HardwareSoftwareTester(dut)
      tester.testProgram(program)
      tester.assertRegister(0, 12)
    }
  }
  
  "CPU" should "handle conditional relative jump (jiz) when condition is true" in {
    val program = Array[Byte](
      MachineCodeBuilder.li(0),
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(2),
      MachineCodeBuilder.jiz(0, 1),
      MachineCodeBuilder.li(9), // r0 = 9 (should be skipped)
      MachineCodeBuilder.li(12), // r0 = 12 (jump target)
      MachineCodeBuilder.halt()
    )

    test(new Cpu()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val tester = new HardwareSoftwareTester(dut)
      tester.testProgram(program)
      tester.assertRegister(0, 12)
    }
  }
  
  "CPU" should "handle conditional relative jump (jiz) when condition is false" in {
    val program = Array[Byte](
      MachineCodeBuilder.li(1),
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(2),
      MachineCodeBuilder.jiz(0, 1),
      MachineCodeBuilder.li(12),
      MachineCodeBuilder.halt()
    )

    test(new Cpu()) { dut =>
      val tester = new HardwareSoftwareTester(dut)
      tester.testProgram(program)
      tester.assertRegister(0, 12)
    }
  }
  
  "CPU" should "handle conditional absolute jump (jaiz) when condition is true" in {
    val program = Array[Byte](
      MachineCodeBuilder.li(0),
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(5),
      MachineCodeBuilder.jaiz(0, 1),
      MachineCodeBuilder.li(9),
      MachineCodeBuilder.li(12),
      MachineCodeBuilder.halt()
    )

    test(new Cpu()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val tester = new HardwareSoftwareTester(dut)
      tester.testProgram(program)
      tester.assertRegister(0, 12)
    }
  }
  
  "CPU" should "handle conditional absolute jump (jaiz) when condition is false" in {
    val program = Array[Byte](
      MachineCodeBuilder.li(1),
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(5),
      MachineCodeBuilder.jaiz(0, 1),
      MachineCodeBuilder.li(12),
      MachineCodeBuilder.halt()
    )

    test(new Cpu()) { dut =>
      val tester = new HardwareSoftwareTester(dut)
      tester.testProgram(program)
      tester.assertRegister(0, 12)
    }
  }
}