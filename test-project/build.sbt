import sbt.Keys.libraryDependencies
import net.aichler.jupiter.sbt.Import.JupiterKeys._

lazy val junit = (project in file("src/junit"))
  .enablePlugins(JvmPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "junit" % "junit" % "4.13.2" % "test"
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")
  )

lazy val jupiter = (project in file("src/jupiter"))
  .settings(
    libraryDependencies ++= Seq(
      "net.aichler" % "jupiter-interface" % jupiterVersion.value % "test",
      "org.junit.jupiter" % "junit-jupiter-params" % junitJupiterVersion.value % "test"
    ),
    resolvers += Resolver.mavenLocal,
    parallelExecution in Test := true
  )

lazy val vintage = (project in file("src/vintage"))
  .settings(
    libraryDependencies ++= Seq(
      "net.aichler" % "jupiter-interface" % jupiterVersion.value % "test",
      "org.junit.vintage" % "junit-vintage-engine" % junitVintageVersion.value % "test"
    ),
    resolvers += Resolver.mavenLocal
  )

lazy val root = (project in file("."))
  .aggregate(junit)
  .aggregate(jupiter)
  .aggregate(vintage)
