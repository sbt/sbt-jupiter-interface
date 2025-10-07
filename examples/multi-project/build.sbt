ThisBuild / scalaVersion := "2.13.17"
ThisBuild / libraryDependencies ++= Seq(
  "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
)

lazy val moduleA = project.in(file("moduleA"))
  .settings(
    name := "moduleA"
  )

lazy val moduleB = project.in(file("moduleB"))
  .settings(
    name := "moduleB"
  )

lazy val root = project.in(file("."))
  .aggregate(moduleA, moduleB)
  .settings(
    name := "multi-project"
  )
