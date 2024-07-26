name := "test-project"
libraryDependencies ++= Seq(
  "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
)

val checkTestDefinitions = taskKey[Unit]("Tests that the test is discovered properly")

checkTestDefinitions := {
  val definitions = (Test / definedTests).value

  assert(definitions.nonEmpty, "Did not find any test !")
  assert(definitions.length == 1, "Found more than the one test (" + definitions.length + ")!")

  streams.value.log.info("Test name = " + definitions.head.name)
  assert(definitions.head.name == "TestFoo", "Failed to discover/name the unit test!")
}
