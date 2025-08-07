package lynx.sw

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MachineCodeBuilderTests extends AnyFlatSpec with Matchers {
  "MachineCodeBuilder" should "just werk " in {
    MachineCodeBuilder.add(1, 2) shouldEqual 0b0001_01_10
  }

  "MachineCodeBuilder" should "read from file correctly" in {
    val result = MachineCodeBuilder.buildFromFile("src/test/resources/fib")
    //println(result)
  }

}
