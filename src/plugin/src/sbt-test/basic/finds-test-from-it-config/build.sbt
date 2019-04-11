
name := "test-project"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(inConfig(IntegrationTest)(JupiterPlugin.scopedSettings): _*)
  .settings(
    libraryDependencies ++= Seq(
      "net.aichler" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % IntegrationTest,
      "org.junit.jupiter" % "junit-jupiter-engine" % "5.4.2" % IntegrationTest
    )
  )

val checkTestDefinitions = taskKey[Unit]("Tests that the test is discovered properly")

checkTestDefinitions := {
  val definitions = (definedTests in IntegrationTest).value

  assert(definitions.nonEmpty, "Did not find any test !")
  assert(definitions.length == 1, "Found more than the one test (" + definitions.length + ")!")

  streams.value.log.info("Test name = " + definitions.head.name)
  assert(definitions.head.name == "TestFoo", "Failed to discover/name the unit test!")
}
