import java.io.PrintWriter

name := "test-project"
libraryDependencies ++= Seq(
  "com.github.sbt.junit" % "jupiter-interface" % "0.17.0+18-3d3bb080+20260312-1453-SNAPSHOT" % Test, // "0.17.0+18-3d3bb080+20260312-1453-SNAPSHOT" JupiterKeys.jupiterVersion.value
  "io.cucumber" % "cucumber-junit-platform-engine" % "7.26.0" % Test,
  "io.cucumber" %% "cucumber-scala" % "8.30.1" % Test,
  "org.junit.platform" % "junit-platform-suite" % JupiterKeys.junitPlatformVersion.value % Test
)


Test / javaOptions += "-Djava.util.logging.config.file=logging.properties"
Test / fork := true

val emptyLogFile = taskKey[Unit]("Init empty log file")

val assertNoWarnings = taskKey[Unit]("Tests")

emptyLogFile := {
  IO.write(file("target/test.log"), "")
}

assertNoWarnings := {
  val log = IO.read(file("target/test.log"))
  println(log)
  assert(
    !log.contains("WARNING: TestExecutionListener [com.github.sbt.junit.jupiter.internal.listeners.FlatPrintingTestListener] threw exception for method: executionFinished"),
    "Expected no warnings in the test log"
  )

}
