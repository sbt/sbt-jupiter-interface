import sbt.Keys.libraryDependencies
import net.aichler.jupiter.sbt.Import.JupiterKeys._


//logBuffered in Test := false

lazy val junit = (project in file("src/junit"))
  .enablePlugins(JvmPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "junit" % "junit" % "4.12" % "test"
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")
  )

lazy val jupiter = (project in file("src/jupiter"))
  .settings(
    libraryDependencies ++= Seq(
      "org.junit.jupiter" % "junit-jupiter-params" % junitJupiterVersion.value % "test"
    ),
    resolvers += Resolver.mavenLocal,
    parallelExecution in Test := true
  )

lazy val root = (project in file("."))
  .aggregate(junit)
  .aggregate(jupiter)
