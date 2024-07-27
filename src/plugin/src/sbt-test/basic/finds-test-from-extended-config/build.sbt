
name := "test-project"

lazy val ItTest = config("it") extend Test

lazy val root = (project in file("."))
  .configs(ItTest)
  .settings(Defaults.itSettings: _*)
  .settings(inConfig(ItTest)(JupiterPlugin.scopedSettings): _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test,
      "org.junit.jupiter" % "junit-jupiter-engine" % "5.4.2" % Test
    )
  )

val checkTestDefinitions = taskKey[Unit]("Tests that the test is discovered properly")

checkTestDefinitions := {
  val definitions = (ItTest / definedTests).value

  assert(definitions.nonEmpty, "Did not find any test !")
  assert(definitions.length == 1, "Found more than the one test (" + definitions.length + ")!")

  streams.value.log.info("Test name = " + definitions.head.name)
  assert(definitions.head.name == "TestFoo", "Failed to discover/name the unit test!")
}

ItTest / testOptions += Tests.Argument(jupiterTestFramework, "-v")
ItTest / javaSource := baseDirectory.value / "src/it/java"
ItTest / classDirectory := baseDirectory.value / "target/scala-2.12/it-classes"
ItTest / resourceDirectory := (baseDirectory apply { baseDir: File => baseDir / "src/it/resources" }).value
