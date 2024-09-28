scalaVersion := "2.13.15"
name := "simple"
libraryDependencies ++= Seq(
  "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
)
