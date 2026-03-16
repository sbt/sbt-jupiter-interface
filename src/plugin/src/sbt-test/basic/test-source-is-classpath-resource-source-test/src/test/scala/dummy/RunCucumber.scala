package dummy

import io.cucumber.scala.{EN, ScalaDsl}
import org.junit.platform.suite.api._

@Suite
@IncludeEngines(Array("cucumber"))
@SelectClasspathResource("features/dummy.feature")
@ConfigurationParameter(
  key = io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME,
  value = "dummy"
)
class RunCucumber

class DummySteps extends ScalaDsl with EN {
  var value: Int = 0

  Given("a value of {int}") { (v: Int) =>
    value = v
  }

  Then("the value should be {int}") { (expected: Int) =>
    assert(value == expected, s"$value was not equal to $expected")
  }
}
