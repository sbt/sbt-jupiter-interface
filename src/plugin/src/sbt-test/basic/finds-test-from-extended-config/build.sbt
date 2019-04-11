
name := "test-project"

lazy val ItTest = config("it") extend Test

lazy val root = (project in file("."))
  .configs(ItTest)
  .settings(Defaults.itSettings: _*)
  .settings(inConfig(ItTest)(JupiterPlugin.scopedSettings): _*)
  .settings(
    libraryDependencies ++= Seq(
      "net.aichler" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test,
      "org.junit.jupiter" % "junit-jupiter-engine" % "5.4.2" % Test
    )
  )

val checkTestDefinitions = taskKey[Unit]("Tests that the test is discovered properly")

checkTestDefinitions := {
  val definitions = (definedTests in ItTest).value

  assert(definitions.nonEmpty, "Did not find any test !")
  assert(definitions.length == 1, "Found more than the one test (" + definitions.length + ")!")

  streams.value.log.info("Test name = " + definitions.head.name)
  assert(definitions.head.name == "TestFoo", "Failed to discover/name the unit test!")
}

testOptions in ItTest += Tests.Argument(jupiterTestFramework, "-v")
javaSource in ItTest := baseDirectory.value / "src/it/java"
classDirectory in ItTest := baseDirectory.value / "target/scala-2.12/it-classes"
resourceDirectory in ItTest := (baseDirectory apply { baseDir: File => baseDir / "src/it/resources" }).value
