package lynx.sw

import org.scalatest.matchers.should.Matchers

// Builder pattern FTW!
class VMTestHelper(private val vm: LynxMachine) extends Matchers {

  def withMemory(address: Int, value: Byte): VMTestHelper = {
    vm.data(address & 0xFF) = value
    this
  }

  def withRegister(reg: Int, value: Byte): VMTestHelper = {
    vm.registers(reg & 0x3) = value
    this
  }

  def step(): VMTestHelper = {
    vm.step()
    this
  }

  def stepUntilHalt(): VMTestHelper = {
    var halted = false
    while (!halted) {
      halted = vm.step()
    }
    this
  }

  def assertRegister(reg: Int, expected: Int): VMTestHelper = {
    vm.registers(reg & 0x3) shouldEqual expected.toByte
    this
  }

  def assertRegisters(r0: Int = -1, r1: Int = -1, r2: Int = -1, r3: Int = -1): VMTestHelper = {
    if (r0 != -1) vm.registers(0) shouldEqual r0.toByte
    if (r1 != -1) vm.registers(1) shouldEqual r1.toByte
    if (r2 != -1) vm.registers(2) shouldEqual r2.toByte
    if (r3 != -1) vm.registers(3) shouldEqual r3.toByte
    this
  }

  def assertMemory(address: Int, expected: Int): VMTestHelper = {
    vm.data(address & 0xFF) shouldEqual expected.toByte
    this
  }

  def assertPC(expected: Int): VMTestHelper = {
    (vm.pc & 0xFF) shouldEqual (expected & 0xFF)
    this
  }

  def assertCycles(expected: Int): VMTestHelper = {
    vm.cyclesExecuted shouldEqual expected
    this
  }

  def printState(): VMTestHelper = {
    println(s"PC: ${vm.pc & 0xFF}, Cycles: ${vm.cyclesExecuted}")
    println(s"Registers: r0=${vm.registers(0) & 0xFF}, r1=${vm.registers(1) & 0xFF}, r2=${vm.registers(2) & 0xFF}, r3=${vm.registers(3) & 0xFF}")
    this
  }
}

object VMTestHelper {
  def apply(hexProgram: String): VMTestHelper = {
    val program = MachineCodeBuilder.buildFromHex(hexProgram)
    val vm = LynxMachine(
      instructions = program,
      data = Array.fill[Byte](256)(0),
      pc = 0,
      registers = Array.fill[Byte](4)(0),
      cyclesExecuted = 0
    )
    new VMTestHelper(vm)
  }
  
  def fromProgram(program: Array[Byte]): VMTestHelper = {
    val vm = LynxMachine(
      instructions = program,
      data = Array.fill[Byte](256)(0),
      pc = 0,
      registers = Array.fill[Byte](4)(0),
      cyclesExecuted = 0
    )
    new VMTestHelper(vm)
  }
}