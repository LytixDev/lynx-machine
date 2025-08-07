package lynx.sw

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MachineCodeBuilderTests extends AnyFlatSpec with Matchers {
  "MachineCodeBuilder" should "just werk " in {
    MachineCodeBuilder.add(1, 2) shouldEqual 0b0001_01_10
  }

  "MachineCodeBuilder" should "read from file correctly" in {
    val result = MachineCodeBuilder.buildFromFile("src/test/resources/fib")
    println(result)
  }

  "VM" should "execute simple arithmetic program" in {
    val program = Array[Byte](
      MachineCodeBuilder.li(5),
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(3),
      MachineCodeBuilder.mv(2, 0),
      MachineCodeBuilder.add(1, 2),
      MachineCodeBuilder.halt()
    )
    
    val vm = LynxMachine(
      instructions = program,
      data = Array.fill[Byte](256)(0),
      pc = 0,
      registers = Array.fill[Byte](4)(0),
      cyclesExecuted = 0
    )
    
    var halted = false
    while (!halted) {
      halted = vm.step()
    }

    vm.registers(0) shouldEqual 8  // r0 should contain 8 (5 + 3)
    vm.registers(1) shouldEqual 5
    vm.registers(2) shouldEqual 3
    vm.pc shouldEqual 6
    vm.cyclesExecuted shouldEqual 6
  }
}
