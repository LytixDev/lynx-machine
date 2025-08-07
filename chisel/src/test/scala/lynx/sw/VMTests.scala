package lynx.sw

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class VMTests extends AnyFlatSpec with Matchers {
  
  "VM" should "execute simple arithmetic program using helper" in {
    val program = Array[Byte](
      MachineCodeBuilder.li(5),
      MachineCodeBuilder.mv(1, 0),
      MachineCodeBuilder.li(3),
      MachineCodeBuilder.mv(2, 0),
      MachineCodeBuilder.add(1, 2),
      MachineCodeBuilder.halt()
    )
    VMTestHelper.fromProgram(program)
      .stepUntilHalt()
      .assertRegisters(r0 = 8, r1 = 5, r2 = 3)
      .assertPC(6)
      .assertCycles(6)
  }

  "VM" should "handle memory operations" in {
    // Simple memory test: store a value, then load it back
    val program = Array[Byte](
      MachineCodeBuilder.li(7),
      MachineCodeBuilder.li(10),
      MachineCodeBuilder.mv(1, 0),       // r1 = r0 (r1 = 10, our address)
      MachineCodeBuilder.li(7),          // r0 = 7 (restore value)
      MachineCodeBuilder.store(0, 1),    // mem[r1] = r0, so mem[10] = 7
      MachineCodeBuilder.li(0),
      MachineCodeBuilder.load(0, 1),     // r0 = mem[r1] = mem[10] = 7
      MachineCodeBuilder.halt()
    )
    
    VMTestHelper.fromProgram(program)
      .step()
      .assertRegisters(r0 = 7)
      .step()
      .assertRegisters(r0 = 10)
      .step()
      .assertRegisters(r0 = 10, r1 = 10)
      .step()
      .assertRegisters(r0 = 7, r1 = 10)
      .step()
      .assertMemory(10, 7)
      .step()
      .step()
      .assertRegisters(r0 = 7, r1 = 10)
      .stepUntilHalt()
  }

  "VM" should "handle unconditional jump" in {
    val program = Array[Byte](
      MachineCodeBuilder.li(5),
      MachineCodeBuilder.jmp(4),         // jump to position 4
      MachineCodeBuilder.li(10),         // r0 = 10 (should be skipped)
      MachineCodeBuilder.halt(),         // halt (should be skipped)
      MachineCodeBuilder.li(15),         // r0 = 15 (jump target)
      MachineCodeBuilder.halt()          // halt
    )
    
    VMTestHelper.fromProgram(program)
      .step()
      .assertPC(1)
      .step()
      .assertPC(4)
      .stepUntilHalt()
  }

  "VM" should "execute simple arithmetic program (legacy)" in {
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